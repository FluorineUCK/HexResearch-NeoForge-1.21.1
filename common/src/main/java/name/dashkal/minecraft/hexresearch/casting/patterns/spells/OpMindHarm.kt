package name.dashkal.minecraft.hexresearch.casting.patterns.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import name.dashkal.minecraft.hexresearch.mindharm.MindHarmLogic
import name.dashkal.minecraft.hexresearch.registry.HRMindHarms
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.npc.Villager

class OpMindHarm : SpellAction {
    override val argc: Int = 1

    /** Cost is 10 dust */
    val cost: Long = MediaConstants.DUST_UNIT.toLong()

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        val villager = args.getEntity(ctx.world, 0, argc)
        ctx.assertEntityInRange(villager)

        // Check Villager
        if (villager !is Villager) throw MishapBadEntity(
            villager,
            Component.translatable("text.hexresearch.thought_sieve.villager")
        )

        // The requirements are met.  Pay the cost and invoke the spell.
        val caster = ctx.castingEntity as? ServerPlayer ?: throw MishapBadCaster()
        return SpellAction.Result(
            Spell(villager, caster),
            cost,
            listOf(
                ParticleSpray.burst(villager.position(), 1.0)
            )
        )
    }

    private data class Spell(val villager: Villager, val caster: ServerPlayer) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            MindHarmLogic.doHarm(caster, villager, HRMindHarms.KILL.id)
        }
    }
}
