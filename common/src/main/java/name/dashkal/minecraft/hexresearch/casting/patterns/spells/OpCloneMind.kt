package name.dashkal.minecraft.hexresearch.casting.patterns.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.*
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import name.dashkal.minecraft.hexresearch.block.entity.CognitiveInducerBlockEntity
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.phys.Vec3

class OpCloneMind : SpellAction {
    override val argc: Int = 2
    val cost: Long = MediaConstants.CRYSTAL_UNIT.toLong()

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        val sourceMind = args.getEntity(ctx.world, 0, argc)
        val targetMindPos = args.getBlockPos(1, argc)
        ctx.assertEntityInRange(sourceMind)
        ctx.assertPosInRange(targetMindPos)

        if (sourceMind !is Villager) {
            throw MishapBadBlock(targetMindPos, "text.hexresearch.imbue_mind.artificial_mind".asTranslatedComponent)
        }

        val targetMind = ctx.world.getBlockEntity(targetMindPos)
        if (targetMind == null || targetMind !is CognitiveInducerBlockEntity) {
            throw MishapBadBlock(targetMindPos, "text.hexresearch.imbue_mind.artificial_mind".asTranslatedComponent)
        }
        ctx.assertPosInRangeForEditing(targetMindPos)

        return SpellAction.Result(
            Spell(sourceMind, targetMind),
            cost,
            listOf(
                ParticleSpray.cloud(sourceMind.position(), 1.0),
                ParticleSpray.burst(Vec3.atCenterOf(targetMindPos), 0.3, 100)
            )
        )
    }

    private data class Spell(
        val sourceMind: Villager,
        val targetMind: CognitiveInducerBlockEntity
    ) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            targetMind.forceMind(
                BuiltInRegistries.VILLAGER_PROFESSION.getKey(sourceMind.villagerData.profession),
                BuiltInRegistries.VILLAGER_TYPE.getKey(sourceMind.villagerData.type),
                sourceMind.villagerData.level
            )
        }
    }
}
