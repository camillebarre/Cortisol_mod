package net.tech.cortisolmod.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.item.custom.HighCortisolAutoInjector;
import net.tech.cortisolmod.item.custom.CortisolSwordItem;
import net.tech.cortisolmod.item.custom.LowCortisolAutoInjector;
import net.tech.cortisolmod.item.custom.ScrollingPhoneItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS=
            DeferredRegister.create(ForgeRegistries.ITEMS, CortisolMod.MOD_ID);


    public static final RegistryObject<Item> CORTISOL_SWORD = ITEMS.register("cortisol_sword",
            () -> new CortisolSwordItem(new Item.Properties())
    );
    public static final RegistryObject<Item> SCROLLING_PHONE=ITEMS.register("scrolling_phone",()->new ScrollingPhoneItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HIGH_CORTISOL_INJECTOR=ITEMS.register("high_cortisol_injector",()->new HighCortisolAutoInjector(new Item.Properties().stacksTo(16), 60 ,15));
    public static final RegistryObject<Item> LOW_CORTISOL_INJECTOR=ITEMS.register("low_cortisol_injector",()->new LowCortisolAutoInjector(new Item.Properties().stacksTo(16), 60 ,15));
    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
