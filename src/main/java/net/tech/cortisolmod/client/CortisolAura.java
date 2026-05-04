package net.tech.cortisolmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tech.cortisolmod.CortisolMod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = CortisolMod.MOD_ID, value = Dist.CLIENT)
public class CortisolAura {

    private static final float AURA_THRESHOLD = 80f;
    private static final double RANGE = 16.0;

    // COLORS
    private static final DustParticleOptions ORANGE =
            new DustParticleOptions(new Vector3f(1.0f, 0.4f, 0.0f), 1.2f);

    private static final DustParticleOptions RED =
            new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.2f);

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null) return;
        // Only process every 3 ticks
        if (Minecraft.getInstance().level.getGameTime() % 3 != 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Player clientPlayer = mc.player;

        for (Player player : mc.level.players()) {

            // Don't spawn aura on self ???
            //if (player == clientPlayer) continue;

            float cortisol = ClientCortisolData.getPlayerCortisol(player.getId());
            if (cortisol < AURA_THRESHOLD) continue;
            if (clientPlayer.distanceTo(player) > RANGE) continue;

            spawnAura(player, cortisol);
        }
    }

    private static void spawnAura(Player player, float cortisol) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        double intensity = Math.min(1.5, (cortisol - AURA_THRESHOLD) / 20.0);

        int count = (int)(2 + intensity * 6);

        for (int i = 0; i < count; i++) {

            double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.8;
            double y = player.getY() + 0.1; // 👈 AU PIEDS
            double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.8;

            // 80–89 ORANGE
            if (cortisol < 90) {
                mc.level.addParticle(
                        ORANGE,
                        x, y, z,
                        0, 0.02, 0
                );
            }

            // 90–99 MIX ORANGE + RED
            else if (cortisol < 100) {
                mc.level.addParticle(
                        ORANGE,
                        x, y, z,
                        0, 0.03, 0
                );
                if (player.getRandom().nextFloat() < 0.5f) {
                    mc.level.addParticle(
                            RED,
                            x, y, z,
                            0, 0.04, 0
                    );
                }
            }

            // 100+ FIRE + RED
            else {
                mc.level.addParticle(
                        RED,
                        x, y, z,
                        0, 0.05, 0
                );
                mc.level.addParticle(
                        ParticleTypes.FLAME,
                        x, y, z,
                        0, 0.04, 0
                );
            }
        }
    }
}