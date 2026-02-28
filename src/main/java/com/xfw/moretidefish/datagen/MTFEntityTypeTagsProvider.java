package com.xfw.moretidefish.datagen;

import com.li64.tide.Tide;
import com.li64.tide.registries.TideEntityTypes;
import com.xfw.moretidefish.MoreTideFish;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MTFEntityTypeTagsProvider extends EntityTypeTagsProvider {

    public MTFEntityTypeTagsProvider(PackOutput output,
                                     CompletableFuture<HolderLookup.Provider> lookupProvider,
                                     @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MoreTideFish.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var aquaticTag = tag(EntityTypeTags.AQUATIC);

        TideEntityTypes.FISH_ENTITIES.stream()
                .filter(entityType -> {
                    ResourceLocation key = entityType.builtInRegistryHolder().key().location();
                    return key.getPath().startsWith("moretidefish_");
                })
                .forEach(aquaticTag::add);
    }

    @Override
    @NotNull
    public String getName() {
        return "Tide Entity Type Tags";
    }
}