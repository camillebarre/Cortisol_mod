package net.tech.cortisolmod.item.custom;

import com.sun.jna.platform.win32.WinDef;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.tech.cortisolmod.cortisol.PlayerCortisolProvider;
import net.tech.cortisolmod.networking.ModMessages;
import net.tech.cortisolmod.networking.packet.CortisolSyncS2CPacket;

public class ScrollingPhoneItem extends Item {
    public static int battery = 100;
    public static int CORTISOL_SUB_PHONE=1;
    public static final String ANIMATION_TAG = "activated";
    private int cooldownTicks = 0;
    public ScrollingPhoneItem(Properties pProperties) {
        super(pProperties);
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {

        if (cooldownTicks>0){
            return InteractionResultHolder.fail(pPlayer.getItemInHand(pHand));
        }

        ItemStack stack = pPlayer.getItemInHand(pHand);


        stack.getOrCreateTag().putBoolean(ANIMATION_TAG, true);

        pPlayer.startUsingItem(pHand);


        return InteractionResultHolder.pass(stack);

    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeLeft) {
        cooldownTicks=20;
        pStack.getOrCreateTag().putBoolean(ANIMATION_TAG, false);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration){


        if (!pLevel.isClientSide && pLivingEntity instanceof Player pPlayer) {


            if (pRemainingUseDuration % 5 == 0) {
                pPlayer.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                    if (cortisol.getCortisol() > 0) {
                        cortisol.subCortisol(CORTISOL_SUB_PHONE);
                        ModMessages.sendToPlayer(new CortisolSyncS2CPacket(cortisol.getCortisol()), (ServerPlayer) pPlayer);
                    }
                });
            }
        }
    }


    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (battery > 0) {
            //battery--;
        }
        if(cooldownTicks>=0){
            cooldownTicks--;
        }

        if (!pIsSelected && pStack.hasTag() && pStack.getTag().getBoolean(ANIMATION_TAG)) {
            pStack.getTag().putBoolean(ANIMATION_TAG, false);
        }

    }

}
