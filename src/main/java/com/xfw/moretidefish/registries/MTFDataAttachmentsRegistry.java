package com.xfw.moretidefish.registries;

import com.mojang.serialization.Codec;
import com.xfw.moretidefish.MoreTideFish;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class MTFDataAttachmentsRegistry {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES= DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MoreTideFish.MODID);
    public static final Supplier<AttachmentType<Integer>> WHIP = ATTACHMENT_TYPES.register("whip",                 () -> AttachmentType.<Integer>builder(() -> 0)
            .serialize(Codec.INT)
            .copyOnDeath()
            .build()
    );


    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
