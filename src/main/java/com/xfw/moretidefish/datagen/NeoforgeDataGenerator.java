package com.xfw.moretidefish.datagen;

import com.xfw.moretidefish.MoreTideFish;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = MoreTideFish.MODID)
public class NeoforgeDataGenerator  {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();

        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        //空的
        CompletableFuture<TagsProvider.TagLookup<Block>> emptyBlockTags = registries.thenApply(provider -> blockTagKey -> Optional.empty());

        //注册生物群系修改器
        generator.addProvider(event.includeServer(), new MTFBiomeProvider(output, registries));

        //注册鱼类数据生成器
        generator.addProvider(event.includeServer(), new MTFDataProvider(output, registries));

        //注册实体类型标签
        generator.addProvider(event.includeServer(), new MTFEntityTypeTagsProvider(output, registries, existingFileHelper));
        //注册物品标签
        generator.addProvider(event.includeServer(), new MTFItemTagsProvider(output, registries, emptyBlockTags, existingFileHelper));

        //注册战利品
        generator.addProvider(
                event.includeServer(),
                new LootTableProvider(
                        output,
                        Set.of(),
                        List.of(new LootTableProvider.SubProviderEntry(
                                MTFEntityLootProvider::new,
                                LootContextParamSets.ENTITY
                        )),
                        registries
                )
        );

        //
        generator.addProvider(event.includeServer(),new MTFModelProvider(output, existingFileHelper));
    }
}