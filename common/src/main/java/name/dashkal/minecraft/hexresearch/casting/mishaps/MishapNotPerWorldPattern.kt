package name.dashkal.minecraft.hexresearch.casting.mishaps

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapNotPerWorldPattern() : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.YELLOW)

    override fun resolutionType(ctx: CastingEnvironment): ResolvedPatternType =
        ResolvedPatternType.UNRESOLVED

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component =
        "hexresearch.mishap.not_per_world_pattern".asTranslatedComponent

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> =
        stack.appended(GarbageIota())
}
