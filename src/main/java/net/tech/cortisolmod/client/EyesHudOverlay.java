package net.tech.cortisolmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.client.cinematic.BlinkCinematic;
import net.minecraft.resources.ResourceLocation;

import static net.tech.cortisolmod.client.cinematic.BlinkCinematic.playSequence;

public class EyesHudOverlay {

    public static final IGuiOverlay HUD_EYES = (gui, graphics, partialTick, screenWidth, screenHeight) -> {

        float blink = BlinkCinematic.getBlinkAmount();
        boolean logoVisible = BlinkCinematic.isLogoVisible();

        if (blink <= 0f && !logoVisible) return;

        if (blink > 0f) {
            renderEye(graphics, screenWidth, screenHeight, blink);
        }

        if (logoVisible) {
            renderLogo(graphics, screenWidth, screenHeight, BlinkCinematic.getLogoAlpha());
        }
    };

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        // Logo seul : pas besoin de toucher aux yeux
        float blink = BlinkCinematic.getBlinkAmount(); // unique appel, met à jour l'état
        boolean logoVisible = BlinkCinematic.isLogoVisible();

        if (blink <= 0f && !logoVisible) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics gg = event.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        if (blink > 0f) {
            renderEye(gg, w, h, blink);
        }

        if (logoVisible) {
            renderLogo(gg, w, h, BlinkCinematic.getLogoAlpha());
        }
    }



    // Simple func to blink eyes one time
    public static void blink() {
        // -1 to use pre-defined duration
        // eye closes %, duration, pause before continue
        float[] blinkSequence = {
                0.9f, -1f, 0f,
                0f, -1f, 0f,
        };
        playSequence(blinkSequence);
    }
    private static void renderEye(GuiGraphics gg, int w, int h, float blink) {
        float t = blink;
        t = t * t * (3f - 2f * t);

        // Skip total si invisible (sécurité)
        if (t <= 0f) return;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        int segments = 14;
        int maxCurve = 140;

        for (int i = 0; i < segments; i++) {
            float f = i / (float) segments;
            float nx = f * 2f - 1f;
            float curve = nx * nx;
            int yOffset = (int) (curve * maxCurve * t);

            int yEnd = (int) (h * 0.5f * t) + yOffset;
            // Skip ce segment si height == 0
            if (yEnd > 0) {
                int xStart = (int) (f * w);
                int xEnd   = (int) ((f + 1f / segments) * w);
                gg.fill(xStart, 0, xEnd, yEnd, 0xFF000000);
            }
        }

        for (int i = 0; i < segments; i++) {
            float f = i / (float) segments;
            float nx = f * 2f - 1f;
            float curve = nx * nx;
            int yOffset = (int) (curve * maxCurve * t);

            int yStart = (int) (h - h * 0.5f * t) - yOffset;
            // Skip ce segment si height == 0
            if (yStart < h) {
                int xStart = (int) (f * w);
                int xEnd   = (int) ((f + 1f / segments) * w);
                gg.fill(xStart, yStart, xEnd, h, 0xFF000000);
            }
        }

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }


    private static final ResourceLocation LOGO = new ResourceLocation(CortisolMod.MOD_ID, "textures/gui/logo.png");
    private static final int LOGO_TEXTURE_WIDTH = 1003;
    private static final int LOGO_TEXTURE_HEIGHT = 283;

    private static void renderLogo(GuiGraphics gg, int w, int h, float alpha) {
        float targetW = w * 0.4f;
        float scale = targetW / LOGO_TEXTURE_WIDTH;
        int logoW = (int) targetW;
        int logoH = (int) (LOGO_TEXTURE_HEIGHT * scale);

        int x = (w - logoW) / 2;
        int y = (h - logoH) / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        gg.pose().pushPose();
        gg.pose().translate(x, y, 0);
        gg.pose().scale(scale, scale, 1f);

        gg.blit(LOGO, 0, 0, 0, 0, LOGO_TEXTURE_WIDTH, LOGO_TEXTURE_HEIGHT, LOGO_TEXTURE_WIDTH, LOGO_TEXTURE_HEIGHT);

        gg.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
