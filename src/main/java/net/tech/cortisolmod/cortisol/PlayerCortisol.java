package net.tech.cortisolmod.cortisol;

import net.minecraft.nbt.CompoundTag;

public class PlayerCortisol {
    public static final int MIN_CORTISOL = 0;
    public static final int VISIBLE_MAX_CORTISOL = 100;
    public static final int REAL_MAX_CORTISOL = 130;

    private int cortisol;

    public int getCortisol() {
        return cortisol;
    }

    public void setCortisol(int value) {
        this.cortisol = Math.max(MIN_CORTISOL, Math.min(value, REAL_MAX_CORTISOL));
    }

    public void addCortisol(int add) {
        setCortisol(cortisol + add);
    }

    public void subCortisol(int sub) {
        setCortisol(cortisol - sub);
    }

    public void copyFrom(PlayerCortisol source) {
        this.cortisol = source.cortisol;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("cortisol", cortisol);
    }

    public void loadNBTData(CompoundTag nbt) {
        cortisol = nbt.getInt("cortisol");
    }
}
