package name.dashkal.minecraft.hexresearch.forge.network;

import name.dashkal.minecraft.hexresearch.forge.client.network.HexResearchClientPayloadHandler;
import name.dashkal.minecraft.hexresearch.network.MindImpressionPacket;
import name.dashkal.minecraft.hexresearch.network.ScrollSyncPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** NeoForge payload registration for Hex Research. */
public final class HexResearchPayloadHandler {
    private HexResearchPayloadHandler() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(HexResearchPayloadHandler::register);
    }

    private static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
            ScrollSyncPacket.TYPE,
            ScrollSyncPacket.STREAM_CODEC,
            makeClientBoundHandler()
        );
        registrar.playToClient(
            MindImpressionPacket.TYPE,
            MindImpressionPacket.STREAM_CODEC,
            makeClientBoundHandler()
        );
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> makeClientBoundHandler() {
        return (payload, context) ->
            context.enqueueWork(() -> HexResearchClientPayloadHandler.handle(payload));
    }
}
