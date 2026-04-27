package net.tech.cortisolmod.item.custom;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
    import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

import java.util.UUID;

public class CortisolSwordItem extends SwordItem {

    public static final String TAG_CORTISOL = "CortisolValue";

    private static final UUID ATTACK_DAMAGE_UUID =
            UUID.fromString("a2d7c3f1-91c2-4a2d-9c6e-1d5b8f0a1111");

    private static final UUID ATTACK_SPEED_UUID =
            UUID.fromString("b1d2c3f4-1234-5678-9abc-111111111111");

    public CortisolSwordItem(Properties properties) {
        super(Tiers.DIAMOND, 0, -2.4f, properties);
    }

    // =========================
    // LOGIQUE CORTISOL → DMG
    // =========================
    public static float getDamageForCortisol(float cortisol) {
        if (cortisol >= 100f) return 10f;
        if (cortisol >= 80f) return 8f;
        if (cortisol >= 60f) return 6f;
        if (cortisol >= 30f) return 4f;
        return 0f;
    }

    public static int getLevel(float cortisol) {
        if (cortisol >= 100f) return 4;
        if (cortisol >= 80f) return 3;
        if (cortisol >= 60f) return 2;
        if (cortisol >= 30f) return 1;
        return 0;
    }

    // =========================
    // ATTRIBUTS DYNAMIQUES
    // =========================
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            EquipmentSlot slot,
            ItemStack stack
    ) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getAttributeModifiers(slot, stack);
        }

        float cortisol = 0f;

        if (stack.hasTag()) {
            cortisol = stack.getTag().getFloat(TAG_CORTISOL);
        }

        float damage = getDamageForCortisol(cortisol) - 1;

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

        builder.put(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                        ATTACK_DAMAGE_UUID,
                        "Cortisol damage",
                        damage,
                        AttributeModifier.Operation.ADDITION
                )
        );

        builder.put(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(
                        ATTACK_SPEED_UUID,
                        "Weapon speed",
                        -2.4,
                        AttributeModifier.Operation.ADDITION
                )
        );

        return builder.build();
    }
}