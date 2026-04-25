package net.tech.cortisolmod.event;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.client.ClientCortisolData;
import net.tech.cortisolmod.client.CortisolHudOverlay;
import net.tech.cortisolmod.cortisol.PlayerCortisol;
import net.tech.cortisolmod.cortisol.PlayerCortisolProvider;
import net.tech.cortisolmod.item.ModItems;
import net.tech.cortisolmod.mixin.PostChainAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

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
        private static final int BREATHING_START_CORTISOL = 90;
        private static final float BASE_BREATHING_SPEED = 1.5f;
        private static final float MAX_BREATHING_SPEED = 3.5f;
        private static final float BASE_BREATHING_INTENSITY = 0.005f;
        private static final float MAX_BREATHING_INTENSITY = 0.05f;
        private static final int SCREEN_SHAKING_START_CORTISOL = 80;


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
        public static void onLevelLoad(LevelEvent.Load event) {
            if (!(event.getLevel() instanceof ClientLevel)) return;

            Minecraft mc = Minecraft.getInstance();

            if (mc.gameRenderer.currentEffect() == null) {
                ResourceLocation blur = new ResourceLocation(CortisolMod.MOD_ID, "shaders/post/cortisol_blur.json");
                mc.gameRenderer.loadEffect(blur);
            }
        }
        @SubscribeEvent
        public static void cameraBlur(net.minecraftforge.event.TickEvent.RenderTickEvent event){
            Minecraft mc = Minecraft.getInstance();
            PostChain chain = mc.gameRenderer.currentEffect();


            if ((!(chain instanceof PostChainAccessor accessor))) {
                return;
            }



            List<PostPass> passes = accessor.getPasses();

            for (PostPass pass : passes) {
                EffectInstance effect = pass.getEffect();
                Uniform u = effect.getUniform("RADIUS");

                if (u != null) {


                    float currentCortisol = ClientCortisolData.getPlayerCortisol();
                    float value = min(max(
                            (currentCortisol - 0.75f * PlayerCortisol.REAL_MAX_CORTISOL)
                                    / (0.25f * PlayerCortisol.REAL_MAX_CORTISOL),
                            0), 1) * 0.4f;

                    u.set(value);

                }
            }



        }

    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.SCROLLING_PHONE.get(),
                    new ResourceLocation("cortisolmod", "activated"),
                    (stack, level, entity, seed) -> {
                        if (stack.hasTag() && stack.getTag().getBoolean("activated")) {
                            return 1.0F;
                        }
                        return 0.0F;
                    });
        });
    }

}

