package net.tech.cortisolmod.cortisol;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.tech.cortisolmod.effect.ModEffects;
import net.tech.cortisolmod.networking.ModMessages;
import net.tech.cortisolmod.networking.packet.CortisolSyncS2CPacket;
import net.tech.cortisolmod.util.AdvancementHelper;

public class PlayerCortisol {
    public static final float MIN_CORTISOL = 0f;
    public static final float VISIBLE_MAX_CORTISOL = 100f;
    public static final float REAL_MAX_CORTISOL = 130f;
    private int lastHitTick = -1;
    private boolean maxAdvancementGiven = false;


    private float cortisol=30;

    public float getCortisol() {
        return cortisol;
    }

    public void setCortisol(float value) {
        this.cortisol = Math.max(MIN_CORTISOL, Math.min(value, REAL_MAX_CORTISOL)
        );
    }

    public void addCortisol(float add, Player player) {
        if (player.hasEffect(ModEffects.CORTISOL_STABILIZER_EFFECT.get())) {
            setCortisol(cortisol + 0.2f*add);
            return;
        }


        setCortisol(cortisol + add);
        if (!maxAdvancementGiven && this.cortisol >= 100) {
            maxAdvancementGiven = true;
            AdvancementHelper.grant((ServerPlayer) player, "cortisolmod:cortisol/hey_whats_that");
        }

    }

    public void subCortisol(float sub, Player player) {
        if (player.hasEffect(ModEffects.CORTISOL_STABILIZER_EFFECT.get())) {
            setCortisol(cortisol - 0.2f*sub);
            return;
        }
        setCortisol(cortisol - sub);

    }

    public void copyFrom(PlayerCortisol source) {
        this.cortisol = source.cortisol;
    }

    public int getLastHitTick() { return lastHitTick; }

    public void setLastHitTick(int tick) { lastHitTick = tick; }


    public void saveNBTData(CompoundTag nbt) {
        nbt.putFloat("cortisol", cortisol);
    }

    public void loadNBTData(CompoundTag nbt) {
        cortisol = nbt.getFloat("cortisol");
    }
}
