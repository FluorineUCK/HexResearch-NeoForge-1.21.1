package name.dashkal.minecraft.hexresearch.network;

import at.petrak.hexcasting.common.entities.EntityWallScroll;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import name.dashkal.minecraft.hexresearch.xplat.XPlatAPI;

public class Networking {
    /**
     * Send a packet to clients updating the drawn pattern on a scroll.
     * @param entity the hanging scroll to update
     * @param level the world the scroll is in
     */
    public static void sendScrollSync(EntityWallScroll entity, ServerLevel level) {
        ScrollSyncPacket packet = new ScrollSyncPacket(entity.getId(), entity.scroll);

        for (ServerPlayer player : level.players()) {
            sendToPlayer(player, packet);
        }
    }

    /**
     * Sends a packet to clients telling them to draw particles for a Cognitive Inducer's mind impression.
     * @param level the world the impression took place in
     * @param source the entity that was impressed
     * @param inducerPos the position of the inducer
     * @param successful if the impression was successful or not (for particle color selection)
     */
    public static void sendMindImpression(ServerLevel level, Entity source, BlockPos inducerPos, boolean successful) {
        MindImpressionPacket packet = new MindImpressionPacket(level.dimension().location(), source.getId(), inducerPos, successful);

        for (ServerPlayer player : level.players()) {
            sendToPlayer(player, packet);
        }
    }

    private static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        XPlatAPI.getInstance().sendPacketToPlayer(player, packet);
    }
}
