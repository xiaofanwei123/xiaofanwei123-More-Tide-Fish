package com.xfw.moretidefish.registries.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xfw.moretidefish.registries.entities.PinkStarfishItemEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public class PinkStarfishItemRenderer extends EntityRenderer<PinkStarfishItemEntity> {

    public PinkStarfishItemRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(PinkStarfishItemEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Vec3 v = entity.getDeltaMovement();
        float yaw = (float) Math.atan2(v.z, v.x);
        poseStack.mulPose(Axis.YN.rotation(yaw));
        float pitch = (float) Math.atan2(v.y, Math.sqrt(v.x * v.x + v.z * v.z));
        poseStack.mulPose(Axis.ZN.rotation(-pitch));

        if (!entity.inGround()) {
            poseStack.mulPose(Axis.XN.rotationDegrees((float) (90 - 20 * Math.cos((entity.tickCount + entity.randomRotation) / 10.0))));
            poseStack.mulPose(Axis.ZN.rotation(entity.tickCount + partialTick));
        } else {
            poseStack.mulPose(Axis.XN.rotationDegrees(entity.getLastFlightXRot()));
            poseStack.mulPose(Axis.ZN.rotation(entity.getLastFlightZRot()));
        }
        float scale = 0.5f;
        poseStack.scale(scale, scale, scale);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                entity.weapon,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                0
        );
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(PinkStarfishItemEntity baseArrowEntity) {
        return null;
    }

}