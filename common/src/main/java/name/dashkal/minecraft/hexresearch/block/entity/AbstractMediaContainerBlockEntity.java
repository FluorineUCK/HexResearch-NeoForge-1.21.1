package name.dashkal.minecraft.hexresearch.block.entity;

import name.dashkal.minecraft.hexresearch.util.MediaContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Base class for any Hex Research block entity that needs to consume media.
 * <p>
 * When using this, be sure to also register the appropriate forge capability in the forge project.
 */
public abstract class AbstractMediaContainerBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final String TAG_CURRENT_MEDIA = "current_media";
    public static final String TAG_MAXIMUM_MEDIA = "max_media";
    public static final String TAG_INFINITE_MEDIA = "infinite_media";

    protected final MediaContainer mediaContainer;

    public AbstractMediaContainerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.mediaContainer = new MediaContainer();
    }

    public AbstractMediaContainerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, int initialMedia, int maxMedia) {
        super(blockEntityType, blockPos, blockState);
        this.mediaContainer = new MediaContainer(initialMedia, maxMedia);
    }

    /**
     * Called when the media container changes via this class's delegate methods.
     * <p>
     * Default implementation is to invoke {@link BlockEntity#setChanged()}.
     * </p>
     */
    protected void onMediaContainerUpdated() {
        setChanged();
    };

    /** Returns the current stored media amount. */
    public int getCurrentMedia() {
        return mediaContainer.getCurrentMedia();
    }

    /** Returns the maximum amount of media this container may store. */
    public int getMaxMedia() {
        return mediaContainer.getMaxMedia();
    }

    /** Returns <code>true</code> if this media container should be considered an infinite source. */
    public boolean isInfinite() {
        return mediaContainer.isInfinite();
    }

    // Data persistence

    @Override
    protected void loadAdditional(@Nonnull CompoundTag compoundTag, @Nonnull HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        mediaContainer.setCurrentMedia(compoundTag.getInt(TAG_CURRENT_MEDIA));
        mediaContainer.setInfinite(compoundTag.getBoolean(TAG_INFINITE_MEDIA));

        if (compoundTag.contains(TAG_MAXIMUM_MEDIA)) {
            mediaContainer.setMaxMedia(compoundTag.getInt(TAG_MAXIMUM_MEDIA));
        } else {
            mediaContainer.setMaxMedia(Integer.MAX_VALUE);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.putInt(TAG_CURRENT_MEDIA, mediaContainer.getCurrentMedia());
        compoundTag.putInt(TAG_MAXIMUM_MEDIA, mediaContainer.getMaxMedia());
        compoundTag.putBoolean(TAG_INFINITE_MEDIA, mediaContainer.isInfinite());
        super.saveAdditional(compoundTag, provider);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }

    // MediaContainer delegates

    public boolean insertMedia(@Nonnull ItemStack itemStack, boolean simulate) {
        boolean r = mediaContainer.insertMedia(itemStack, simulate);
        if (!simulate) {
            onMediaContainerUpdated();
        }
        return r;
    }

    public int extractMediaFromItem(@Nonnull ItemStack itemStack, boolean simulate) {
        return mediaContainer.extractMediaFromItem(itemStack, simulate);
    }

    @Override
    @Nonnull
    public int[] getSlotsForFace(@Nonnull Direction direction) {
        return mediaContainer.getSlotsForFace(direction);
    }

    @Override
    public boolean canPlaceItem(int i, @Nonnull ItemStack itemStack) {
        return mediaContainer.canPlaceItem(i, itemStack);
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, @Nonnull ItemStack itemStack, @Nullable Direction direction) {
        return mediaContainer.canPlaceItemThroughFace(i, itemStack, direction);
    }

    @Override
    public boolean canTakeItemThroughFace(int i, @Nonnull ItemStack itemStack, @Nonnull Direction direction) {
        return mediaContainer.canTakeItemThroughFace(i, itemStack, direction);
    }

    @Override
    public int getContainerSize() {
        return mediaContainer.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return mediaContainer.isEmpty();
    }

    @Override
    @Nonnull
    public ItemStack getItem(int i) {
        return mediaContainer.getItem(i);
    }

    @Override
    @Nonnull
    public ItemStack removeItem(int i, int j) {
        return mediaContainer.removeItem(i, j);
    }

    @Override
    @Nonnull
    public ItemStack removeItemNoUpdate(int i) {
        return mediaContainer.removeItemNoUpdate(i);
    }

    @Override
    public void setItem(int i, @Nonnull ItemStack itemStack) {
        mediaContainer.setItem(i, itemStack);
        onMediaContainerUpdated();
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return mediaContainer.stillValid(player);
    }

    @Override
    public void clearContent() {
        mediaContainer.clearContent();
        onMediaContainerUpdated();
    }
}
