package com.xfw.moretidefish.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import com.xfw.moretidefish.registries.item.LanternFishItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "dev.lambdaurora.lambdynlights.api.DynamicLightHandlers",remap = false)
public class DynamicLightHandlersMixin {
    @ModifyReturnValue(method = "getLuminanceFrom(Lnet/minecraft/world/entity/Entity;)I", at = @At("RETURN"))
    private static <T extends Entity> int modifyEntityLuminance(int original, T entity) {
        if (entity instanceof Player player) {//检查玩家是否戴了LanternFish头盔
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!helmet.isEmpty() && helmet.getItem() instanceof LanternFishItem) {
                return Math.max(original, player.isUnderWater()? 15:10);
            }
        }
        return original;
    }

}
