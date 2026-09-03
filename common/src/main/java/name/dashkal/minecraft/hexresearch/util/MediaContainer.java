package name.dashkal.minecraft.hexresearch.util;

import at.petrak.hexcasting.api.utils.MediaHelper;
import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Implementation of `WorldlyContainer` for handling media.
 */
public class MediaContainer implements WorldlyContainer {
    private static final int[] SLOTS = { 0 };

    private int currentMedia;
    private int maxMedia;
    private boolean infinite = false;

    /** Create a new empty media container with no limit. */
    public MediaContainer() {
        this(0, Integer.MAX_VALUE);
    }

    /**
     * Create a new media container.
     * <p>
     * Note: maxMedia may be less than initialMedia, indicating the container is "overcharged".
     *
     * @param initialMedia the initial amount of media to hold
     * @param maxMedia the maximum media this container may hold
     */
    public MediaContainer(int initialMedia, int maxMedia) {
        this.maxMedia = maxMedia;
        this.currentMedia = initialMedia;
    }

    /**
     * Adds a media item to this container.
     * <p>
     * Notes:
     * <ul>
     *     <li>The passed ItemStack will be modified as items are consumed.</li>
     *     <li>No change is given for partial fills that reach the cap.</li>
     *     <li>Rejected items are left untouched.</li>
     * </ul>
     *
     * @param itemStack the media item to add
     * @param simulate if true, no media will actually be added, but the ItemStack will <em>still be modified</em>
     * @return <code>true</code>, if the insert accepted any media
     */
    public boolean insertMedia(@Nonnull ItemStack itemStack, boolean simulate) {
        if (infinite || getRemainingCapacity() <= 0) {
            return false;
        }

        if (itemStack.is(HexItems.CREATIVE_UNLOCKER.get())) {
            itemStack.shrink(1);
            setInfinite(true);
            return true;
        }

        int extracted = extractMediaFromItem(itemStack, simulate);
        return addMedia(extracted) > 0;
    }

    /**
     * Extracts as much media as we can from the given ItemStack.
     *
     * @param itemStack the ItemStack to extract media from
     * @param simulate if true, do not actually modify the ItemStack
     * @return the amount of extracted media
     */
    public int extractMediaFromItem(@Nonnull ItemStack itemStack, boolean simulate) {
        return (int) MediaHelper.extractMedia(itemStack, getRemainingCapacity(), true, simulate);
    }

    /**
     * Inserts media into this container.
     * <p>
     * Note: This method intentionally does not clamp to 0, allowing for "media debt".
     *
     * @param amount the amount of media to add
     * @return the remainder that did not fit
     */
    public int addMedia(int amount) {
        if (infinite) {
            // Don't accept any when infinite
            return amount;
        }

        int newMedia = currentMedia + amount;

        if (newMedia > maxMedia) {
            this.currentMedia = maxMedia;
            return newMedia - maxMedia;
        } else {
            this.currentMedia = newMedia;
            return 0;
        }
    }

    /**
     * Attempts to consume media from this container.
     *
     * @param amount the amount of media to consume
     * @return <code>true</code> if this container had sufficient media
     */
    public boolean consumeMedia(int amount) {
        if (infinite) {
            return true;
        }

        if (currentMedia >= amount) {
            this.currentMedia -= amount;
            return true;
        }

        return false;
    }

    /** Returns <code>true</code> if this container has sufficient capacity to accept the given amount of media. */
    public boolean mayAcceptMedia(int amount) {
        return (!infinite && (maxMedia - currentMedia) > amount);
    }

    /** Returns <code>true</code> if this container has sufficient media to spend given amount of media. */
    public boolean maySpendMedia(int amount) {
        return (infinite || currentMedia >= amount);
    }

    public int getRemainingCapacity() {
        return Math.max(0, maxMedia - currentMedia);
    }

    /** Returns the current stored media amount. */
    public int getCurrentMedia() {
        return currentMedia;
    }

    /**
     * Sets the media stored amount.
     * <p>
     * Note: The amount will <em>not</em> be clamped.
     *
     * @param media the new current media amount
     */
    public void setCurrentMedia(int media) {
        this.currentMedia = Math.min(Math.max(0, media), maxMedia);
    }

    /** Returns the maximum amount of media this container may store. */
    public int getMaxMedia() {
        return maxMedia;
    }

    /** Sets the maximum amount of media this container may store. */
    public void setMaxMedia(int maxMedia) {
        this.maxMedia = maxMedia;
    }

    /** Clamps the current media amount to 0..getMaxMedia() inclusive. */
    public void clampCurrentMedia() {
        this.currentMedia = Math.min(Math.max(0, currentMedia), maxMedia);
    }

    /** Returns <code>true</code> if this media container should be considered an infinite source. */
    public boolean isInfinite() {
        return infinite;
    }

    /** Sets this container to infinite. `consumeMedia()` will always succeed while this flag is set. */
    public void setInfinite(boolean infinite) {
        this.infinite = infinite;
    }

    // WorldlyContainer implementation

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction direction) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItem(int i, @Nonnull ItemStack itemStack) {
        if (infinite || currentMedia >= maxMedia) {
            return false;
        }
        if (itemStack.is(HexItems.CREATIVE_UNLOCKER.get())) {
            return true;
        }

        int amount = (int) MediaHelper.extractMedia(itemStack, getRemainingCapacity(), true, true);
        return amount > 0;
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, @Nonnull ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(i, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int i, @Nonnull ItemStack itemStack, @Nonnull Direction direction) {
        return false;
    }

    @Override
    public int getContainerSize() {
        return SLOTS.length;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getItem(int i) {
        return ItemStack.EMPTY.copy();
    }

    @Nonnull
    @Override
    public ItemStack removeItem(int i, int j) {
        return ItemStack.EMPTY.copy();
    }

    @Nonnull
    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY.copy();
    }

    @Override
    public void setItem(int i, @Nonnull ItemStack itemStack) {
        insertMedia(itemStack, false);
    }

    @Override
    public void setChanged() {
        // Handled by the owning entity
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        // No items to clear, and media is not drained this way
    }
}
