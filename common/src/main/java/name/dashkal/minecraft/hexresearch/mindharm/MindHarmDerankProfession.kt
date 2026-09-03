package name.dashkal.minecraft.hexresearch.mindharm

import name.dashkal.minecraft.hexresearch.HexResearch
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.entity.npc.VillagerProfession
import net.minecraft.world.entity.npc.VillagerTrades
import kotlin.math.min

/** The villager loses a rank in their profession, falling to a Nitwit if they lose their last trade. */
class MindHarmDerankProfession : MindHarmMechanic {
    override fun getId(): ResourceLocation = HexResearch.id("mindharm_derank")

    override fun getSeverity(): MindHarmSeverity = MindHarmSeverity.PERMANENT_MINOR

    override fun doHarm(attacker: Entity?, villager: Villager): Boolean {
        val rank = villager.villagerData.level
        return if (rank > 0) {
            // Reduce the level by 1
            villager.villagerData = villager.villagerData.setLevel(rank - 1)

            /*
             * Delete the trades added in the most recent level
             *
             * Remove the minimum of
             *  - the remaining trades
             *  - the usual trades per level
             *  - the number of trades available at the rank we are demoting from
             */
            val toRemove = min(
                villager.offers.size,
                min(
                    2,
                    VillagerTrades.TRADES[villager.villagerData.profession]?.get(rank)?.size ?: 0
                )
            )
            repeat(toRemove) {
                villager.offers.removeLast()
            }

            // If that was the last trade
            if (villager.offers.isEmpty()) {
                // They are now a nitwit
                villager.villagerData = villager.villagerData.setProfession(VillagerProfession.NITWIT).setLevel(0)
            }

            true
        } else {
            // No rank to lose
            false
        }
    }
}
