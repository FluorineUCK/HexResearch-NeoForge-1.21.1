package name.dashkal.minecraft.hexresearch.mindharm

import at.petrak.hexcasting.api.casting.mishaps.Mishap
import name.dashkal.minecraft.hexresearch.HexResearch
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.npc.Villager

/** The villager is killed outright */
class MindHarmKill : MindHarmMechanic {
    override fun getId(): ResourceLocation = HexResearch.id("mindharm_kill")

    override fun getSeverity(): MindHarmSeverity = MindHarmSeverity.DEATH

    override fun doHarm(attacker: Entity?, villager: Villager): Boolean {
        // Kill the villager the same way a botched brainweep does
        Mishap.trulyHurt(villager, villager.damageSources().generic(), villager.health)

        return villager.isDeadOrDying;
    }
}
