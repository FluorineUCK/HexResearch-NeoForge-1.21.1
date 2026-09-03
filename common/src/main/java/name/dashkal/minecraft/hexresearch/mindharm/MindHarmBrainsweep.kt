package name.dashkal.minecraft.hexresearch.mindharm

import at.petrak.hexcasting.api.HexAPI
import name.dashkal.minecraft.hexresearch.HexResearch
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.npc.Villager

/** The villager is brainswept (flayed) */
class MindHarmBrainsweep : MindHarmMechanic {
    val ID: ResourceLocation = HexResearch.id("mindharm_brainsweep")

    override fun getId(): ResourceLocation = ID

    override fun getSeverity(): MindHarmSeverity = MindHarmSeverity.PERMANENT_MAJOR

    override fun doHarm(attacker: Entity?, villager: Villager): Boolean {
        return if (!HexAPI.instance().isBrainswept(villager)) {
            HexAPI.instance().brainsweep(villager)
            true
        } else {
            false
        }
    }
}
