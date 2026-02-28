package com.xfw.moretidefish.datagen;

import com.li64.tide.Tide;
import com.li64.tide.data.loot.ApplyFishEntityLengthFunction;
import com.li64.tide.registries.TideFish;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.function.BiConsumer;

public class MTFEntityLootProvider implements LootTableSubProvider {


    private final HolderLookup.Provider registries;

    public MTFEntityLootProvider(HolderLookup.Provider registries) {
        this.registries = registries;
    }
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        TideFish.ORDERED.stream()
                .filter(this::isTideFishItem)  // 过滤符合条件的鱼
                .forEach(item -> {
                    String path = "entities/" + BuiltInRegistries.ITEM.getKey(item).getPath();
                    ResourceLocation id = Tide.resource(path);
                    ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, id);
                    output.accept(key, simpleFishLootTable(item));
                });
    }

    /**
     * 判断物品是否属于 Tide 模组且 ID 以 "moretidefish_" 开头。
     */
    private boolean isTideFishItem(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key.getNamespace().equals(Tide.MOD_ID) && key.getPath().startsWith("moretidefish_");
    }

    private LootTable.Builder simpleFishLootTable(Item fishItem) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0f))
                .add(LootItem.lootTableItem(fishItem)
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())
                        .apply(ApplyFishEntityLengthFunction::new)
                        .apply(SmeltItemFunction.smelted()
                                .when(shouldSmeltLoot(registries))));

        return LootTable.lootTable().withPool(pool);
    }

    private static AnyOfCondition.Builder shouldSmeltLoot(HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<Enchantment> enchantmentLookup = registries.lookupOrThrow(Registries.ENCHANTMENT);

        LootItemCondition.Builder onFireCondition = LootItemEntityPropertyCondition.hasProperties(
                LootContext.EntityTarget.THIS,
                EntityPredicate.Builder.entity()
                        .flags(EntityFlagsPredicate.Builder.flags().setOnFire(true))
        );

        LootItemCondition.Builder hasSmeltingEnchantCondition = LootItemEntityPropertyCondition.hasProperties(
                LootContext.EntityTarget.DIRECT_ATTACKER,
                EntityPredicate.Builder.entity()
                        .equipment(EntityEquipmentPredicate.Builder.equipment()
                                .mainhand(ItemPredicate.Builder.item()
                                        .withSubPredicate(
                                                ItemSubPredicates.ENCHANTMENTS,
                                                ItemEnchantmentsPredicate.enchantments(List.of(
                                                        new EnchantmentPredicate(
                                                                enchantmentLookup.getOrThrow(EnchantmentTags.SMELTS_LOOT),
                                                                MinMaxBounds.Ints.ANY
                                                        )
                                                ))
                                        )
                                )
                        )
        );

        return AnyOfCondition.anyOf(onFireCondition, hasSmeltingEnchantCondition);
    }
}