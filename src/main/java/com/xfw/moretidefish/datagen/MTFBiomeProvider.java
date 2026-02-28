package com.xfw.moretidefish.datagen;

import com.xfw.moretidefish.MoreTideFish;
import com.xfw.moretidefish.registries.MTFConfigs;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MTFBiomeProvider extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, MTFBiomeProvider::bootstrap);

    public MTFBiomeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(MoreTideFish.MODID));
    }

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var biomes = context.lookup(Registries.BIOME);

        MTFConfigs.onRegisterSpawnConfigs(config -> {
            String entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(config.entityType()).getPath();
            ResourceKey<BiomeModifier> key = ResourceKey.create(
                    NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                    MoreTideFish.Resource("add_" + entityKey + "_spawns")//TODO不确定命名空间是否得和生物命名空间一致
            );
            context.register(key, new BiomeModifiers.AddSpawnsBiomeModifier(
                    biomes.getOrThrow(config.biomes()),
                    List.of(new MobSpawnSettings.SpawnerData(
                            config.entityType(), config.weight(),
                            config.minGroup(), config.maxGroup()
                    ))
            ));
        });
    }
}
