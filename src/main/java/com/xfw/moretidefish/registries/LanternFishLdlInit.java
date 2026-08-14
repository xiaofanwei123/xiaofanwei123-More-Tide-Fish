package com.xfw.moretidefish.registries;

import com.xfw.moretidefish.MoreTideFish;
import com.xfw.moretidefish.registries.item.LanternFishItem;
import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Range;

public class LanternFishLdlInit implements DynamicLightsInitializer {
    private static final LanternFishLuminance INSTANCE = new LanternFishLuminance();
    private static final EntityLuminance.Type TYPE =
            EntityLuminance.Type.registerSimple(MoreTideFish.Resource("lantern_fish_luminance"), INSTANCE);

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        context.entityLightSourceManager().onRegisterEvent()
                .register(ctx -> ctx.register(EntityType.PLAYER, INSTANCE));
    }

    @Override
    public void onInitializeDynamicLights(ItemLightSourceManager itemLightSourceManager) {
    }

    private static class LanternFishLuminance implements EntityLuminance {
        @Override
        public Type type() {
            return TYPE;
        }

        @Override
        public @Range(from = 0L, to = 15L) int getLuminance(ItemLightSourceManager manager, Entity entity) {
            if (entity instanceof Player player && player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof LanternFishItem) {
                return player.isUnderWater() ? 15 : 10;
            }
            return 0;
        }
    }
}