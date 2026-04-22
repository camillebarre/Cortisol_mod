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
