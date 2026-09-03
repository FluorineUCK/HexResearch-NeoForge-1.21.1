package name.dashkal.minecraft.hexresearch.forge.client.network;

import at.petrak.hexcasting.common.entities.EntityWallScroll;
import name.dashkal.minecraft.hexresearch.client.block.entity.CognitiveInducerClient;
import name.dashkal.minecraft.hexresearch.network.MindImpressionPacket;
import name.dashkal.minecraft.hexresearch.network.ScrollSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;

/** Client-only effects for payloads registered by the NeoForge network layer. */
public final class HexResearchClientPayloadHandler {
    private HexResearchClientPayloadHandler() {
    }

    public static void handle(CustomPacketPayload payload) {
        if (payload instanceof ScrollSyncPacket scrollSync) {
            handleScrollSync(scrollSync);
        } else if (payload instanceof MindImpressionPacket mindImpression) {
            CognitiveInducerClient.handleParticlePacket(
                mindImpression.getDimensionId(),
                mindImpression.getEntityId(),
                mindImpression.getInducerPos(),
                mindImpression.isSuccessful()
            );
        }
    }

    private static void handleScrollSync(ScrollSyncPacket payload) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }

        Entity entity = client.level.getEntity(payload.getEntityId());
        if (entity instanceof EntityWallScroll wallScroll) {
            wallScroll.scroll = payload.getNewScroll();
            wallScroll.recalculateDisplay();
        }
    }
}
