package net.tech.cortisolmod.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.tech.cortisolmod.cortisol.PlayerCortisolProvider;
import net.tech.cortisolmod.networking.ModMessages;
import net.tech.cortisolmod.networking.packet.CortisolSyncS2CPacket;
import net.tech.cortisolmod.util.ModSounds;

import java.util.function.Consumer;

import static net.minecraft.world.item.SpyglassItem.USE_DURATION;

public class LowCortisolAutoInjectorItem extends Item {

    private final int cooldown;
    private final float cortisol_amount;
    private static final int USE_DURATION= 15;
    public LowCortisolAutoInjectorItem(Properties pProperties, int cooldown, float cortisol_add) {
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

            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                cortisol.subCortisol(this.cortisol_amount,player);

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
    public void initializeClient(Consumer<IClientItemExtensions> consumer){

        ClientAutoInjector.register(consumer, USE_DURATION);
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