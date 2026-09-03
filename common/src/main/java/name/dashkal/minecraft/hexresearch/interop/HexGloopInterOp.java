package name.dashkal.minecraft.hexresearch.interop;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import name.dashkal.minecraft.hexresearch.interop.impl.HexGloopInteropImpl;
import name.dashkal.minecraft.hexresearch.util.Mind;
import net.minecraft.core.BlockPos;

import java.util.Optional;

/**
 * Interop with <a href="https://github.com/SamsTheNerd/HexGloop">HexGloop</a> by SamTheNerd
 */
public class HexGloopInterOp {
    private final IndirectionLogic<HexGloopInteropImpl> indirect;

    // Package-private to ensure we only ever construct one
    HexGloopInterOp() {
        this.indirect = new IndirectionLogic<>("hexgloop", IndirectionLogic.Versions.regex("1\\.19\\.2-0.*"), HexGloopInteropImpl::new);
    }

    /** Returns a handle to the HexGloop interop object. */
    public static HexGloopInterOp getInstance() {
        return Interop.getHexGloop();
    }

    /** Returns <code>true</code> if a compatible version of HexGloop is present. */
    public boolean isPresent() {
        return indirect.isPresent();
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
        return indirect.withApiFuncOpt(api -> api.offerMind(ctx, blockPos, mind));
    }
}
