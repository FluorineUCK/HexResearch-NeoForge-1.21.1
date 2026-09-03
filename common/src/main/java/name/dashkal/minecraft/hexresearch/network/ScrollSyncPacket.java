package name.dashkal.minecraft.hexresearch.network;

import name.dashkal.minecraft.hexresearch.HexResearch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ScrollSyncPacket implements CustomPacketPayload {
    public static final ResourceLocation PACKET_ID = HexResearch.id("scroll_sync");
    public static final Type<ScrollSyncPacket> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ScrollSyncPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public @NotNull ScrollSyncPacket decode(RegistryFriendlyByteBuf buffer) {
                return new ScrollSyncPacket(
                    buffer.readInt(),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)
                );
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ScrollSyncPacket payload) {
                buffer.writeInt(payload.entityId);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, payload.newScroll);
            }
        };

    private final int entityId;
    private final ItemStack newScroll;

    public ScrollSyncPacket(int entityId, ItemStack newScroll) {
        this.entityId = entityId;
        this.newScroll = newScroll;
    }

    public int getEntityId() {
        return entityId;
    }

    public ItemStack getNewScroll() {
        return newScroll;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
