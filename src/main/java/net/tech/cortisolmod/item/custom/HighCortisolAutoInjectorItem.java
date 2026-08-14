package net.tech.cortisolmod.item.custom;

import net.minecraft.server.level.ServerPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.tech.cortisolmod.cortisol.PlayerCortisolProvider;
import net.tech.cortisolmod.networking.ModMessages;
import net.tech.cortisolmod.networking.packet.CortisolSyncS2CPacket;
import net.tech.cortisolmod.util.AdvancementHelper;
import net.tech.cortisolmod.util.ModSounds;

import java.util.function.Consumer;

public class HighCortisolAutoInjectorItem extends Item {

    private final int cooldown;
    private final float cortisol_amount;
    private final int useDuration= 15;
    public HighCortisolAutoInjectorItem(Properties pProperties, int cooldown, float cortisol_add) {
        super(pProperties);
        this.cooldown = cooldown;
        this.cortisol_amount = cortisol_add;

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // Lance l'animation de "chargement" / utilisation
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {

            level.playSound(
                    null,
                    player.blockPosition(),
                    ModSounds.SYRINGE_USE.get(),
                    SoundSource.PLAYERS,
                    1.0f,
                    1.0f
            );

            AdvancementHelper.grant((ServerPlayer) player, "cortisolmod:cortisol/i_love_drugs");

            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                cortisol.addCortisol(this.cortisol_amount,player);

                ModMessages.sendToAllPlayers(
                        new CortisolSyncS2CPacket(player.getId(), cortisol.getCortisol())
                );
            });

            player.getCooldowns().addCooldown(this, cooldown);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        consumer.accept(new IClientItemExtensions() {

            private final HumanoidModel.ArmPose INJECT_POSE =
                    HumanoidModel.ArmPose.create("INJECT", true,
                            (model, entity, arm) -> {
                                float x= (float)(entity.getUseItemRemainingTicks()-2) /useDuration;
                                float progress =
                                        1.0F - (x);

                                float anim = Math.max((float)Math.sin(progress * Math.PI)*(float)Math.exp( 1- 2.8*x),0f);

                                if (arm == HumanoidArm.RIGHT) {
                                    model.rightArm.yRot = -0.5F;
                                    model.rightArm.xRot = -anim * 2.0F;
                                    model.rightArm.zRot = -0.7F;

                                    model.leftArm.yRot = 0.7F;
                                    model.leftArm.xRot = -0.3F;
                                    model.leftArm.zRot = 0.3F;

                                } else {

                                    model.leftArm.yRot = -0.5F;
                                    model.leftArm.xRot = -anim * 2.0F;
                                    model.leftArm.zRot = -0.7F;

                                    model.rightArm.yRot = 0.7F;
                                    model.rightArm.xRot = -0.3F;
                                    model.rightArm.zRot = 0.3F;
                                }
                            });

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (!itemStack.isEmpty()) {
                    if (entityLiving.getUsedItemHand() == hand && entityLiving.getUseItemRemainingTicks() > 0) {
                        return INJECT_POSE;
                    }
                }
                return HumanoidModel.ArmPose.EMPTY;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                if (player.isUsingItem()
                        && ItemStack.isSameItemSameTags(player.getUseItem(), itemInHand)) {
                    float x= (float)(player.getUseItemRemainingTicks()-2) /useDuration;

                    float progress =player.getUseItemRemainingTicks()-5;


                    float anim = Math.max((float)(Math.exp(-0.316*(progress))*(1-Math.exp(-3.8*progress))),0f);

                    int dir = arm == HumanoidArm.RIGHT ? 1 : -1;

                    // avant / arrière
                    //poseStack.translate(0.0F, -curve * 0.10F, -curve * 0.35F);

                    //haut/bas
                    poseStack.translate(0.0F, anim*0.5f, 0.0F);

                    // rotation
                    //poseStack.mulPose(Axis.XP.rotationDegrees(-curve * 35f));
                }


                return false;
            }
        });
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 15;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }
}
