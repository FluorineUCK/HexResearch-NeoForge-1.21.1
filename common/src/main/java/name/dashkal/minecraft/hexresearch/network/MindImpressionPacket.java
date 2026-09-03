package name.dashkal.minecraft.hexresearch.network;

import name.dashkal.minecraft.hexresearch.HexResearch;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MindImpressionPacket implements CustomPacketPayload {
    public static final ResourceLocation PACKET_ID = HexResearch.id("mind_impression");
    public static final Type<MindImpressionPacket> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, MindImpressionPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public @NotNull MindImpressionPacket decode(RegistryFriendlyByteBuf buffer) {
                return new MindImpressionPacket(
                    buffer.readResourceLocation(),
                    buffer.readInt(),
                    buffer.readBlockPos(),
                    buffer.readBoolean()
                );
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, MindImpressionPacket payload) {
                buffer.writeResourceLocation(payload.dimensionId);
                buffer.writeInt(payload.entityId);
                buffer.writeBlockPos(payload.inducerPos);
                buffer.writeBoolean(payload.successful);
            }
        };

    private final ResourceLocation dimensionId;
    private final int entityId;
    private final BlockPos inducerPos;
    private final boolean successful;

    public MindImpressionPacket(ResourceLocation dimensionId, int entityId, BlockPos inducerPos, boolean successful) {
        this.dimensionId = dimensionId;
        this.entityId = entityId;
        this.inducerPos = inducerPos;
        this.successful = successful;
    }

    public ResourceLocation getDimensionId() {
        return dimensionId;
    }

    public int getEntityId() {
        return entityId;
    }

    public BlockPos getInducerPos() {
        return inducerPos;
    }

    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
