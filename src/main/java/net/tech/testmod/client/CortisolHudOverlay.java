package net.tech.testmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.tech.testmod.TestMod;
import net.tech.testmod.cortisol.PlayerCortisol;

public class CortisolHudOverlay {
    private static final ResourceLocation CORTISOL_BAR = new ResourceLocation(TestMod.MOD_ID, "/textures/cortisol/cortisol_meter.png");
    private static final ResourceLocation CORTISOL_ARROW = new ResourceLocation(TestMod.MOD_ID, "/textures/cortisol/cortisol_arrow.png");

    public static final int HUD_BAR_WIDTH = 110;
    public static final int HUD_BAR_HEIGHT = 65;
    public static final int HUD_ARROW_WIDTH = 30;
    public static final int HUD_ARROW_HEIGHT = 90;
    public static final int HUD_ARROW_X = 40;
    public static final int HUD_ARROW_Y_OFFSET = 37;

    public static final float ANGLE_DEGREES_PER_CORTISOL = 1.8f;
    public static final float ANGLE_OFFSET_DEGREES = -90f;
    public static final float ANGLE_SMOOTHING = 0.1f;

    public static final float SHAKING_START_CORTISOL = 100f;
    public static final float SHAKING_END_CORTISOL = 130f;
    public static final float MAX_SHAKE_ANGLE_DEGREES = 18f;
    public static final float SHAKE_RANDOM_FACTOR = 0.5f;

    private static float angle = (ClientCortisolData.getPlayerCortisol() * ANGLE_DEGREES_PER_CORTISOL) + ANGLE_OFFSET_DEGREES;
    public static IGuiOverlay HUD_CORTISOL = (CortisolHudOverlay::render);

    private static void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        int x_bar = 0;
        int y_bar = screenHeight - HUD_BAR_HEIGHT;

        int currentCortisol = ClientCortisolData.getPlayerCortisol();
        int displayCortisol = Math.min(currentCortisol, PlayerCortisol.VISIBLE_MAX_CORTISOL);

        float targetAngle = (displayCortisol * ANGLE_DEGREES_PER_CORTISOL) + ANGLE_OFFSET_DEGREES;

        float overflowCortisol = Math.max(0, currentCortisol - (int) SHAKING_START_CORTISOL);
        float overflowProgress = Math.min(overflowCortisol / (SHAKING_END_CORTISOL - SHAKING_START_CORTISOL), 1.0f);
        float shakeAngle = 0.0f;

        if (overflowProgress > 0.0f) {
            float shakeStrength = MAX_SHAKE_ANGLE_DEGREES * overflowProgress;
            shakeAngle = (float) ((Math.random() - SHAKE_RANDOM_FACTOR) * 2.0D * shakeStrength);
        }

        angle += (targetAngle - angle) * ANGLE_SMOOTHING;

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CORTISOL_BAR);

        guiGraphics.blit(CORTISOL_BAR, x_bar, y_bar, 0, 0, HUD_BAR_WIDTH, HUD_BAR_HEIGHT, HUD_BAR_WIDTH, HUD_BAR_HEIGHT);

        int x_arrow = HUD_ARROW_X;
        int y_arrow = screenHeight - HUD_ARROW_HEIGHT + HUD_ARROW_Y_OFFSET;
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        pose.translate(x_arrow + HUD_ARROW_WIDTH / 2f, y_arrow + HUD_ARROW_HEIGHT / 2f, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(angle + shakeAngle));
        pose.translate(-x_arrow - HUD_ARROW_WIDTH / 2f, -y_arrow - HUD_ARROW_HEIGHT / 2f, 0);

        guiGraphics.blit(CORTISOL_ARROW, x_arrow, y_arrow, 0, 0, HUD_ARROW_WIDTH, HUD_ARROW_HEIGHT, HUD_ARROW_WIDTH, HUD_ARROW_HEIGHT);
        pose.popPose();

        if (!FMLEnvironment.production) {
            String debugText = "Cortisol: " + ClientCortisolData.getPlayerCortisol();
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                debugText,
                6,
                6,
                0xFFFFFF,
                true
            );
            String debugCommand = "Cortisol Command: /cortisol";
            guiGraphics.drawString(
                Minecraft.getInstance().font,
                debugCommand,
                6,
                16,
                0xFFFFFF,
                true
            );
        }
    }
}
