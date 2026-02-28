package com.xfw.moretidefish.registries.renderers;

import com.li64.tide.Tide;
import com.li64.tide.registries.entities.renderers.FishRenderer;

import com.xfw.moretidefish.registries.entities.PinkStarfish;
import com.xfw.moretidefish.registries.models.PinkStarfishModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class PinkStarfishRenderer extends FishRenderer<PinkStarfishModel> {
    //默认纹理路径
    private static final ResourceLocation DEFAULT_TEXTURE = Tide.resource("textures/entity/fish/moretidefish_ninjia_starfish.png");

    //patrick_star
    private static final ResourceLocation PATRICK_STARK = Tide.resource("textures/entity/fish/patrick_star.png");

    public PinkStarfishRenderer(String key, PinkStarfishModel model, EntityRendererProvider.Context context) {
        super(key, model, context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Mob entity) {
        if (entity instanceof PinkStarfish starfish) {
            String name = starfish.getCustomName() != null ? starfish.getCustomName().getString().toLowerCase().trim() : "";
            if (name.equals("patrick star")) {
                return PATRICK_STARK;
            }
        }
        return this.getTextureLocation();
    }
}