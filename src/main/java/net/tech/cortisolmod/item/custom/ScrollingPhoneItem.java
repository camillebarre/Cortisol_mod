package net.tech.cortisolmod.item.custom;

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

    public ScrollingPhoneItem(Properties pProperties) {
        super(pProperties);
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);

        if (!pLevel.isClientSide) {

            CompoundTag nbt = stack.getOrCreateTag();

            boolean currentState = nbt.getBoolean("activated");
            nbt.putBoolean("activated", !currentState);


        }

        pPlayer.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol->{
            if (cortisol.getCortisol()>0) {
                cortisol.subCortisol(1);
                ModMessages.sendToPlayer(new CortisolSyncS2CPacket(cortisol.getCortisol()), (ServerPlayer) pPlayer);
            }
        });


        return InteractionResultHolder.fail(pPlayer.getItemInHand(pHand));

    }



    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (battery > 0) {
            //battery--;
        }

    }

}
