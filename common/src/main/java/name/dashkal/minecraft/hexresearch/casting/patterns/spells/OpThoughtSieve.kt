package name.dashkal.minecraft.hexresearch.casting.patterns.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.common.entities.EntityWallScroll
import at.petrak.hexcasting.common.items.storage.ItemScroll
import at.petrak.hexcasting.api.HexAPI
import name.dashkal.minecraft.hexresearch.HexResearch
import name.dashkal.minecraft.hexresearch.casting.mishaps.MishapNotPerWorldPattern
import name.dashkal.minecraft.hexresearch.casting.mishaps.MishapNotScroll
import name.dashkal.minecraft.hexresearch.effect.MindFatigueEffect
import name.dashkal.minecraft.hexresearch.hexcompat.PerWorldPatternResolver
import name.dashkal.minecraft.hexresearch.hexcompat.ScrollPatternReader
import name.dashkal.minecraft.hexresearch.mindharm.MindHarmLogic
import name.dashkal.minecraft.hexresearch.network.Networking
import name.dashkal.minecraft.hexresearch.util.Either
import name.dashkal.minecraft.hexresearch.util.Right
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.item.ItemStack
import org.apache.logging.log4j.Logger
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class OpThoughtSieve : SpellAction {
    /** entity(Villager), entity(Scroll) -> */
    override val argc = 2

    /** Cost is 50 dust */
    val cost: Long = 50L * MediaConstants.DUST_UNIT.toLong()

    private val logger = HexResearch.LOGGER;

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        val villager = args.getEntity(ctx.world, 0, argc)
        val scrollEntity = args.getEntity(ctx.world, 1, argc)

        // Ambit checks
        ctx.assertEntityInRange(villager)
        ctx.assertEntityInRange(scrollEntity)

        // Check Villager
        if (villager !is Villager) throw MishapBadEntity(
            villager,
            Component.translatable("text.hexresearch.thought_sieve.villager")
        )
        // Check Scroll
        val (scroll, pattern) = checkScroll(ctx, scrollEntity)
        val perWorldPattern = findPerWorldPattern(ctx, pattern)

        // The requirements are met.  Pay the cost and invoke the spell.
        val caster = ctx.castingEntity as? ServerPlayer ?: throw MishapBadCaster()
        return SpellAction.Result(
            Spell(villager, caster, scroll, perWorldPattern),
            cost,
            listOf(
                ParticleSpray.burst(villager.position(), 1.0),
                ParticleSpray.burst(scrollEntity.position(), 1.0)
            )
        )
    }

    /**
     * Checks the passed in entity to see if it's a valid scroll.
     *
     * A valid scroll is either an ItemStack containing a Scroll item, or a EntityWallScroll (a hanging scroll).
     */
    private fun checkScroll(ctx: CastingEnvironment, scrollEntity: Entity): Pair<TargetScroll, HexPattern> {
        logger.debug("OpThoughtSieve.checkScroll")
        val (targetEntity, itemStack) = when (scrollEntity) {
            is ItemEntity -> {
                logger.debug("OpThoughtSieve.checkScroll: Found ItemEntity")
                Pair(Either.left(scrollEntity), scrollEntity.item)
            }
            is EntityWallScroll -> {
                logger.debug("OpThoughtSieve.checkScroll: Found EntityWallScroll")
                Pair(Either.right(scrollEntity), scrollEntity.scroll)
            }
            else -> throw MishapNotScroll()
        }

        val item = itemStack.item
        if (item is ItemScroll) {
            val pattern = ScrollPatternReader.read(itemStack, ctx.world)
            if (pattern != null) {
                logger.debug("OpThoughtSieve.checkScroll: Found Pattern: $pattern")
                return Pair(TargetScroll(targetEntity, itemStack, item), pattern)
            }
        }

        throw MishapNotScroll();
    }

    /**
     * Searches the registered per-world patterns for a match to the provided one.
     *
     * If found, returns it.  If not, mishaps.
     */
    private fun findPerWorldPattern(ctx: CastingEnvironment, pattern: HexPattern): HexPattern {
        logger.debug("OpThoughtSieve.findPerWorldPattern")

        val perWorldPattern = PerWorldPatternResolver.find(ctx.world, pattern)
        if (perWorldPattern != null) {
            logger.debug("OpThoughtSieve.findPerWorldPattern: Found $perWorldPattern")
            return perWorldPattern
        }
        logger.debug("OpThoughtSieve.findPerWorldPattern: Did not find anything")
        throw MishapNotPerWorldPattern()
    }

    /** Named tuple to carry all the information we require about the target scroll into the spell resolution phase */
    private data class TargetScroll(val entity: Either<ItemEntity, EntityWallScroll>, val itemStack: ItemStack, val item: ItemScroll)

    private data class Spell(val villager: Villager, val caster: ServerPlayer, val targetScroll: TargetScroll, val perWorldPattern: HexPattern) : RenderedSpell {
        private val logger: Logger = HexResearch.LOGGER

        override fun cast(ctx: CastingEnvironment) {
            // This counts as "mind murder" and should follow HexCasting's configuration for that
            if (HexConfig.server().doVillagersTakeOffenseAtMindMurder()) {
                logger.debug("OpThoughtSieve.Spell.cast: Broadcasting mind murder")
                villager.gossips.decay()
            }

            // Roll for mind harm before checking for success, as it might de-rank the villager and so affect chances
            logger.debug("OpThoughtSieve.Spell.cast: Rolling initial harm")
            rollMindHarm()

            // If the villager has mind fatigue, apply additional harm without a roll
            if (villager.activeEffects.any { it.effect.value() == MindFatigueEffect.getInstance() }) {
                logger.debug("OpThoughtSieve.Spell.cast: Mind Fatigue present, doing extra harm")
                MindHarmLogic.doRandomHarm(caster, villager)
            }

            // If the first harm(s) killed or brainswept the villager, we're done.
            if (!villager.isDeadOrDying && !HexAPI.instance().isBrainswept(villager)) {
                logger.debug("OpThoughtSieve.Spell.cast: Checking for success")
                if (checkSuccess()) {
                    logger.debug("OpThoughtSieve.Spell.cast: Success")
                    fillInTargetScroll(ctx)
                    // Give the advancement
                    ctx.world.server.advancements.get(HexResearch.id("enlightened_thought_sieve"))
                        ?.let { caster.advancements.award(it, "cast_spell") }
                    // 30 seconds of mind fatigue
                    logger.debug("OpThoughtSieve.Spell.cast: Applying Mind Fatigue")
                    villager.addEffect(MindFatigueEffect.effectInstance(20 * 30))
                } else {
                    // Failure means an additional mind harm roll
                    logger.debug("OpThoughtSieve.Spell.cast: Failure - Extra harm roll")
                    rollMindHarm()
                }
            } else {
                logger.debug("OpThoughtSieve.Spell.cast: Dead or brainless")
            }
        }

        /** Rewrite the target scroll with the exact pattern for this world */
        private fun fillInTargetScroll(ctx: CastingEnvironment) {
            logger.debug("OpThoughtSieve.Spell.cast: Redrawing scroll")
            targetScroll.item.writeDatum(targetScroll.itemStack, PatternIota(perWorldPattern))

            // If a wall scroll, force it to redraw
            val entity = targetScroll.entity
            if (entity is Right) {
                logger.debug("OpThoughtSieve.Spell.fillInTargetScroll: Sending update packet to client")
                Networking.sendScrollSync(entity.right, ctx.world)
            }
        }

        /** Random chance of success based on villager mastery. */
        private fun checkSuccess(): Boolean {
            val villagerRank = villager.villagerData.level
            return if (villagerRank < 5) {
                // Not a master, check for success
                Random.nextDouble() < successChance(villagerRank)
            } else {
                // Is a master, just succeed
                true
            }
        }

        /**
         * Calculates the chance of the great scroll being found, given the rank of the villager.
         *
         * `Rank (0-5) * 0.2`
         *
         * A novice will never succeed. A master will always succeed.
         */
        private fun successChance(villagerRank: Int): Double {
            return max(0.0, min(villagerRank.toDouble() * 0.2, 1.0))
        }

        /** Randomly roll for the villager having their mind harmed */
        private fun rollMindHarm() {
            if (Random.nextDouble() < mindHarmChance(villager.getPlayerReputation(caster))) {
                logger.debug("OpThoughtSieve.Spell.rollMindHarm: Doing harm")
                MindHarmLogic.doRandomHarm(caster, villager)
            } else {
                logger.debug("OpThoughtSieve.Spell.rollMindHarm: Villager made its save")
            }
        }

        /**
         * Calculates the chance of the villager mind harm, given the reputation of the caster.
         *
         * `(4 * log10(x)) / 9`
         * - At 13 popularity there's a 50% chance of mind harm
         * - At 48 popularity there's a 25% chance of mind harm
         * - At 106 popularity there's a 10% chance of mind harm
         * - At 178 popularity and above, harm will never occur
         */
        private fun mindHarmChance(reputation: Int): Double {
            val dmgRoll = (4.0 * log10(reputation.toDouble())) / 9.0
            return max(0.0, min(1.0 - dmgRoll, 1.0))
        }
    }
}
