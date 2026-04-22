package net.tech.cortisolmod.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ExplodingCarLauncherItem extends Item {
    private static final int COOLDOWN_TIME= 20*2;
    private int cooldownTicks = 0;
    public ExplodingCarLauncherItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {

        if (cooldownTicks>0){
            return InteractionResultHolder.fail(pPlayer.getItemInHand(pUsedHand));
        }
        if (!pLevel.isClientSide){
            PrimedTnt primedTnt = new PrimedTnt(EntityType.TNT,pLevel);
            primedTnt.setPos(pPlayer.getX(),pPlayer.getY()+2,pPlayer.getZ());
            Vec3 launchDirection = pPlayer.getLookAngle();
            primedTnt.setDeltaMovement(launchDirection.scale(3));
            primedTnt.setFuse(10);
            pLevel.addFreshEntity(primedTnt);

            pPlayer.getItemInHand(pUsedHand).hurtAndBreak(1,pPlayer,p -> p.broadcastBreakEvent(pUsedHand));

            cooldownTicks=COOLDOWN_TIME;
        }

        return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if(cooldownTicks>0){
            cooldownTicks--;
        }
    }
}
