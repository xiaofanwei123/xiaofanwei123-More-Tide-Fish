package com.xfw.moretidefish.registries.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class FishFinderItem extends Item {
    public FishFinderItem(Properties properties) {
        super(properties);
    }
    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext context, List<Component> lines, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, lines, pIsAdvanced);
        lines.add(Component.translatable("tooltip.moretidefish.fish_finder_tooltip").withStyle(ChatFormatting.GRAY));
    }
}
