package com.xfw.moretidefish.registries;

import com.xfw.moretidefish.MoreTideFish;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MTFSoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS= DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MoreTideFish.MODID);;

    public static final Supplier<SoundEvent> ELECTRIC_EEL_RELEASE= registerSound("electric_eel_release");
    public static final Supplier<SoundEvent> ELECTRIC_EEL_WHIP= registerSound("electric_eel_whip");

    public MTFSoundRegistry(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

    private static Supplier<SoundEvent> registerSound(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(MoreTideFish.Resource( id)));
    }


    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
