package com.xfw.moretidefish.mixin;

import com.li64.tide.registries.TideFish;
import com.xfw.moretidefish.registries.MTFRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(TideFish.class)
public abstract class TideFishMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onTideFishStaticInit(CallbackInfo ci) {
        MTFRegistry.init();
    }
}