package com.xfw.moretidefish.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.xfw.moretidefish.registries.MTFDataAttachmentsRegistry;
import com.xfw.moretidefish.registries.entities.ElectricEelWhip;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @WrapOperation(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"))
    private void renderStatic(ItemRenderer instance, ItemStack itemStack, ItemDisplayContext context, boolean posestack$pose, PoseStack poseStack, MultiBufferSource vertexconsumer, int light, int combinedOverlay, BakedModel model, Operation<Void> original, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) boolean leftHand) {
        if (entity instanceof Player player && !leftHand) {
            //右手使用鞭子时取消渲染
            if(player.level().getEntity(player.getData(MTFDataAttachmentsRegistry.WHIP)) instanceof ElectricEelWhip){
                return;
            }
        }
        original.call(instance, itemStack, context, posestack$pose, poseStack, vertexconsumer, light, combinedOverlay, model);
    }

}
