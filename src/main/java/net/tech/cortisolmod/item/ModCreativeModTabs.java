package net.tech.cortisolmod.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.tech.cortisolmod.CortisolMod;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS=
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CortisolMod.MOD_ID);
    public static final RegistryObject<CreativeModeTab> TEST_TAB = CREATIVE_MODE_TABS.register("cortisol_tab",()-> CreativeModeTab.builder().icon(()-> new ItemStack(ModItems.CORTISOL_SWORD.get()))
            .title(Component.translatable("creativetab.cortisol_tab"))
            .displayItems((pParameters, pOutput)-> {
                pOutput.accept(ModItems.SCROLLING_PHONE.get());
                pOutput.accept(ModItems.CORTISOL_SWORD.get());
                pOutput.accept(ModItems.HIGH_CORTISOL_INJECTOR.get());

            })
            .build());
    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
