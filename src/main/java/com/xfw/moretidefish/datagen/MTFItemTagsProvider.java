package com.xfw.moretidefish.datagen;

import com.li64.tide.Tide;
import com.li64.tide.data.TideTags;
import com.li64.tide.registries.TideFish;
import com.xfw.moretidefish.misc.MTFTags;
import com.xfw.moretidefish.registries.MTFRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MTFItemTagsProvider extends ItemTagsProvider {

    public MTFItemTagsProvider(PackOutput output,
                                CompletableFuture<HolderLookup.Provider> lookupProvider,
                                CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
                                @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Tide.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        //==========我的标签定义==========
        var moretidefish = tag(MTFTags.Items.MORETIDEFISH);

        //遍历所有已注册的物品，筛选 Tide 模组的 moretidefish_ 鱼
        TideFish.FISH_KEYS.stream()
                .filter(key -> key.location().getPath().startsWith("moretidefish_"))
                .map(BuiltInRegistries.ITEM::get)
                .filter(Objects::nonNull)
                .forEach(moretidefish::add);


        // ==========鱼相关标签==========
        //可烹饪鱼标签
        tag(TideTags.Items.COOKABLE_FISH).addTag(MTFTags.Items.MORETIDEFISH);


        //传奇鱼标签
        tag(TideTags.Items.LEGENDARY_FISH)
                .add(MTFRegistry.ELECTRIC_EEL);

        //鱼标签：模组中符合条件的鱼
        tag(TideTags.Items.FISH).addTag(MTFTags.Items.MORETIDEFISH);

        // ========== 其他标签 ==========
        tag(ItemTags.HEAD_ARMOR)
                .add(MTFRegistry.LANTERN_FISH);


        // ========== 钓鱼装备相关 ==========
        //鱼竿
//        tag(TideTags.Items.FISHING_RODS)
//                .add(Items.FISHING_ROD,
//                        TideItems.STONE_FISHING_ROD);

        //鱼线
//        tag(TideTags.Items.LINES)
//                .add(TideItems.FISHING_LINE);

        //鱼钩
//        tag(TideTags.Items.HOOKS)
//                .add(TideItems.FISHING_HOOK);

        //鱼饵
//        tag(TideTags.Items.BAIT_PLANTS)
//                .add(TideItems.BAIT);

    }

    @Override
    @NotNull
    public String getName() {
        return "moretidefish Item Tags";
    }
}