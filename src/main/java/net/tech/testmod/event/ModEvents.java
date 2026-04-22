package net.tech.testmod.event;

import com.google.common.eventbus.Subscribe;
import net.minecraft.advancements.critereon.EntityHurtPlayerTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.tech.testmod.TestMod;
import net.tech.testmod.cortisol.PlayerCortisol;
import net.tech.testmod.cortisol.PlayerCortisolProvider;
import net.tech.testmod.networking.ModMessages;
import net.tech.testmod.networking.packet.CortisolSyncS2CPacket;
import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.NotNull;

import java.awt.event.InputEvent;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = TestMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {


    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event){
        if (event.getObject() instanceof Player){
           if (!event.getObject().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).isPresent()){
               event.addCapability(new ResourceLocation(TestMod.MOD_ID, "properties"),new PlayerCortisolProvider());
           }
        }
    }
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event){
        if (event.isWasDeath()){
            event.getOriginal().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
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

            ServerPlayer player = (ServerPlayer) event.player ;
            net.minecraft.world.level.Level level = player.level();

            BlockPos playerPos = player.blockPosition();


            //cortisol decrease when near campfire (low low cortisol)
            if (player.tickCount%10==0) {
                for (BlockPos pos : BlockPos.betweenClosed(
                        playerPos.offset(-5, -2, -5),
                        playerPos.offset(5, 2, 5))) {

                    if (level.getBlockState(pos).getBlock() == Blocks.CAMPFIRE) {

                        player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                            if (cortisol.getCortisol() > 0) {
                                cortisol.subCortisol(1);


                            }
                        });

                        break;
                    }
                }
            }

            //increase cortisol if creeper nearby

            for (Entity entity : level.getEntities(player,player.getBoundingBox().inflate(8))){

                    player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                        if (cortisol.getCortisol() < 75) {
                            if (entity instanceof Creeper && player.tickCount%20==0) {
                                cortisol.addCortisol(1);

                            }
                        }
                    });


            }
            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {

                  //randomly drop item when above 80 cortisol
                if (cortisol.getCortisol() > 80 &&
                        player.getRandom().nextFloat() < 0.001f) {

                    ItemStack stack = player.getMainHandItem();

                    if (!stack.isEmpty()) {
                        player.drop(stack.copy(), true);
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }
                // speed
                // increase if above 70 cortisol
                var attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (attr == null) return;

                UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

                attr.removeModifier(id);

                if (cortisol.getCortisol() > 70 ) {

                    double speedBoost=0.2;

                    AttributeModifier modifier = new AttributeModifier(id,"cortisol speed",speedBoost,AttributeModifier.Operation.MULTIPLY_TOTAL);
                    attr.addTransientModifier(modifier);

                }

                ModMessages.sendToPlayer(new CortisolSyncS2CPacket(cortisol.getCortisol()), player);

            });


        }
    }



    @SubscribeEvent
    public static void onPlayerEat(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (event.getItem().isEdible()) {
                event.getEntity().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                    if (cortisol.getCortisol() > 0) {
                        cortisol.subCortisol(3);
                        ModMessages.sendToPlayer(new CortisolSyncS2CPacket(cortisol.getCortisol()), player);

                    }
                });
            }
        }
    }



    //Add cortisol

    @SubscribeEvent
    public static  void onPlayerAttack(@NotNull AttackEntityEvent event){
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof Monster){
            event.getEntity().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol-> {
                if (cortisol.getCortisol()<100) {
                    cortisol.addCortisol(1);
                    ModMessages.sendToPlayer(new CortisolSyncS2CPacket(cortisol.getCortisol()),player);

                }

            });

        }
    }

    @SubscribeEvent
    public static  void onPlayerDamage(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            event.getEntity().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                if (cortisol.getCortisol() < 100) {
                    cortisol.addCortisol(1);
                    ModMessages.sendToPlayer(new CortisolSyncS2CPacket(cortisol.getCortisol()), player);

                }

            });
        }
    }

    @SubscribeEvent
    public static  void onPlayerBreak(BlockEvent.BreakEvent event){

        event.getPlayer().getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol-> {
            if (cortisol.getCortisol()<100) {
                cortisol.addCortisol(1);
                ModMessages.sendToPlayer(new CortisolSyncS2CPacket(cortisol.getCortisol()),(ServerPlayer) event.getPlayer());

            }

        });


    }

    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event){

        if (!event.getLevel().isClientSide()) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                    ModMessages.sendToPlayer(new CortisolSyncS2CPacket(cortisol.getCortisol()), player);
                });


            }

        }
    }
}



