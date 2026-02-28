package com.xfw.moretidefish;

import com.mojang.logging.LogUtils;
import com.xfw.moretidefish.misc.ModArmorMaterial;
import com.xfw.moretidefish.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(MoreTideFish.MODID)
public class MoreTideFish {
    public static final String MODID = "moretidefish";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MoreTideFish(IEventBus modEventBus, ModContainer modContainer) {
        ModArmorMaterial.register(modEventBus);
        MTFDataComponents.register(modEventBus);
        MTFItemRegistry.register(modEventBus);
        MTFEntityRegistry.register(modEventBus);
        MTFMobEffectRegistry.register(modEventBus);
        MTFParticleRegistry.register(modEventBus);
        MTFDataAttachmentsRegistry.register(modEventBus);
        MTFSoundRegistry.register(modEventBus);
    }


    public static ResourceLocation Resource(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(MoreTideFish.MODID, path);
    }
}
