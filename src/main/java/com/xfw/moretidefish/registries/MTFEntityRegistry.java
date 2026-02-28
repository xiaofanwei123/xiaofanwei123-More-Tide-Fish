package com.xfw.moretidefish.registries;

import com.xfw.moretidefish.MoreTideFish;
import com.xfw.moretidefish.registries.entities.ElectricEelWhip;
import com.xfw.moretidefish.registries.entities.PinkStarfishItemEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MTFEntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE,MoreTideFish.MODID);

    public static final Supplier<EntityType<PinkStarfishItemEntity>>
            PINK_STARFISH_ITEM_ENTITY = ENTITIES.register(
            "pink_starfish_item",
            () -> EntityType.Builder.<PinkStarfishItemEntity>of(PinkStarfishItemEntity::new,MobCategory.MISC)
                    .sized(0.65F, 0.65F)
                    .clientTrackingRange(16)
                    .updateInterval(10)
                    .build("pink_starfish_item")
    );
    public static final Supplier<EntityType<ElectricEelWhip>> ELECTRIC_EEL_ITEM = ENTITIES.register(
            "electric_eel_item",
            () -> EntityType.Builder.<ElectricEelWhip>of(ElectricEelWhip::new, MobCategory.MISC)
                    .sized(0.0f, 0.0f)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("electric_eel_item")
    );

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
