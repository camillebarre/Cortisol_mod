package net.tech.cortisolmod;

import com.mojang.logging.LogUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.tech.cortisolmod.block.ModBlocks;
import net.tech.cortisolmod.effect.ModEffects;
import net.tech.cortisolmod.event.ClientEventRender;
import net.tech.cortisolmod.item.ModCreativeModTabs;
import net.tech.cortisolmod.item.ModItems;
import net.tech.cortisolmod.networking.ModMessages;
import net.tech.cortisolmod.particle.ModParticles;
import net.tech.cortisolmod.util.ModSounds;
import net.tech.cortisolmod.worldgen.biome.ModTerraBlender;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CortisolMod.MOD_ID)
public class CortisolMod
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "cortisolmod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public CortisolMod()
    {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();


        ModCreativeModTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeModTabs.register(modEventBus);
        ModEffects.register(modEventBus);
        ModTerraBlender.registerBiomes();

        ModParticles.register(modEventBus);
        ModSounds.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::clientSetup);

    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        ModMessages.register();
    }

    // Add the example block item to the building blocks tab


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEventRender());
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
    }
}
