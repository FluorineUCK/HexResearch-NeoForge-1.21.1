package name.dashkal.minecraft.hexresearch.interop.impl;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import dev.architectury.platform.Platform;
import name.dashkal.minecraft.hexresearch.HexResearch;
import name.dashkal.minecraft.hexresearch.util.Mind;
import net.minecraft.core.BlockPos;

import java.util.Optional;

/**
 * Implementation class for HexGloop interoperation.
 * <p>
 * Do not access this class directly. Use HexGloopInterOp in the package above.
 */
public class HexGloopInteropImpl {
    static {
        if (!Platform.isModLoaded("hexgloop")) {
            throw new AssertionError("HexGloopInteropImpl loaded when hexgloop was not present");
        } else {
            HexResearch.LOGGER.info("Initializing HexGloop interop");
        }
    }

    /**
     * Offers a mind to HexGloop (IDynamicFlayTarget) to see if it wants to accept the mind.
     *
     * @param ctx the casting context in which we are imbuing
     * @param blockPos block position of the target block
     * @param mind the mind being offered
     * @return an optional Runnable that if present, will accept the offered mind
     */
    public Optional<Runnable> offerMind(CastingEnvironment ctx, BlockPos blockPos, Mind mind) {
        return Optional.empty();
    }
}
