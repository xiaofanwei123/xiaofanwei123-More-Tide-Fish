package com.xfw.moretidefish.networking;

import com.li64.tide.Tide;
import com.li64.tide.data.commands.TestType;
import com.li64.tide.data.fishing.selector.FishingEntry;
import com.li64.tide.registries.entities.misc.fishing.HookAccessor;
import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import com.xfw.moretidefish.MoreTideFish;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record FishFinderPayload(int playerId) implements CustomPacketPayload {

    public static final Type<FishFinderPayload> TYPE = new Type<>(MoreTideFish.Resource("fish_finder_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FishFinderPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeInt(payload.playerId()),
                    (buf) -> new FishFinderPayload(buf.readInt())
            );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FishFinderPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = (Player)context.player().level().getEntity(payload.playerId());

            if(player == null) return;
            TideFishingHook hook = HookAccessor.getHook(player);

            if (hook == null) {
                player.sendSystemMessage(Component.translatable("commands.fishing.no_fish_finder").withStyle(ChatFormatting.RED));
                return;
            }

            Map<FishingEntry, Double> results = Tide.FISHING_MANAGER.test(hook.getContext(), TestType.FISH);
            if (results == null || results.isEmpty()) {
                player.sendSystemMessage(Component.translatable("commands.fishing.nothing_found").withStyle(ChatFormatting.RED));
                return;
            }

            double total = results.values().stream().mapToDouble(d -> d).sum();
            List<Map.Entry<FishingEntry, Double>> sortedResults = results.entrySet().stream()
                    .sorted(Comparator.comparing(entry -> -entry.getValue())).toList();

            MutableComponent formattedResults = Component.translatable("commands.fishing.results")
                    .withStyle(ChatFormatting.GOLD);
            sortedResults.forEach(entry -> {
                formattedResults.append(Component.literal("\n"));
                formattedResults.append(entry.getKey().getTestKey().withStyle(ChatFormatting.WHITE));
                formattedResults.append(Component.literal(" - ").withStyle(ChatFormatting.WHITE));
                String percent = String.format("%.1f", (entry.getValue() / total) * 100.0) + "%";
                formattedResults.append(Component.literal(percent).withStyle(ChatFormatting.AQUA));
            });

            player.sendSystemMessage(formattedResults);
        });
    }
}
