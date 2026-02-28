package com.xfw.moretidefish.misc;

import com.xfw.moretidefish.MoreTideFish;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MTFTags {
    public static final class Items {
        public static final TagKey<Item> MORETIDEFISH =
                TagKey.create(Registries.ITEM, MoreTideFish.Resource("moretidefish"));
    }
}
