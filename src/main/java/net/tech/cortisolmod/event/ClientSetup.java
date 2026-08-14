package net.tech.cortisolmod.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.client.ClientCortisolData;
import net.tech.cortisolmod.client.CortisolHudOverlay;
import net.tech.cortisolmod.client.EyesHudOverlay;
import net.tech.cortisolmod.client.cinematic.CinematicConfig;
import net.tech.cortisolmod.item.ModItems;
import net.tech.cortisolmod.item.custom.CortisolSwordItem;
import net.tech.cortisolmod.item.custom.LowCortisolBowItem;
import net.tech.cortisolmod.item.custom.ScrollingPhoneItem;
import net.tech.cortisolmod.particle.CortisolParticle;
import net.tech.cortisolmod.particle.ModParticles;

public class ClientSetup {

    public static void loadCortisolShader(){
        Minecraft mc = Minecraft.getInstance();


        ResourceLocation blur = new ResourceLocation(CortisolMod.MOD_ID, "shaders/post/cortisol_shader.json");
        mc.gameRenderer.loadEffect(blur);

    }


    @Mod.EventBusSubscriber(modid = CortisolMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerParticleProvider(RegisterParticleProvidersEvent event){
            event.registerSpriteSet(ModParticles.CORTISOL_PARTICLE.get(), CortisolParticle.Provider::new);
        }

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
                        new ResourceLocation("cortisolmod", "cortisol_level"),
                        (stack, level, entity, seed) -> {

                            // If it's not a Player
                            if (!(entity instanceof net.minecraft.world.entity.player.Player player)) {
                                return 0f;
                            }

                            float cortisol = ClientCortisolData.getPlayerCortisol(player.getId());
                            return CortisolSwordItem.getLevel(cortisol);
                        }
                );
                ItemProperties.register(
                        ModItems.LOW_CORTISOL_BOW.get(),
                        new ResourceLocation("cortisolmod", "cortisol_level"),
                        (stack, level, entity, seed) -> {
                            if (!(entity instanceof Player player)) return 0f;
                            return LowCortisolBowItem.getLevel(ClientCortisolData.getPlayerCortisol(player.getId()));
                        }
                );

                ItemProperties.register(
                        ModItems.LOW_CORTISOL_BOW.get(),
                        new ResourceLocation("cortisolmod", "cortisol_level"),
                        (stack, level, entity, seed) -> {
                            if (!(entity instanceof Player player)) return 0f;
                            return LowCortisolBowItem.getLevel(ClientCortisolData.getPlayerCortisol(player.getId()));
                        }
                );
                ItemProperties.register(
                        ModItems.LOW_CORTISOL_BOW.get(),
                        new ResourceLocation("pulling"),
                        (stack, level, entity, seed) -> {
                            return entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
                        }
                );

                ItemProperties.register(
                        ModItems.LOW_CORTISOL_BOW.get(),
                        new ResourceLocation("pull"),
                        (stack, level, entity, seed) -> {
                            if (entity == null) return 0.0F;
                            return entity.getUseItem() != stack ? 0.0F : (float)(stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F;
                        }
                );
            });
            event.enqueueWork(() -> {
                ItemProperties.register(
                        ModItems.SCROLLING_PHONE.get(),
                        new ResourceLocation("cortisolmod", ScrollingPhoneItem.ANIMATION_TAG),
                        (stack, level, entity, seed) -> {
                            if (stack.hasTag() && stack.getTag().getBoolean(ScrollingPhoneItem.ANIMATION_TAG)) {
                                return 1.0F;
                            }
                            return 0.0F;
                        }
                );
            });

        }


    }
    @Mod.EventBusSubscriber(modid = CortisolMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientForgeBusEvents {

        @SubscribeEvent
        public static void onLevelLoad(LevelEvent.Load event) {
            if (!(event.getLevel() instanceof ClientLevel)) return;
            loadCortisolShader();
        }
    }


}
