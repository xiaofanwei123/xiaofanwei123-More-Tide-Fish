package com.xfw.moretidefish.registries;

import com.xfw.moretidefish.MoreTideFish;
import com.xfw.moretidefish.registries.item.FishFinderItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MTFItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MoreTideFish.MODID);

    public static DeferredHolder<Item, Item> FISH_FINDER = ITEMS.register("fish_finder", () -> new FishFinderItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
