package com.xfw.moretidefish.datagen;

import com.li64.tide.data.fishing.FishData;
import com.li64.tide.registries.TideFish;
import com.xfw.moretidefish.MoreTideFish;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MTFDataProvider implements DataProvider {
    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public MTFDataProvider(PackOutput packOutput,
                           CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.packOutput = packOutput;
        this.lookupProvider = lookupProvider;
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        return this.lookupProvider.thenCompose(provider -> {
            Map<ResourceLocation, FishData> data = new HashMap<>();

            //遍历TideFish.DATA_BUILDERS
            TideFish.DATA_BUILDERS.forEach((item, constructor) -> {
                ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(item);

                //物品注册在 tide 命名空间下,检查是否是tide:moretidefish
                if (itemKey.getPath().startsWith("moretidefish_")) {
                    try {
                        FishData.Builder builder = FishData.builder();
                        constructor.accept(builder.fish(item));

                        //添加桶物品
                        if (TideFish.ENTITY_DATA.containsKey(itemKey.getPath())) {
                            ResourceLocation bucketKey = itemKey.withSuffix("_bucket");
                            if (BuiltInRegistries.ITEM.containsKey(bucketKey)) {
                                builder.bucket(BuiltInRegistries.ITEM.get(bucketKey));
                            }
                        }

                        FishData fishData = builder.build();

                        String fishId = itemKey.getPath();

                        ResourceLocation dataKey =MoreTideFish.Resource("fishing/fish/" + fishId);

                        data.put(dataKey, fishData);

                        MoreTideFish.LOGGER.info("Generated fish data for {} -> {}",
                                itemKey, dataKey);

                    } catch (Exception e) {
                        MoreTideFish.LOGGER.error("Failed to generate fish data for item: {}", itemKey, e);
                    }
                }
            });

            return generateJsonFiles(cachedOutput, data);
        });
    }

    private CompletableFuture<?> generateJsonFiles(CachedOutput cachedOutput,
                                                   Map<ResourceLocation, FishData> data) {
        CompletableFuture<?>[] futures = new CompletableFuture[data.size()];
        int i = 0;

        for (Map.Entry<ResourceLocation, FishData> entry : data.entrySet()) {
            ResourceLocation key = entry.getKey();
            FishData fishData = entry.getValue();

            try {
                JsonElement json = FishData.CODEC.encodeStart(JsonOps.INSTANCE, fishData)
                        .getOrThrow();

                String path = String.format("data/%s/%s.json",
                        key.getNamespace(),
                        key.getPath()
                );

                futures[i++] = DataProvider.saveStable(cachedOutput, json,
                        packOutput.getOutputFolder().resolve(path));

            } catch (Exception e) {
                MoreTideFish.LOGGER.error("Failed to save fish data for {}", key, e);
            }
        }

        return CompletableFuture.allOf(futures);
    }

    @Override
    @NotNull
    public String getName() {
        return "More Tide Fish Data";
    }
}