package com.xfw.moretidefish.registries.models;

import com.li64.tide.Tide;
import com.xfw.moretidefish.registries.item.LanternFishItem;
import com.xfw.moretidefish.MoreTideFish;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class LanternFishHelmetModel extends GeoModel<LanternFishItem> {
    public LanternFishHelmetModel() {
        super();
    }

    @Override
    public ResourceLocation getModelResource(LanternFishItem object) {
        return ResourceLocation.fromNamespaceAndPath(MoreTideFish.MODID, "geo/moretidefish_lantern_fish_helmet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LanternFishItem object) {
        return ResourceLocation.fromNamespaceAndPath(Tide.MOD_ID, "textures/entity/fish/moretidefish_lantern_fish_helmet.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LanternFishItem lanternFish) {
        return null;
    }
}
