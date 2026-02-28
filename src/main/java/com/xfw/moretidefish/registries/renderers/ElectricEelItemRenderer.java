package com.xfw.moretidefish.registries.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.xfw.moretidefish.MoreTideFish;
import com.xfw.moretidefish.registries.entities.ElectricEelWhip;
import com.xfw.moretidefish.registries.models.ElectricEelItemModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.Optional;

public class ElectricEelItemRenderer extends EntityRenderer<ElectricEelWhip> {
    private static final ResourceLocation ENTITY_TEXTURE = MoreTideFish.Resource("textures/entity/tentacle_spike.png");
    private static final ResourceLocation GLOW_TEXTURE = MoreTideFish.Resource("textures/entity/tentacle_spike_glow.png");
    private final ElectricEelItemModel<ElectricEelWhip> model;


    public ElectricEelItemRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new ElectricEelItemModel<>(context.bakeLayer(ElectricEelItemModel.LAYER_LOCATION));

    }

    @Override
    public void render(ElectricEelWhip entity, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
        stack.pushPose();
        translateAndRotate(entity, partialTicks, stack, false);
        renderWhip(entity, partialTicks, stack, buffer, light);
        stack.popPose();
        super.render(entity, yaw, partialTicks, stack, buffer, light);
    }

    public void translateAndRotate(ElectricEelWhip entity, float partialTicks, PoseStack stack, boolean inverted) {
        Player player = entity.getPlayerOwner();
        if (player != null) {
            Vec3 handPos = getPlayerHandPos(player, partialTicks);
            Vec3 pos = new Vec3(
                    Mth.lerp(partialTicks, entity.xo, entity.getX()),
                    Mth.lerp(partialTicks, entity.yo, entity.getY()),
                    Mth.lerp(partialTicks, entity.zo, entity.getZ())
            );
            stack.translate(handPos.x - pos.x, handPos.y - pos.y, handPos.z - pos.z);
            stack.mulPose(new Quaternionf().rotationX(90 * Mth.DEG_TO_RAD));
            if (inverted) {
                stack.mulPose(new Quaternionf().rotationZ((Mth.lerp(partialTicks, player.yHeadRotO, player.yHeadRot) + 180) * Mth.DEG_TO_RAD));
                stack.mulPose(new Quaternionf().rotationX(-player.getViewXRot(partialTicks) * Mth.DEG_TO_RAD));
            } else {
                stack.mulPose(new Quaternionf().rotationZ(Mth.lerp(partialTicks, player.yHeadRotO, player.yHeadRot) * Mth.DEG_TO_RAD));
                stack.mulPose(new Quaternionf().rotationX(player.getViewXRot(partialTicks) * Mth.DEG_TO_RAD));
            }
        }
    }

    public void renderWhip(ElectricEelWhip entity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
        stack.pushPose();
        float bob = entity.tickCount + partialTicks;

        stack.scale(-1.0F, -1.0F, 1.0F);
        stack.translate(0.0F, -1.5F, 0.0F);

        this.model.setupAnim(entity, 0, 0, bob, 0, 0);
        RenderType renderType = this.model.renderType(getTextureLocation(entity));
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer, renderType, false, entity.isFoil());
        this.model.renderToBuffer(stack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
        renderType = RenderType.eyes(GLOW_TEXTURE);
        vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer, renderType, false, entity.isFoil());
        this.model.renderToBuffer(stack, vertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        //鞭子的粒子
        int t = entity.getSpawnedTicks();
        int lifespan = entity.getLifespan();
        float mid = lifespan / 2.0f;
        if (t >= mid) {
            float distance = (t - mid) / (lifespan - mid);
            float probability = (float) Math.cos(distance * Math.PI / 2);

            if (entity.getRandom().nextFloat() < probability) {
                //随即决定非0的体节
                int segmentIndex = entity.getRandom().nextInt(14) + 1;
                Vec3 pos = getWhipSegmentPosition(entity, partialTicks, segmentIndex);
                //位置传给实体
                entity.pendingParticlePositions.offer(new Pair<>(segmentIndex, pos));
            }
        }

        stack.popPose();
    }

    private Vec3 getPlayerHandPos(Player player, float partialTicks) {
        int arm = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
            double fovFactor = 960.0 / (double) this.entityRenderDispatcher.options.fov().get();
            Vec3 vec3 = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float) arm * 1.25f, -1.25f).scale(fovFactor);
            return player.getEyePosition(partialTicks).add(vec3);
        } else {
            Optional<Vec3> handPos = getThirdPersonPlayerHandPosition(player, entityRenderDispatcher, Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot), partialTicks, player.getMainArm(), new Vec3(0, 0.6, 0));
            if (handPos.isPresent()) {
                return handPos.get();
            }
            float yaw = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot) * (float) (Math.PI / 180.0);
            double sin = Mth.sin(yaw);
            double cos = Mth.cos(yaw);
            float playerScale = player.getScale();
            double sideOffset = arm * 0.35 * (double) playerScale;
            double forwardOffset = 0.35 * (double) playerScale;
            float crouchingFactor = player.isCrouching() ? -0.1875F : 0.0F;
            return player.getEyePosition(partialTicks).add(-cos * sideOffset - sin * forwardOffset, (double) crouchingFactor - 0.85 * (double) playerScale, -sin * sideOffset + cos * forwardOffset);
        }
    }


    @Override
    public ResourceLocation getTextureLocation(ElectricEelWhip entity) {
        return ENTITY_TEXTURE;
    }

    private static final MultiBufferSource DUMMY_BUFFER = new MultiBufferSource() {
        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new VertexConsumer() {
                @Override
                public VertexConsumer addVertex(float x, float y, float z) {
                    return this;
                }

                @Override
                public VertexConsumer setColor(int red, int green, int blue, int alpha) {
                    return this;
                }

                @Override
                public VertexConsumer setUv(float u, float v) {
                    return this;
                }

                @Override
                public VertexConsumer setUv1(int u, int v) {
                    return this;
                }

                @Override
                public VertexConsumer setUv2(int u, int v) {
                    return this;
                }

                @Override
                public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
                    return this;
                }
            };
        }
    };

    //
    private Vec3 getWhipSegmentPosition(ElectricEelWhip entity, float partialTicks, int segmentIndex) {
        PoseStack stack = new PoseStack();
        stack.translate(
                Mth.lerp(partialTicks, entity.xo, entity.getX()),
                Mth.lerp(partialTicks, entity.yo, entity.getY()),
                Mth.lerp(partialTicks, entity.zo, entity.getZ())
        );
        translateAndRotate(entity, partialTicks, stack, true);
        this.model.translateToSegment(stack, segmentIndex);
        Vector4f vec = new Vector4f(0, 0, 0, 1).mul(stack.last().pose());
        return new Vec3(vec.x(), vec.y(), vec.z());
    }
    //

    public static Optional<Vec3> getThirdPersonPlayerHandPosition(Player player, EntityRenderDispatcher renderDispatcher, float yaw, float partialTick, HumanoidArm arm, Vec3 offset) {
        if (player instanceof AbstractClientPlayer clientPlayer && renderDispatcher.getRenderer(clientPlayer) instanceof PlayerRenderer renderer) {
            PoseStack stack = new PoseStack();
            renderer.render(clientPlayer, yaw, partialTick, stack, DUMMY_BUFFER, LightTexture.FULL_BRIGHT);
            PlayerModel<AbstractClientPlayer> model = renderer.getModel();

            stack.translate(
                    Mth.lerp(partialTick, player.xo, player.getX()),
                    Mth.lerp(partialTick, player.yo, player.getY()),
                    Mth.lerp(partialTick, player.zo, player.getZ())
            );
            stack.mulPose(new Quaternionf().rotationY((-yaw + 180.0F) * Mth.DEG_TO_RAD));
            stack.scale(-1, -1, 1);
            stack.scale(0.9375F, 0.9375F, 0.9375F);
            //renderer.scale(clientPlayer, stack, partialTick);
            stack.translate(0, -1.5f, 0);
            model.translateToHand(arm, stack);

            Vector4f vec = new Vector4f((float) offset.x(), (float) offset.y(), (float) offset.z(), 1).mul(stack.last().pose());
            Vec3 pos = new Vec3(vec.x(), vec.y(), vec.z());
            Vec3 subtract = pos.subtract(player.position());
            return Optional.of(player.position().add(subtract.scale(player.getScale())));
        }
        return Optional.empty();
    }
}