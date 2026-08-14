package net.tech.cortisolmod.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tech.cortisolmod.CortisolMod;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CortisolMod.MOD_ID);


    public static final RegistryObject<SoundEvent> SYRINGE_USE = registerSoundEvent("syringe_use");


    public static  RegistryObject<SoundEvent> registerSoundEvent(String name){
        return SOUND_EVENTS.register(name,()->SoundEvent.createVariableRangeEvent(new ResourceLocation(CortisolMod.MOD_ID,name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}