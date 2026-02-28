package com.xfw.moretidefish.registries;

import com.xfw.moretidefish.MoreTideFish;
import com.xfw.moretidefish.registries.effects.ElectrifiedEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MTFMobEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, MoreTideFish.MODID);

    //ElectrifiedEffect
    public static final DeferredHolder<MobEffect, MobEffect> ELECTRIFIED = MOB_EFFECTS.register("electrified", () -> new ElectrifiedEffect(MobEffectCategory.HARMFUL, 0x25b8b0));

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}
