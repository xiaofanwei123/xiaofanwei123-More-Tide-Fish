package com.xfw.moretidefish.registries.renderers;

import com.xfw.moretidefish.registries.item.LanternFishItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.Optional;

public class LanternFishHelmetRender extends GeoArmorRenderer<LanternFishItem> {
    private GeoBone helmetBone = null;

    public LanternFishHelmetRender(GeoModel<LanternFishItem> model) {
        super(model);
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    protected void grabRelevantBones(BakedGeoModel bakedModel) {
        super.grabRelevantBones(bakedModel);
        Optional<GeoBone> bone = model.getBone("bone");
        bone.ifPresent(geoBone -> this.helmetBone = geoBone);
    }

    @Override
    protected void applyBaseTransformations(HumanoidModel<?> baseModel) {
        super.applyBaseTransformations(baseModel);
        if (this.helmetBone != null) {
            RenderUtil.matchModelPartRot(baseModel.head, this.helmetBone);
        }
    }

    @Override
    public void applyBoneVisibilityByPart(EquipmentSlot currentSlot, ModelPart currentPart, HumanoidModel<?> model) {
        super.applyBoneVisibilityByPart(currentSlot, currentPart, model);
        if (currentPart == model.head && currentSlot == EquipmentSlot.HEAD) {
            setBoneVisible(this.helmetBone, true);
        }
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);
        setBoneVisible(this.helmetBone, visible);
    }

}