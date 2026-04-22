package net.tech.cortisolmod.cortisol;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerCortisolProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static  Capability<PlayerCortisol> PLAYER_CORTISOL = CapabilityManager.get(new CapabilityToken<PlayerCortisol>() {  });
    private PlayerCortisol cortisol = null;
    private  final LazyOptional<PlayerCortisol> optional= LazyOptional.of(this::createPlayerCortisol);

    private PlayerCortisol createPlayerCortisol() {
        if (this.cortisol==null) {
            this.cortisol = new PlayerCortisol();
        }
        return this.cortisol;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap==PLAYER_CORTISOL){
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerCortisol().saveNBTData(nbt);
        return nbt;

    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerCortisol().loadNBTData(nbt);
    }
}
