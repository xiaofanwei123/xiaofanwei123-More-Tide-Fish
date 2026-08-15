package com.xfw.moretidefish.event;


import com.li64.tide.data.TideTags;
import com.xfw.moretidefish.MoreTideFish;
import com.xfw.moretidefish.networking.FishFinderPayload;
import com.xfw.moretidefish.registries.MTFItemRegistry;
import com.xfw.moretidefish.registries.MTFModels;
import com.xfw.moretidefish.registries.MTFParticleRegistry;
import com.xfw.moretidefish.registries.particles.AirBubbleParticle;
import com.xfw.moretidefish.registries.particles.SparkParticle;
import com.xfw.moretidefish.registries.particles.ZapParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;


@EventBusSubscriber(modid = MoreTideFish.MODID, value = Dist.CLIENT)
public class MTFClient {
    @SubscribeEvent
    public static void registerEntityModels(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        if (MTFModels.LAYER_DEFS.isEmpty()) MTFModels.init();
        MTFModels.LAYER_DEFS.forEach(event::registerLayerDefinition);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        if (MTFModels.RENDERERS.isEmpty()) MTFModels.init();
        MTFModels.RENDERERS.forEach(reg -> registerEntityRenderer(reg, event));
    }

    public static <T extends Entity> void registerEntityRenderer(MTFModels.RendererRegistration<T> reg, EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(reg.entityType(), reg.renderer());
    }

    @SubscribeEvent
    public static void setupParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MTFParticleRegistry.AIR_BUBBLE.get(), AirBubbleParticle.Provider::new);
        event.registerSpriteSet(MTFParticleRegistry.ZAP_PARTICLE.get(), ZapParticle.Provider::new);
        event.registerSpriteSet(MTFParticleRegistry.SPARK_PARTICLE.get(), SparkParticle.Provider::new);

    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null && mc.screen == null && mc.options.keyShift.matches(event.getKey(), event.getScanCode()) &&
                (
                    (player.getOffhandItem().is(MTFItemRegistry.ADVANCED_FISH_FINDER) && player.getMainHandItem().is(TideTags.Items.FISHING_RODS))
                    ||
                    (player.getMainHandItem().is(MTFItemRegistry.ADVANCED_FISH_FINDER) && player.getOffhandItem().is(TideTags.Items.FISHING_RODS))
                )
        )
        {
            PacketDistributor.sendToServer(new FishFinderPayload(player.getId()));
        }
    }

}
