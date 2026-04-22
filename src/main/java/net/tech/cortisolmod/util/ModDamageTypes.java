package net.tech.cortisolmod.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.tech.cortisolmod.CortisolMod;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> CORTISOL = ResourceKey.create(
            net.minecraft.core.registries.Registries.DAMAGE_TYPE,
            new ResourceLocation(CortisolMod.MOD_ID, "cortisol")
    );

    public static DamageSource cortisolDamage(ServerLevel level) {
        RegistryAccess registryAccess = level.registryAccess();
        return new DamageSource(registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(CORTISOL));
    }
}