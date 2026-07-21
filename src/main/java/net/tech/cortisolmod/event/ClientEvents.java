package net.tech.cortisolmod.event;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.client.ClientCortisolData;
import net.tech.cortisolmod.client.cinematic.BlinkCinematic;
import net.tech.cortisolmod.cortisol.PlayerCortisol;
import net.tech.cortisolmod.cortisol.PlayerCortisolProvider;
import net.tech.cortisolmod.mixin.PostChainAccessor;

import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;


public class ClientEvents {


    @Mod.EventBusSubscriber(modid = CortisolMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class cameraEffects {

        private static final ResourceLocation RED_TEXTURE =
                new ResourceLocation(CortisolMod.MOD_ID,
                        "textures/misc/red_overlay.png");

        private static final int BREATHING_START_CORTISOL = 90;
        private static final float BASE_BREATHING_SPEED = 1.5f;
        private static final float MAX_BREATHING_SPEED = 3.5f;
        private static final float BASE_BREATHING_INTENSITY = 0.005f;
        private static final float MAX_BREATHING_INTENSITY = 0.05f;
        private static final int SCREEN_SHAKING_START_CORTISOL = 80;
        private static CameraType lastCameraType = null;



        @SubscribeEvent
        public static void onFovCompute(ViewportEvent.ComputeFov event) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) {
                return;
            }

            float cortisol = ClientCortisolData.getPlayerCortisol();
            if (cortisol <= BREATHING_START_CORTISOL) {
                return;
            }

            float progress = min(
                    (float) (cortisol - BREATHING_START_CORTISOL) / (PlayerCortisol.REAL_MAX_CORTISOL - BREATHING_START_CORTISOL),
                    1.0f
            );

            float breathingSpeed = BASE_BREATHING_SPEED + (MAX_BREATHING_SPEED - BASE_BREATHING_SPEED) * progress;
            float breathingIntensity = BASE_BREATHING_INTENSITY + (MAX_BREATHING_INTENSITY - BASE_BREATHING_INTENSITY) * progress;

            long time = System.currentTimeMillis();
            double t = time / 1000.0;
            double breathing = max(0, Math.sin(t * breathingSpeed * Math.PI * 2.0) - 0.4);

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
                float cortisol = ClientCortisolData.getPlayerCortisol();
                if (cortisol > SCREEN_SHAKING_START_CORTISOL) {
                float intensity = (float) (cortisol - SCREEN_SHAKING_START_CORTISOL) / 25;

                    float shakeX = (float) (Math.random() - 0.5) * intensity;
                    float shakeY = (float) (Math.random() - 0.5) * intensity;

                    event.setPitch(event.getPitch() + shakeX);
                    event.setYaw(event.getYaw() + shakeY);
                }
            });
        }


        @SubscribeEvent
        public static void checkCameraPerson(TickEvent.ClientTickEvent event) {
            // Only process once per full tick (END phase ensures world/renderer states are ready)
            if (event.phase != TickEvent.Phase.END) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.gameRenderer == null) return;

            // Ensure we are in first person and that no shader chain is currently active
            if (mc.options.getCameraType().isFirstPerson() && mc.gameRenderer.currentEffect() == null) {
                ClientSetup.loadCortisolShader();
            }
        }

        @SubscribeEvent
        public static void onRenderHotbar(RenderGuiOverlayEvent.Pre event) {
            if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
                if (BlinkCinematic.getBlinkAmount() > 0.23f) {
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public static void shaderUpdate(net.minecraftforge.event.TickEvent.RenderTickEvent event){

            Minecraft mc = Minecraft.getInstance();
            PostChain chain = mc.gameRenderer.currentEffect();


            if ((!(chain instanceof PostChainAccessor accessor))) {
                return;
            }

            List<PostPass> passes = accessor.getPasses();

            for (PostPass pass : passes) {
                EffectInstance effect = pass.getEffect();
                Uniform radius = effect.getUniform("RADIUS");
                Uniform intensity = effect.getUniform("Intensity");
                if (radius != null) {


                    float currentCortisol = ClientCortisolData.getPlayerCortisol();
                    float value = min(max(
                            (currentCortisol - 0.75f * PlayerCortisol.REAL_MAX_CORTISOL)
                                    / (0.25f * PlayerCortisol.REAL_MAX_CORTISOL),
                            0), 1) * 0.4f;

                    radius.set(value);

                }
                if (intensity!=null){

                    intensity.set(max(1-ClientCortisolData.getPlayerCortisol()/30,0f));
                }
            }
        }



    }


}

