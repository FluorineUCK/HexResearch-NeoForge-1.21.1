package name.dashkal.minecraft.hexresearch.client.scrying;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.datafixers.util.Pair;
import name.dashkal.minecraft.hexresearch.block.entity.AbstractMediaContainerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MediaContainerOverlay implements ScryingLensOverlayRegistry.OverlayBuilder {
    @Override
    public void addLines(List<Pair<ItemStack, Component>> lines, BlockState state, BlockPos pos, Player observer, Level world, Direction hitFace) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof AbstractMediaContainerBlockEntity mc) {
            ItemStack dustStack = new ItemStack(HexItems.AMETHYST_DUST.get());
            if (mc.isInfinite()) {
                lines.add(Pair.of(dustStack, Component.translatable("hexresearch.misc.overlay.infinite")));
            } else {
                int mediaDust = (int) (mc.getCurrentMedia() / MediaConstants.DUST_UNIT);
                lines.add(Pair.of(dustStack, Component.literal(Integer.toString(mediaDust))));
            }
        }
    }
}
