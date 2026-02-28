package com.xfw.moretidefish.registries;

import com.mojang.serialization.MapCodec;
import com.xfw.moretidefish.MoreTideFish;
import com.xfw.moretidefish.registries.particles.ZapParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MTFParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPE = DeferredRegister.create(Registries.PARTICLE_TYPE, MoreTideFish.MODID);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AIR_BUBBLE = PARTICLE_TYPE.register("air_bubble", () -> new SimpleParticleType(false));

    public static final Supplier<ParticleType<ZapParticleOption>> ZAP_PARTICLE = PARTICLE_TYPE.register("zap", () -> new ParticleType<>(false) {
        public MapCodec<ZapParticleOption> codec() {
            return ZapParticleOption.MAP_CODEC;
        }

        public StreamCodec<? super RegistryFriendlyByteBuf, ZapParticleOption> streamCodec() {
            return ZapParticleOption.STREAM_CODEC;
        }
    });

    public static final Supplier<SimpleParticleType> SPARK_PARTICLE = PARTICLE_TYPE.register("spark_particle",
            () -> new SimpleParticleType(false));

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPE.register(modEventBus);
    }
}
