package net.tech.cortisolmod.event;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;

import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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
import net.tech.cortisolmod.item.ModItems;
import net.tech.cortisolmod.item.custom.CortisolSwordItem;
import net.tech.cortisolmod.networking.ModMessages;
import net.tech.cortisolmod.networking.packet.CortisolSyncS2CPacket;
import net.tech.cortisolmod.networking.packet.StartIntroCinematicS2CPacket;
import net.tech.cortisolmod.util.AdvancementHelper;
import net.tech.cortisolmod.util.ModDamageTypes;

import java.util.List;
import java.util.Set;

import static java.lang.Math.min;

@Mod.EventBusSubscriber(modid = CortisolMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    // --- LESS CORTISOL  ---
    public static final int EAT_DECREASE_AMOUNT = 4;
    public static final int CAMPFIRE_DECREASE_AMOUNT = 4;
    public static final float CROP_DECREASE_AMOUNT = 1.5f;
    public static final float PIG_RIDING_CORTISOL = 0.1f;
    public static final float FISHING_CORTISOL = 0.1f;

    // --- MORE CORTISOL  ---
    public static final int ATTACK_INCREASE_AMOUNT = 3;
    public static final int DAMAGE_INCREASE_AMOUNT = 5;
    public static final float BREAK_UNDER_INCREASE_AMOUNT = 2f;
    public static final float BREAK_INCREASE_AMOUNT = 1f;
    public static final float CORTISOL_INGOT_INCREASE_AMOUNT = 0.1f;
    public static final float BRIDGE_OVER_VOID_CORTISOL = 2f;
    public static final float WOLF_ON_FIRE_CORTISOL = 4f;

    // --- CONFIGURATION ---
    public static final float CORTISOL_EXPLOSION_RADIUS = 5;
    public static final float BASE_CORTISOL = 30.f;

    // --- THRESHOLDS ---
    public static final int SLOW_THRESHOLD = 20;
    public static final int SPEED_CORTISOL_THRESHOLD = 70;
    public static final int DROP_ITEM_CORTISOL_THRESHOLD = 80;
    public static final int BLINKING_TREASHOLD = 20;
    public static final int SHAKING_START_CORTISOL = 100;
    public static final int DAMAGE_START_CORTISOL = 100;
    public static final int DEATH_CORTISOL = 130;

    // --- STRESS DAMAGE ---
    public static final int DAMAGE_TICK_INTERVAL = 20;
    public static final float DAMAGE_PER_TICK = 1.0f;

    // --- SPECIAL MOBS ---
    public static final float CREEPER_CORTISOL = 1f;
    public static final double CREEPER_CORTISOL_RADIUS = 7;
    public static final float SPECIAL_MOB_CORTISOL = 2.5f;
    public static final double SPECIAL_MOB_CORTISOL_RADIUS = 5;

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
            event.getOriginal().reviveCaps();

            event.getOriginal().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(newStore -> {

                    //newStore.setCortisol(BASE_CORTISOL);

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
            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
            if (player.tickCount % UPDATE_INTERVAL_TICKS == 0) {
                BlockPos playerPos = player.blockPosition();

                for (BlockPos pos : BlockPos.betweenClosed(
                        playerPos.offset(-5, -2, -5),
                        playerPos.offset(5, 2, 5))) {
                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {

                            if (cortisol.getCortisol() > PlayerCortisol.MIN_CORTISOL) {
                                cortisol.subCortisol(CAMPFIRE_DECREASE_AMOUNT,player);

                                ModMessages.sendToAllPlayers(
                                        new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                                );
                            }

                            AdvancementHelper.grant(player, "cortisolmod:cortisol/low_cortisol");
                        break;
                    }
                }

                    // Special mob cortisol
                    AABB detectionZone = player.getBoundingBox().inflate(SPECIAL_MOB_CORTISOL_RADIUS);

                    List<Mob> nearbyMobs = level.getEntitiesOfClass(
                            Mob.class,
                            detectionZone,
                            EntitySelector.NO_SPECTATORS
                    );

                    boolean foundCortisolMob = nearbyMobs.stream()
                            .anyMatch(mob -> mob.getPersistentData().getBoolean("cortisol_mob"));

                    if (foundCortisolMob) {
                        cortisol.addCortisol(SPECIAL_MOB_CORTISOL,player);

                        ModMessages.sendToAllPlayers(
                                new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                        );

                    }

            }



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

                // Grant the 100 cortisol advancement
                if (currentCortisol >= 100) {
                    AdvancementHelper.grant(player, "cortisolmod:hey_whats_that");
                }

                if (player.getVehicle() instanceof Pig){
                    cortisol.subCortisol(PIG_RIDING_CORTISOL, player);
                    ModMessages.sendToAllPlayers(
                            new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                    );
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

                if(player.fishing!=null&&player.fishing.isInWater()){
                    cortisol.subCortisol(FISHING_CORTISOL,player);
                    ModMessages.sendToAllPlayers(
                            new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                    );

                }
                if( player.getInventory().hasAnyOf(Set.of(
                        ModItems.CORTILIUM_INGOT.get() )
                ) )
                {
                    cortisol.addCortisol(CORTISOL_INGOT_INCREASE_AMOUNT,player);
                    ModMessages.sendToAllPlayers(
                            new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                    );
                }


                //slippery hands

//                if (currentCortisol > DROP_ITEM_CORTISOL_THRESHOLD &&
//                        player.getRandom().nextFloat() < DROP_ITEM_CHANCE) {
//
//                    ItemStack stack = player.getMainHandItem();
//
//                    if (!stack.isEmpty()) {
//                        player.drop(stack.copy(), true);
//                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
//                    }
//                }

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

                    held.getOrCreateTag().putFloat("cortisol", currentCortisol);

                    int swordLevel = CortisolSwordItem.getLevel(currentCortisol);
                    int current = held.getOrCreateTag().getInt("cortisol_level");

                    if (swordLevel != current) {
                        held.getOrCreateTag().putInt("cortisol_level", swordLevel);


                        player.getInventory().setChanged();
                        player.inventoryMenu.broadcastChanges();
                    }
                }


            });
        }
    }

    @SubscribeEvent
    public static void onPlayerEat(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getItem().isEdible()) {
                event.getEntity().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                    if (cortisol.getCortisol() > PlayerCortisol.MIN_CORTISOL) {
                        cortisol.subCortisol(EAT_DECREASE_AMOUNT,player);
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
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                if (cortisol.getCortisol() < PlayerCortisol.REAL_MAX_CORTISOL) {


                    cortisol.addCortisol(DAMAGE_INCREASE_AMOUNT, player);

                    ModMessages.sendToAllPlayers(
                            new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                    );

                }


            });
        }
        if (event.getSource().getEntity() instanceof ServerPlayer player && (event.getEntity() instanceof Monster  ||event.getEntity() instanceof Player)) {

            if (event.getEntity() instanceof Monster mob && mob.getPersistentData().getBoolean("cortisol_mob")){

                AdvancementHelper.grant(player, "cortisolmod:cortisol/attack_cortisol_mob");

            }
            if (event.getAmount() > 0) {


                player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {

                    if (cortisol.getCortisol() < PlayerCortisol.REAL_MAX_CORTISOL) {
                        int currentTick = player.tickCount;
                        if (cortisol.getLastHitTick() != currentTick) {

                            cortisol.addCortisol(ATTACK_INCREASE_AMOUNT, player);

                            cortisol.setLastHitTick(currentTick);

                            ModMessages.sendToAllPlayers(
                                    new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                            );

                        }
                    }
                });
            }


        }
        if (event.getSource().is(DamageTypes.ON_FIRE)
                || event.getSource().is(DamageTypes.IN_FIRE)
                || event.getSource().is(DamageTypes.LAVA)
                || event.getSource().is(DamageTypes.HOT_FLOOR)) {

            if (event.getEntity() instanceof Wolf wolf && wolf.isTame()) {
                LivingEntity owner = wolf.getOwner();

                if (owner instanceof Player player) {
                    player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                        cortisol.addCortisol(WOLF_ON_FIRE_CORTISOL, player);

                        ModMessages.sendToAllPlayers(
                                new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                        );

                    });

                }

            }
        }
    }

    @SubscribeEvent
    public static void onPlayerBreak(BlockEvent.BreakEvent event){
        Player player = event.getPlayer();


        player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {


            BlockPos playerPos = player.blockPosition();


            BlockPos pos =event.getPos();
            LevelAccessor level =event.getLevel();
            BlockState state = level.getBlockState(pos);

            if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                cortisol.subCortisol(CROP_DECREASE_AMOUNT, event.getPlayer());

                ModMessages.sendToAllPlayers(
                        new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                );


            }
            if (pos.equals(playerPos.below())&& !(level.isEmptyBlock(pos.north()) ||
                    level.isEmptyBlock(pos.south()) ||
                    level.isEmptyBlock(pos.east()) ||
                    level.isEmptyBlock(pos.west())) ) {
                //increase cortisol when mining under your feets
                cortisol.addCortisol(BREAK_UNDER_INCREASE_AMOUNT, event.getPlayer());

                ModMessages.sendToAllPlayers(
                        new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                );


            }   
        });
    }

    @SubscribeEvent
    public static void onPlayerPlace(BlockEvent.EntityPlaceEvent event){
        LevelAccessor level = event.getLevel();
        BlockPos block =event.getPos();
        for (int y = block.getY()-1; y>=min(level.getMinBuildHeight(),block.getY()-70) ; y--){
            if (!level.getBlockState(new BlockPos(block.getX(),y,block.getZ() )).isAir()){
                return;
            }
        }

        if (event.getEntity() instanceof Player player){
            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol->{
                cortisol.addCortisol(BRIDGE_OVER_VOID_CORTISOL,player);

                ModMessages.sendToAllPlayers(
                        new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                );
            });
        }


    }
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {

                ModMessages.sendToPlayer(
                        new CortisolSyncS2CPacket(
                                player.getId(),
                                cortisol.getCortisol()
                        ),
                        player
                );
            });
        }
    }




    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event){
        if (event.getEntity() instanceof ServerPlayer player && event.getSource().is(ModDamageTypes.CORTISOL)){
            player.level().explode(player,player.getX(),player.getY(),player.getZ(),CORTISOL_EXPLOSION_RADIUS,Level.ExplosionInteraction.TNT);
            AdvancementHelper.grant(player, "cortisolmod:cortisol/kaboom");

        }
        if (event.getEntity() instanceof  Monster mob && mob.getPersistentData().getBoolean("cortisol_mob")&& event.getSource().getEntity() instanceof Player){
            ServerPlayer player = (ServerPlayer) event.getSource().getEntity();

            AdvancementHelper.grant(player, "cortisolmod:cortisol/get_unstressed");
            ItemStack held = player.getMainHandItem();

            if (held.getItem() instanceof CortisolSwordItem && held.getOrCreateTag().getInt("cortisol_level")==4){
                AdvancementHelper.grant(player, "cortisolmod:cortisol/kind_of_easy");

            }

        }
    }

    private static final String INTRO_TAG = "intro_played";
    // Start intro cutscene only if it is the first time the player logs in this world
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
            ModMessages.sendToPlayer(
                    new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol()),
                    player
            );
        });

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



