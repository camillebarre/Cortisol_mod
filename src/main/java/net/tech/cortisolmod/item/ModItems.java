package net.tech.cortisolmod.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.item.custom.*;
import net.tech.cortisolmod.item.custom.HighCortisolAutoInjectorItem;
import net.tech.cortisolmod.item.custom.CortisolSwordItem;
import net.tech.cortisolmod.item.custom.LowCortisolAutoInjectorItem;
import net.tech.cortisolmod.item.custom.LowCortisolBowItem;
import net.tech.cortisolmod.item.custom.ScrollingPhoneItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS=
            DeferredRegister.create(ForgeRegistries.ITEMS, CortisolMod.MOD_ID);


    public static final RegistryObject<Item> CORTISOL_SWORD = ITEMS.register("cortisol_sword",
            () -> new CortisolSwordItem(new Item.Properties().durability(2500))
    );
    public static final RegistryObject<Item> SCROLLING_PHONE=ITEMS.register("scrolling_phone",()->new ScrollingPhoneItem(new Item.Properties().stacksTo(1).durability(2500)));
    public static final RegistryObject<Item> HIGH_CORTISOL_INJECTOR=ITEMS.register("high_cortisol_injector",()->new HighCortisolAutoInjectorItem(new Item.Properties().stacksTo(16), 20 ,30));
    public static final RegistryObject<Item> LOW_CORTISOL_INJECTOR=ITEMS.register("low_cortisol_injector",()->new LowCortisolAutoInjectorItem(new Item.Properties().stacksTo(16), 20 ,30));
    public static final RegistryObject<Item> CORTILIUM=ITEMS.register("cortilium",()->new CortiliumItem(new Item.Properties()));
    public static final RegistryObject<Item> CORTILIUM_INGOT=ITEMS.register("cortilium_ingot",()->new CortiliumItem(new Item.Properties()));
    public static final RegistryObject<Item> CORTISOL_STABILIZER_SERINGE=ITEMS.register("cortisol_stabilizer_seringe",()->new CortisolStabilizerSeringe(new Item.Properties(), 20,120*20));
    public static final RegistryObject<Item> LOW_CORTISOL_BOW=ITEMS.register("low_cortisol_bow",
            ()->new LowCortisolBowItem(new Item.Properties().stacksTo(1).durability(500))
    );
    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
