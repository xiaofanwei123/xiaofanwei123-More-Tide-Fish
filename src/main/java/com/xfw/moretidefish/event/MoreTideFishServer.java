package com.xfw.moretidefish.event;


import com.xfw.moretidefish.MoreTideFish;
import com.xfw.moretidefish.networking.FishFinderPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MoreTideFish.MODID)
public class MoreTideFishServer {

    @SubscribeEvent
    public static void setupPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(FishFinderPayload.TYPE, FishFinderPayload.STREAM_CODEC, FishFinderPayload::handle);
    }

}
