package name.dashkal.minecraft.hexresearch.casting.patterns.villager

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.npc.Villager

class OpVillagerRank : ConstMediaAction {
    override val argc: Int = 1
    override val mediaCost: Long = 0L

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val target = args.getEntity(ctx.world, 0, argc)
        ctx.assertEntityInRange(target)
        if (target !is Villager) throw MishapBadEntity(
            target,
            Component.translatable("text.hexresearch.villager_actions.villager")
        )

        return target.villagerData.level.asActionResult
    }
}
