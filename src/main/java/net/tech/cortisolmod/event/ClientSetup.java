package net.tech.cortisolmod.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.client.ClientCortisolData;
import net.tech.cortisolmod.client.CortisolHudOverlay;
import net.tech.cortisolmod.client.EyesHudOverlay;
import net.tech.cortisolmod.client.cinematic.CinematicConfig;
import net.tech.cortisolmod.item.ModItems;
import net.tech.cortisolmod.item.custom.CortisolSwordItem;
import net.tech.cortisolmod.item.custom.ScrollingPhoneItem;

public class ClientSetup {

    public static void loadBlurShader(){
        Minecraft mc = Minecraft.getInstance();

        if (mc.gameRenderer.currentEffect() == null) {
            ResourceLocation blur = new ResourceLocation(CortisolMod.MOD_ID, "shaders/post/cortisol_blur.json");
            mc.gameRenderer.loadEffect(blur);
        }
    }

    @Mod.EventBusSubscriber(modid = CortisolMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("cortisol", CortisolHudOverlay.HUD_CORTISOL);
            event.registerAboveAll("eyes", EyesHudOverlay.HUD_EYES);
        }

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new ResourceManagerReloadListener() {
                @Override
                public void onResourceManagerReload(ResourceManager manager) {
                    CinematicConfig.load(manager);
                }
            });
        }

        @SubscribeEvent
        public static void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ItemProperties.register(
                        ModItems.CORTISOL_SWORD.get(),
                        new ResourceLocation(CortisolMod.MOD_ID, "cortisol_level"),
                        (stack, level, entity, seed) -> {
                            float cortisol = ClientCortisolData.getPlayerCortisol();
                            return (float) CortisolSwordItem.getLevel(cortisol);
                        }
                );
            });
            event.enqueueWork(() -> {
                ItemProperties.register(ModItems.SCROLLING_PHONE.get(),
                        new ResourceLocation("cortisolmod", ScrollingPhoneItem.ANIMATION_TAG),
                        (stack, level, entity, seed) -> {
                            if (stack.hasTag() && stack.getTag().getBoolean(ScrollingPhoneItem.ANIMATION_TAG)) {
                                return 1.0F;
                            }
                            return 0.0F;
                        });
            });

        }


    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ClientLevel)) return;

        loadBlurShader();
    }
}
