package net.tech.cortisolmod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.client.EyesHudOverlay;
import net.tech.cortisolmod.cortisol.PlayerCortisol;
import net.tech.cortisolmod.cortisol.PlayerCortisolProvider;
import net.tech.cortisolmod.item.custom.CortisolSwordItem;
import net.tech.cortisolmod.networking.ModMessages;
import net.tech.cortisolmod.networking.packet.CortisolSyncS2CPacket;
import net.tech.cortisolmod.networking.packet.StartIntroCinematicS2CPacket;
import net.tech.cortisolmod.util.ModDamageTypes;

import java.util.List;

@Mod.EventBusSubscriber(modid = CortisolMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    public static final int CAMPFIRE_DECREASE_AMOUNT = 1;
    public static final int EAT_DECREASE_AMOUNT = 1;
    public static final int ATTACK_INCREASE_AMOUNT = 1;
    public static final int DAMAGE_INCREASE_AMOUNT = 1;
    public static final int BREAK_INCREASE_AMOUNT = 1;

    public static final int SLOW_THRESHOLD = 5;
    public static final int SPEED_CORTISOL_THRESHOLD = 70;
    public static final int DROP_ITEM_CORTISOL_THRESHOLD = 80;
    public static final int BLINKING_TREASHOLD = 20;
    public static final int SHAKING_START_CORTISOL = 100;
    public static final int DEATH_CORTISOL = 130;
    public static final int DAMAGE_START_CORTISOL = 120;
    public static final int DAMAGE_TICK_INTERVAL = 20;
    public static final float DAMAGE_PER_TICK = 2.0f;


    public static final float CREEPER_CORTISOL= 1f;
    public static final double CREEPER_CORTISOL_RADIUS = 7;


    public static final int UPDATE_INTERVAL_TICKS = 20;
    public static final int LOW_CORTISOL_SLOWNESS_DURATION = 40;
    public static final int LOW_CORTISOL_SLOWNESS_AMPLIFIER = 0;
    public static final int HIGH_CORTISOL_SPEED_DURATION = 40;
    public static final int HIGH_CORTISOL_SPEED_AMPLIFIER = 0;


    public static final float DROP_ITEM_CHANCE = 0.001f;

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event){
        if (event.getObject() instanceof Player){
           if (!event.getObject().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).isPresent()){
               event.addCapability(new ResourceLocation(CortisolMod.MOD_ID, "properties"),new PlayerCortisolProvider());
           }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event){
        if (event.isWasDeath()){
            event.getOriginal().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);

                    if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                        ModMessages.sendToAllPlayers(
                                new CortisolSyncS2CPacket(serverPlayer.getId(), newStore.getCortisol())
                        );
                    }
                });
            });
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event){
        event.register(PlayerCortisol.class);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) {
            ServerPlayer player = (ServerPlayer) event.player;
            Level level = player.level();

            //number of tick for every refresh
            if (player.tickCount % UPDATE_INTERVAL_TICKS == 0) {
                BlockPos playerPos = player.blockPosition();

                for (BlockPos pos : BlockPos.betweenClosed(
                        playerPos.offset(-5, -2, -5),
                        playerPos.offset(5, 2, 5))) {

                    if (level.getBlockState(pos).getBlock() == Blocks.CAMPFIRE) {
                        player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                            if (cortisol.getCortisol() > PlayerCortisol.MIN_CORTISOL) {
                                cortisol.subCortisol(CAMPFIRE_DECREASE_AMOUNT);
                                ModMessages.sendToAllPlayers(
                                        new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                                );
                            }
                        });
                        break;
                    }
                }
                player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                    //creeper cortisol
                    AABB detectionZone = player.getBoundingBox().inflate(CREEPER_CORTISOL_RADIUS);

                    List<Creeper> nearbyCreepers = level.getEntitiesOfClass(Creeper.class, detectionZone, EntitySelector.NO_SPECTATORS);

                    if (!nearbyCreepers.isEmpty()) {
                        cortisol.addCortisol(CREEPER_CORTISOL);
                        ModMessages.sendToAllPlayers(
                                new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                        );
                    }
                });
            };


            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                float currentCortisol = cortisol.getCortisol();

                //slowness
                if (currentCortisol < SLOW_THRESHOLD) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN,
                            LOW_CORTISOL_SLOWNESS_DURATION,
                            LOW_CORTISOL_SLOWNESS_AMPLIFIER,
                            false,
                            false,
                            true
                    ));
                }

                //damage
                if (currentCortisol >= DAMAGE_START_CORTISOL) {
                    if (player.tickCount % DAMAGE_TICK_INTERVAL == 0) {
                        player.hurt(ModDamageTypes.cortisolDamage((ServerLevel) level), DAMAGE_PER_TICK);
                    }
                }

                if (currentCortisol >= DEATH_CORTISOL) {
                    // Kill the player
                    player.hurt(ModDamageTypes.cortisolDamage((ServerLevel) level), Float.MAX_VALUE);
                    return;
                }

                if(player.fishing!=null){
                    cortisol.subCortisol(0.05f);

                }

                //slippery hands

                if (currentCortisol > DROP_ITEM_CORTISOL_THRESHOLD &&
                        player.getRandom().nextFloat() < DROP_ITEM_CHANCE) {

                    ItemStack stack = player.getMainHandItem();

                    if (!stack.isEmpty()) {
                        player.drop(stack.copy(), true);
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }

                // speed
                if (currentCortisol > SPEED_CORTISOL_THRESHOLD) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            HIGH_CORTISOL_SPEED_DURATION,
                            HIGH_CORTISOL_SPEED_AMPLIFIER,
                            false,
                            false,
                            true
                    ));
                }

                ItemStack held = player.getMainHandItem();

                //cortisol sword update
                if (held.getItem() instanceof CortisolSwordItem) {
                    int swordLevel = CortisolSwordItem.getLevel(currentCortisol);
                    int current = held.getOrCreateTag().getInt("cortisol_level");

                    if (swordLevel != current) {
                        held.getOrCreateTag().putInt("cortisol_level", swordLevel);


                        player.getInventory().setChanged();
                        player.inventoryMenu.broadcastChanges();
                    }
                }

                if (held.getItem() instanceof CortisolSwordItem) {
                    var attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
                    if (attribute == null) return;

                    AttributeModifier modifier = attribute.getModifier(CortisolSwordItem.ATTACK_DAMAGE_UUID);
                    float damage = CortisolSwordItem.getDamageForCortisol(cortisol.getCortisol());

                    if (modifier == null || modifier.getAmount() != damage) {
                        attribute.removeModifier(CortisolSwordItem.ATTACK_DAMAGE_UUID);
                        attribute.addTransientModifier(new AttributeModifier(
                                CortisolSwordItem.ATTACK_DAMAGE_UUID,
                                "Cortisol damage",
                                damage,
                                AttributeModifier.Operation.ADDITION
                        ));
                    }
                }

                //random blinking
                if (currentCortisol<BLINKING_TREASHOLD&&player.getRandom().nextFloat()<0.01f){
                    System.out.println((long) (-currentCortisol/0.03+1000));
                    EyesHudOverlay.blink((long) (-currentCortisol/0.03+1000));
                }
                //update cortisol
                ModMessages.sendToAllPlayers(
                        new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                );
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerEat(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getItem().isEdible()) {
                event.getEntity().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                    if (cortisol.getCortisol() > PlayerCortisol.MIN_CORTISOL) {
                        cortisol.subCortisol(EAT_DECREASE_AMOUNT);
                        ModMessages.sendToAllPlayers(
                                new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                        );
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player){
            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                if (cortisol.getCortisol() < PlayerCortisol.REAL_MAX_CORTISOL) {
                    cortisol.addCortisol(DAMAGE_INCREASE_AMOUNT);
                    ModMessages.sendToAllPlayers(
                            new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                    );
                }
            });
        }
        if (event.getSource().getEntity() instanceof ServerPlayer player && event.getEntity() instanceof Monster) {


            if (event.getAmount() > 0) {

                player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                    if (cortisol.getCortisol() < PlayerCortisol.REAL_MAX_CORTISOL) {
                        cortisol.addCortisol(ATTACK_INCREASE_AMOUNT);
                        ModMessages.sendToAllPlayers(
                                new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                        );
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerBreak(BlockEvent.BreakEvent event){
        event.getPlayer().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
            if (cortisol.getCortisol() < PlayerCortisol.REAL_MAX_CORTISOL) {
                cortisol.addCortisol(BREAK_INCREASE_AMOUNT);
                Player player = event.getPlayer();
                ModMessages.sendToAllPlayers(
                        new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                );
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event){
        if (!event.getLevel().isClientSide()) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                    ModMessages.sendToAllPlayers(
                            new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                    );
                });
            }
        }
    }


    private static final String INTRO_TAG = "intro_played";
    // Start intro cutscene only if it is the first time the player logs in this world
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag persistentData = player.getPersistentData();
        CompoundTag forgeData = persistentData.getCompound(ServerPlayer.PERSISTED_NBT_TAG);

        if (!forgeData.getBoolean(INTRO_TAG)) {
            // On retient le fait que le joueur l'a joué
            forgeData.putBoolean(INTRO_TAG, true);
            persistentData.put(ServerPlayer.PERSISTED_NBT_TAG, forgeData);

            // On lance la cinématique
            ModMessages.sendToPlayer(new StartIntroCinematicS2CPacket(), player);
        }
    }
}



