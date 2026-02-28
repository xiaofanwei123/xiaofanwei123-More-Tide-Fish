package com.xfw.moretidefish.registries;

import com.li64.tide.registries.TideEntityTypes;
import com.li64.tide.registries.entities.models.FishModel;
import com.li64.tide.registries.entities.renderers.FishRenderer;
import com.li64.tide.registries.entities.renderers.GlowingEyesFishRenderer;
import com.xfw.moretidefish.MoreTideFish;
import com.xfw.moretidefish.registries.models.*;
import com.xfw.moretidefish.registries.renderers.ElectricEelItemRenderer;
import com.xfw.moretidefish.registries.renderers.PinkStarfishItemRenderer;
import com.xfw.moretidefish.registries.renderers.PinkStarfishRenderer;
import com.xfw.moretidefish.registries.renderers.PufferfishBombEntityRender;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

//TODO为鱼注册模型和渲染
public class MTFModels {
    public static final ArrayList<MTFModels.RendererRegistration<?>> RENDERERS = new ArrayList<>();
    public static final Map<ModelLayerLocation, Supplier<LayerDefinition>> LAYER_DEFS = new HashMap<>();


    public record RendererRegistration<T extends Entity>(EntityType<T> entityType, EntityRendererProvider<T> renderer) {}


    public MTFModels() {
    }

    public static void init() {
        //灯笼鱼
        registerCustomFish("moretidefish_lantern_fish", LanternFishModel::new, LanternFishModel.MODEL_LOCATION,LanternFishModel::createBodyLayer, GlowingEyesFishRenderer::new);

        //海星
        registerCustomFish("moretidefish_pink_starfish", PinkStarfishModel::new, PinkStarfishModel.MODEL_LOCATION, PinkStarfishModel::createBodyLayer, (key, model, ctx) -> new PinkStarfishRenderer(key, (PinkStarfishModel) model, ctx));
        registerEntityRenderer(MTFEntityRegistry.PINK_STARFISH_ITEM_ENTITY.get(), PinkStarfishItemRenderer::new);
        registerLayerDefinition(PinkStarfishModel.MODEL_LOCATION, PinkStarfishModel::createBodyLayer);

        //掠夺鲨
        registerSimpleFish("moretidefish_reaver_shark", ReaverSharkModel::new, ReaverSharkModel.MODEL_LOCATION,ReaverSharkModel::createBodyLayer);

        //爆炸河豚
        registerCustomFish("moretidefish_pufferfish_bomb", PufferfishBombBigModel::new, PufferfishBombBigModel.MODEL_LOCATION, PufferfishBombBigModel::createBodyLayer, PufferfishBombEntityRender::new);
        registerLayerDefinition(PufferfishBombMidModel.MODEL_LOCATION, PufferfishBombMidModel::createBodyLayer);
        registerLayerDefinition(PufferfishBombSmallModel.MODEL_LOCATION, PufferfishBombSmallModel::createBodyLayer);

        //电鳗
        registerSimpleFish("moretidefish_electric_eel", ElectricEelModel::new, ElectricEelModel.MODEL_LOCATION,ElectricEelModel::createBodyLayer,GlowingEyesFishRenderer::new);
        registerEntityRenderer(MTFEntityRegistry.ELECTRIC_EEL_ITEM.get(), ElectricEelItemRenderer::new);
        registerLayerDefinition(ElectricEelItemModel.LAYER_LOCATION, ElectricEelItemModel::createBodyLayer);
    }

    public static void registerSimpleFish(String key, Function<EntityRendererProvider.Context, FishModel> modelSupplier,
                                          ModelLayerLocation location, Supplier<LayerDefinition> layerDefinition) {
        registerCustomFish(key, modelSupplier, location, layerDefinition, FishRenderer::new);
    }

    public static void registerSimpleFish(String key, Function<EntityRendererProvider.Context, FishModel> modelSupplier,
                                          ModelLayerLocation location, Supplier<LayerDefinition> layerDefinition, TriFunction<String, FishModel, EntityRendererProvider.Context, FishRenderer<?>> rendererSupplier) {
        registerCustomFish(key, modelSupplier, location, layerDefinition, rendererSupplier);
    }

    public static void registerCustomFish(String key, Function<EntityRendererProvider.Context, FishModel> modelSupplier,
                                          ModelLayerLocation location, Supplier<LayerDefinition> layerDefinition,
                                          TriFunction<String, FishModel, EntityRendererProvider.Context, FishRenderer<?>> rendererSupplier) {
        registerFishRenderer(key, ctx -> rendererSupplier.apply(key, modelSupplier.apply(ctx), ctx));
        registerLayerDefinition(location, layerDefinition);
    }

    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity> void registerFishRenderer(String key, EntityRendererProvider<T> renderer) {
        EntityType<?> entityType = TideEntityTypes.ENTITY_TYPES.get(key);
        if (entityType == null) {
            MoreTideFish.LOGGER.error("Failed to register moretidefish renderer for unknown entity type: '{}'", key);
            return;
        }
        try {
            registerEntityRenderer((EntityType<T>) entityType, renderer);
        } catch (Exception e) {
            MoreTideFish.LOGGER.error("Failed to register moretidefish renderer for incompatible entity type: '{}'", key);
        }
    }

    public static <T extends Entity> void registerEntityRenderer(EntityType<T> entityType, EntityRendererProvider<T> renderer) {
        RENDERERS.add(new MTFModels.RendererRegistration<>(entityType, renderer));
    }

    public static void registerLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> layerDefinition) {
        LAYER_DEFS.putIfAbsent(location, layerDefinition);
    }
}
