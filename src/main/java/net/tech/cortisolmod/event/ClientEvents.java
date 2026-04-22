package net.tech.cortisolmod.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.client.ClientCortisolData;
import net.tech.cortisolmod.client.CortisolHudOverlay;
import net.tech.cortisolmod.cortisol.PlayerCortisol;
import net.tech.cortisolmod.cortisol.PlayerCortisolProvider;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = CortisolMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("cortisol", CortisolHudOverlay.HUD_CORTISOL);
        }
    }

    @Mod.EventBusSubscriber(modid = CortisolMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class cameraShake {
        private static final int BREATHING_START_CORTISOL = 80;
        private static final float BASE_BREATHING_SPEED = 1.5f;
        private static final float MAX_BREATHING_SPEED = 3.5f;
        private static final float BASE_BREATHING_INTENSITY = 0.01f;
        private static final float MAX_BREATHING_INTENSITY = 0.1f;

        @SubscribeEvent
        public static void onFovCompute(ViewportEvent.ComputeFov event) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) {
                return;
            }

            int cortisol = ClientCortisolData.getPlayerCortisol();
            if (cortisol <= BREATHING_START_CORTISOL) {
                return;
            }

            float progress = Math.min(
                    (float) (cortisol - BREATHING_START_CORTISOL) / (PlayerCortisol.REAL_MAX_CORTISOL - BREATHING_START_CORTISOL),
                    1.0f
            );

            float breathingSpeed = BASE_BREATHING_SPEED + (MAX_BREATHING_SPEED - BASE_BREATHING_SPEED) * progress;
            float breathingIntensity = BASE_BREATHING_INTENSITY + (MAX_BREATHING_INTENSITY - BASE_BREATHING_INTENSITY) * progress;

            long time = System.currentTimeMillis();
            double t = time / 1000.0;
            double breathing = Math.max(0, Math.sin(t * breathingSpeed * Math.PI * 2.0) - 0.4);

            double baseFov = event.getFOV();
            double newFov = baseFov * (1.0f - (float) (breathing * breathingIntensity));

            event.setFOV(newFov);
        }

        @SubscribeEvent
        public static void screenShaking(ViewportEvent.ComputeCameraAngles event) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) {
                return;
            }

            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(data -> {
                int cortisol = ClientCortisolData.getPlayerCortisol();
                if (cortisol > 80) {
                    float intensity = (float) (cortisol - 80) / 20;

                    float shakeX = (float) (Math.random() - 0.5) * intensity;
                    float shakeY = (float) (Math.random() - 0.5) * intensity;

                    event.setPitch(event.getPitch() + shakeX);
                    event.setYaw(event.getYaw() + shakeY);
                }
            });
        }
    }
}
