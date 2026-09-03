package name.dashkal.minecraft.hexresearch.forge.cap;

import name.dashkal.minecraft.hexresearch.HexResearch;
import name.dashkal.minecraft.hexresearch.block.entity.AbstractMediaContainerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class MediaContainerItemHandler implements IItemHandler {
    public static final ResourceLocation ID = HexResearch.id("mediacontainer");

    private final AbstractMediaContainerBlockEntity blockEntity;

    public MediaContainerItemHandler(AbstractMediaContainerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getSlots() {
        return blockEntity.getContainerSize();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slotId) {
        return blockEntity.getItem(slotId);
    }

    @Override
    public @NotNull ItemStack insertItem(int slotId, @NotNull ItemStack itemStack, boolean simulate) {
        if (!isItemValid(slotId, itemStack)) {
            return itemStack;
        }

        ItemStack stack = itemStack.copy();

        if (!simulate) {
            blockEntity.insertMedia(stack, false);
        } else {
            // We're simulating, but the capability contract still wants us to return an ItemStack with the media taken.
            blockEntity.extractMediaFromItem(stack, false);
        }

        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slotId, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slotId) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slotId, @NotNull ItemStack itemStack) {
        return blockEntity.canPlaceItem(slotId, itemStack);
    }

}
