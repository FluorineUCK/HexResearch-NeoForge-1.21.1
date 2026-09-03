package name.dashkal.minecraft.hexresearch.casting.mishaps

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.TreeList
import name.dashkal.minecraft.hexresearch.block.entity.CognitiveInducerBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor
import net.minecraft.world.phys.Vec3

class MishapBadMindImbue(val artificialMind: CognitiveInducerBlockEntity, val targetPos: BlockPos) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.GREEN)

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component =
        error("bad_brainsweep", blockAtPos(ctx, this.targetPos))

    override fun particleSpray(ctx: CastingEnvironment): ParticleSpray {
        return ParticleSpray.burst(Vec3.atCenterOf(targetPos), 1.0)
    }

    override fun execute(ctx: CastingEnvironment, errorCtx: Context, stack: TreeList<Iota>): TreeList<Iota> {
        artificialMind.clearMind()
        return stack
    }
}
