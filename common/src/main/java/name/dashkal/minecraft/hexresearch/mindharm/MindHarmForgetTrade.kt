package name.dashkal.minecraft.hexresearch.mindharm

import name.dashkal.minecraft.hexresearch.HexResearch
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.npc.VillagerProfession
import kotlin.random.Random

/** The villager loses one random trade, falling to a Nitwit if that was the last. */
class MindHarmForgetTrade : MindHarmMechanic {
    override fun getId(): ResourceLocation = HexResearch.id("mindharm_forget_trade")

    override fun getSeverity(): MindHarmSeverity = MindHarmSeverity.PERMANENT_MINOR

    override fun doHarm(attacker: Entity?, villager: Villager): Boolean {
        return if (!villager.offers.isEmpty()) {
            villager.offers.removeAt(Random.nextInt(0, villager.offers.size))

            // If that was the last trade
            if (villager.offers.isEmpty()) {
                // They are now a nitwit
                villager.villagerData = villager.villagerData.setProfession(VillagerProfession.NITWIT).setLevel(0)
            }
            true
        } else {
            // No trades left to lose
            false
        }
    }
}