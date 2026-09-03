package name.dashkal.minecraft.hexresearch.casting.mishaps

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import name.dashkal.minecraft.hexresearch.util.Option
import name.dashkal.minecraft.hexresearch.util.Some
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.DyeColor

class MishapNotCaptureShard(val hand: Option<InteractionHand>, val shouldBeFilled: Boolean) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.BROWN)

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component {
        return if (shouldBeFilled) {
            "hexresearch.mishap.uncrystallize_life.filled".asTranslatedComponent
        } else {
            "hexresearch.mishap.not_capture_shard.empty".asTranslatedComponent
        }
    }

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> {
        // Hexcasting 0.12 no longer exposes the old held-item drop helper. Keep this
        // mishap non-destructive; the class is currently unused by registered actions.
        return stack
    }
}
