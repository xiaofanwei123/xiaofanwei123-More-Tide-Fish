package com.xfw.moretidefish.registries.renderers;

import com.li64.tide.Tide;
import com.li64.tide.registries.entities.models.FishModel;
import com.li64.tide.registries.entities.renderers.FishRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.xfw.moretidefish.registries.entities.PufferfishBomb;
import com.xfw.moretidefish.registries.models.PufferfishBombBigModel;
import com.xfw.moretidefish.registries.models.PufferfishBombMidModel;
import com.xfw.moretidefish.registries.models.PufferfishBombSmallModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

public class PufferfishBombEntityRender extends FishRenderer<PufferfishBombBigModel> {

    private static final ResourceLocation DEFAULT_TEXTURE = Tide.resource("textures/entity/fish/moretidefish_pufferfish_bomb.png");
    private int puffStateO = 3;
    private final FishModel small; ;
    private final FishModel mid;
    private final FishModel big= this.getModel();

    public PufferfishBombEntityRender(String key, FishModel model, EntityRendererProvider.Context context) {
        super(key, new PufferfishBombBigModel(context), context);
        this.small = new PufferfishBombSmallModel(context);
        this.mid = new PufferfishBombMidModel(context);
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return DEFAULT_TEXTURE;
    }

    @Override
    public void render(Mob entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        PufferfishBomb bomb = (PufferfishBomb) entity;

        int puffState = bomb.getPuffState();
        if (puffState != this.puffStateO) {
            if (puffState == 0) {
                this.model = this.small;
            } else if (puffState == 1) {
                this.model = this.mid;
            } else {
                this.model = this.big;
            }
        }
        this.puffStateO = puffState;
        this.shadowRadius = 0.1F + 0.1F * (float) puffState;

        if (puffState == 2) {
            int fuse = bomb.getFuse();
            float fuseTime = (float) fuse - partialTicks;

            if (fuseTime + 1.0F < 15.0F) {
                float f = 1.0F - (fuseTime + 1.0F) / 10.0F;
                f = Mth.clamp(f, 0.0F, 1.0F);
                f = f * f * f * f; //f^4 曲线
                float scale = 1.0F + f * 0.3F;
                poseStack.scale(scale, scale, scale);
            }
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected float getWhiteOverlayProgress(Mob entity, float partialTicks) {
        PufferfishBomb bomb = (PufferfishBomb) entity;

        if (bomb.getPuffState() == 2) {
            int fuse = bomb.getFuse();
            if (fuse % 5 == 0) {
                return 1.0F;
            }
        }
        return 0.0F;
    }
}