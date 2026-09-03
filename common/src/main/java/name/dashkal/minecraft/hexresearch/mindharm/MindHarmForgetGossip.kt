package name.dashkal.minecraft.hexresearch.mindharm

import name.dashkal.minecraft.hexresearch.HexResearch
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.npc.Villager

/**
 * The villager forgets a random gossip entry.
 */
class MindHarmForgetGossip : MindHarmMechanic {
    override fun getId(): ResourceLocation = HexResearch.id("mindharm_forget_gossip")

    override fun getSeverity(): MindHarmSeverity = MindHarmSeverity.REVERSIBLE

    override fun doHarm(attacker: Entity?, villager: Villager): Boolean {
        // TODO implement
        return true
    }
}