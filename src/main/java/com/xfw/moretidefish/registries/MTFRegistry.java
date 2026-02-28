package com.xfw.moretidefish.registries;

import com.li64.tide.data.fishing.MinigameBehavior;
import com.li64.tide.data.journal.FishRarity;
import com.li64.tide.data.journal.JournalGroup;
import com.li64.tide.registries.TideFish;
import com.li64.tide.registries.TideFoods;
import com.li64.tide.registries.TideItems;
import com.li64.tide.registries.blocks.FishDisplayShape;
import com.li64.tide.registries.entities.fish.SmoothSwimmingFish;
import com.li64.tide.registries.entities.fish.TideFishEntity;
import com.xfw.moretidefish.registries.entities.ElectricEel;
import com.xfw.moretidefish.registries.entities.PinkStarfish;
import com.xfw.moretidefish.registries.entities.PufferfishBomb;
import com.xfw.moretidefish.registries.item.LanternFishItem;
import com.xfw.moretidefish.registries.item.PinkStarfishItem;
import com.xfw.moretidefish.registries.item.PufferfishBombItem;
import com.xfw.moretidefish.registries.item.ElectricEelItem;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

public class MTFRegistry {

    public static final Item LANTERN_FISH = new TideFish.Builder("moretidefish_lantern_fish")
            .item(properties->new LanternFishItem())
            .food(TideFoods.RAW_FISH)
            .cookedItem(TideItems.COOKED_FISH)
            .fishData(builder -> builder
                .size(20.0, 35.0, 75.0)
                .strength(0.75f)
                .speed(1f)
                .behavior(MinigameBehavior.DARTS)
                .foundIn(BiomeTags.IS_DEEP_OCEAN)
                .saltwater()
                .selectionWeight(30)
                .elevationRange(-20, 40)
                .overworld()
                .water()
                .journalLocation("journal.info.location.deep_ocean")
                .journalGroup(JournalGroup.SALTWATER)
                .journalRarity(FishRarity.RARE)
                .displayData(display -> display.offsets(-0.1f, -0.05f, 0f))
            )
            .entityData(TideFish.FishEntityData.of(
                    TideFishEntity::new,
                    builder -> builder
                            .sized(0.5f, 0.25f)
                            .clientTrackingRange(4),
                    Mob.createMobAttributes()
                            .add(Attributes.MAX_HEALTH, 3.0)
                            .add(Attributes.MOVEMENT_SPEED, 0.8f)
            ))
            .build();

    //PINK_STARFISH
    public static final Item PINK_STARFISH = new TideFish.Builder("moretidefish_pink_starfish")
            .item(PinkStarfishItem::new)
            .properties(p -> p.rarity(Rarity.RARE))
            .fishData(builder -> builder
                    .size(70.0, 85.0, 120.0)
                    .strength(0.88f)
                    .speed(2.0f)
                    .selectionWeight(1.5)
                    .selectionQuality(0.5)
                    .foundIn(BiomeTags.IS_DEEP_OCEAN)
                    .saltwater()
                    .overworld()
                    .water()
                    .surface()
                    .journalLocation("journal.info.location.deep_ocean")
                    .journalGroup(JournalGroup.SALTWATER)
                    .journalRarity(FishRarity.RARE)
                    .displayData(display -> display
                            .offsets(0f, 0f, 0.02f)
                            .rotation(0f, 0f, 90f))
            )
            .entityData(TideFish.FishEntityData.of(
                    PinkStarfish::new,
                    builder -> builder
                            .sized(0.65f, 0.15f)
                            .clientTrackingRange(4),
                    Mob.createMobAttributes()
                            .add(Attributes.MAX_HEALTH, 10.0)
                            .add(Attributes.MOVEMENT_SPEED, 0.0f)
            ))
            .build();

    public static final Item REAVER_SHARK = new TideFish.Builder("moretidefish_reaver_shark")
            .item(properties->new PickaxeItem(Tiers.DIAMOND,properties.attributes(PickaxeItem.createAttributes(Tiers.DIAMOND, 3.0F, -3F))))
            .food(TideFoods.BIG_RAW_FISH)
            .cookedItem(TideItems.LARGE_COOKED_FISH)
            .fishData(builder -> builder
                .size(210.0, 265.0, 335.0)
                .strength(0.8f)
                .speed(1.7f)
                .behavior(MinigameBehavior.PLATEAU)
                .selectionWeight(3)
                .selectionQuality(0.5)
                .temperature(0.35f, 0.2f)
                .foundIn(BiomeTags.IS_DEEP_OCEAN)
                .saltwater()
                .overworld()
                .water()
                .surface()
                .journalLocation("journal.info.location.deep_ocean")
                .journalGroup(JournalGroup.SALTWATER)
                .journalRarity(FishRarity.VERY_RARE)
                .displayData(display -> display
                        .shape(FishDisplayShape.SHAPE_3x1)
                        .offsets(-0.05f, -0.15f, 0f))
            )
            .entityData(TideFish.FishEntityData.of(
                    SmoothSwimmingFish::new,
                    builder -> builder
                            .sized(1.8f, 0.6f)
                            .clientTrackingRange(4),
                    Mob.createMobAttributes()
                            .add(Attributes.MAX_HEALTH, 25.0f)
                            .add(Attributes.MOVEMENT_SPEED, 1.0f)
            ))
            .build();

    //河豚炸弹
    public static final Item PUFFERFISH_BOMB_ENTITY = new TideFish.Builder("moretidefish_pufferfish_bomb")
            .item(PufferfishBombItem::new)
            .food(TideFoods.BIG_RAW_FISH)
            .properties(p -> p.rarity(Rarity.RARE))
            .fishData(builder -> builder
                    .size(12, 22.0, 75)
                    .strength(0.6f)
                    .speed(1f)
                    .selectionWeight(25)
                    .temperature(1, 0.8f)
                    .overworld()
                    .water()
                    .saltwater()
                    .above(40)
                    .journalLocation("journal.info.location.deep_ocean")
                    .journalGroup(JournalGroup.SALTWATER)
                    .journalRarity(FishRarity.UNCOMMON)
                    .displayData(display -> display
                            .offsets(0.05f, 0f, -0.1f)))
                    .entityData(
                            TideFish.FishEntityData.of(
                                    PufferfishBomb::new,
                                    builder -> builder
                                            .sized(0.7F, 0.7F)
                                            .eyeHeight(0.455F)
                                            .clientTrackingRange(4),
                                    Mob.createMobAttributes()
                                            .add(Attributes.MAX_HEALTH, 6)
                                            .add(Attributes.MOVEMENT_SPEED, 1.0f)
                            ))
            .build();

    //ElectricEel
    public static final Item ELECTRIC_EEL = new TideFish.Builder("moretidefish_electric_eel")
        .item((ElectricEelItem::new))
            .food(TideFoods.BIG_RAW_FISH)
            .cookedItem(TideItems.LARGE_COOKED_FISH)
            .fishData(builder -> builder
                    .size(350.0, 375.0, 400.0)
                    .strength(0.8f)
                    .speed(2.0f)
                    .behavior(MinigameBehavior.JITTER)
                    .selectionWeight(3)
                    .selectionQuality(0.5)
                    .inStructure(BuiltinStructures.OCEAN_MONUMENT)
                    .foundIn(BiomeTags.IS_DEEP_OCEAN)
                    .saltwater()
                    .overworld()
                    .water()
                    .surface()
                    .journalLocation("journal.info.location.deep_ocean")
                    .journalGroup(JournalGroup.SALTWATER)
                    .journalRarity(FishRarity.LEGENDARY)
                    .displayData(display -> display
                            .shape(FishDisplayShape.SHAPE_2x1)
                            .offsets(-0.05f, -0.15f, 0f))
            )
            .entityData(TideFish.FishEntityData.of(
                    ElectricEel::new,
                    builder -> builder
                            .sized(1.2f, 0.3f)
                            .clientTrackingRange(4),
                    Mob.createMobAttributes()
                            .add(Attributes.MAX_HEALTH, 25.0f)
                            .add(Attributes.MOVEMENT_SPEED, 1.0f)
            ))
            .build();

    public static void init() {}
}