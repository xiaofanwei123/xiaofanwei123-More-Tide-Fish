package com.xfw.moretidefish.registries.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class AdvancedFishFinderItem extends com.li64.tide.registries.items.FishFinderItem {

    public AdvancedFishFinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public void addTooltip(ItemStack itemStack, Consumer<Component> consumer) {
        super.addTooltip(itemStack, consumer);
        consumer.accept(Component.translatable("tooltip.moretidefish.advanced_fish_finder_tooltip").withStyle(ChatFormatting.GRAY));
    }
}
