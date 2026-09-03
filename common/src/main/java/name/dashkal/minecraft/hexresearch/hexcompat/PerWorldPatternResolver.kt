package name.dashkal.minecraft.hexresearch.hexcompat

import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.common.casting.PatternRegistryManifest
import at.petrak.hexcasting.common.lib.HexRegistries
import name.dashkal.minecraft.hexresearch.util.HexPatternMatch
import net.minecraft.server.level.ServerLevel

/**
 * Public-API bridge for resolving Hex Casting's per-world (great-spell) patterns.
 *
 * HexResearch must inspect the action tag rather than its own registrations: the
 * target scroll may contain a great spell supplied by Hex Casting or any addon.
 */
object PerWorldPatternResolver {
    @JvmStatic
    fun find(world: ServerLevel, target: HexPattern): HexPattern? {
        val patternWorld = world.server.overworld()
        val actionRegistry = world.registryAccess().registryOrThrow(HexRegistries.ACTION)
        val candidates = actionRegistry.getTagOrEmpty(HexTags.Actions.PER_WORLD_PATTERN).mapNotNull { holder ->
            val key = holder.unwrapKey().orElse(null) ?: return@mapNotNull null
            PatternRegistryManifest.getCanonicalStrokesPerWorld(key, patternWorld)
        }
        return findInCandidates(target, candidates)
    }

    @JvmStatic
    fun findInCandidates(target: HexPattern, candidates: Iterable<HexPattern>): HexPattern? =
        candidates.firstOrNull { HexPatternMatch.shapeMatches(target, it) }
}
