package net.tech.cortisolmod.item.custom;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.tech.cortisolmod.cortisol.PlayerCortisolProvider;
import net.tech.cortisolmod.networking.ModMessages;
import net.tech.cortisolmod.networking.packet.CortisolSyncS2CPacket;

public class HighCortisolAutoInjector extends Item {

    private final int cooldown;
    private final float cortisol_amount;


    public HighCortisolAutoInjector(Properties pProperties, int cooldown, float cortisol_add) {
        super(pProperties);
        this.cooldown = cooldown;
        this.cortisol_amount =cortisol_add;

    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        pPlayer.getCooldowns().addCooldown(this, this.cooldown);

        pPlayer.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol->{
            cortisol.addCortisol(cortisol_amount);
            ModMessages.sendToPlayer(new CortisolSyncS2CPacket(cortisol.getCortisol()), (ServerPlayer) pPlayer);

        });
        if (!pPlayer.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemstack,pLevel.isClientSide());

    }
}
