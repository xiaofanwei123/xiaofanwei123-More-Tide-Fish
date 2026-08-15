package com.xfw.moretidefish.registries;

import com.xfw.moretidefish.MoreTideFish;
import com.xfw.moretidefish.registries.item.AdvancedFishFinderItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MTFItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MoreTideFish.MODID);

    public static DeferredHolder<Item, Item> ADVANCED_FISH_FINDER = ITEMS.register("advanced_fish_finder", () -> new AdvancedFishFinderItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
