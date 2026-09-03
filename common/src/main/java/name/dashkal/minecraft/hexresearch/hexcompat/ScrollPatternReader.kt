package name.dashkal.minecraft.hexresearch.hexcompat

import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexAngle
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.casting.PatternRegistryManifest
import at.petrak.hexcasting.common.items.storage.ItemScroll
import at.petrak.hexcasting.common.lib.HexDataComponents
import at.petrak.hexcasting.common.lib.HexRegistries
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

/**
 * Reads player-written, loot-generated per-world, and pre-data-component scrolls.
 *
 * Hex Casting stores a directly written scroll in its PATTERN data component. Loot-generated
 * ancient scrolls initially store only ACTION and materialize PATTERN from an inventory tick.
 * A dropped or immediately placed loot scroll therefore has no value from ItemScroll.readIota,
 * even though it already identifies a valid per-world action. Resolve that deferred form here
 * instead of requiring an unrelated inventory tick before Thought Sieve can inspect it.
 *
 * Hex Casting 0.10/0.11 stored a scroll's pattern in the old item tag as a compound containing
 * `start_dir` and an `angles` byte array. Minecraft's 1.21 item stack data fixer preserves unknown
 * mod tags under CUSTOM_DATA; Hex Casting pre-2 does not read that legacy payload itself. Migrate
 * it at this addon boundary so old worlds and saved creative-hotbar scrolls remain usable.
 */
object ScrollPatternReader {
    private const val LEGACY_PATTERN = "pattern"
    private const val LEGACY_ACTION = "op_id"
    private const val LEGACY_START_DIR = "start_dir"
    private const val LEGACY_ANGLES = "angles"

    @JvmStatic
    fun read(stack: ItemStack, world: ServerLevel): HexPattern? {
        val scroll = stack.item as? ItemScroll ?: return null
        val direct = scroll.readIota(stack)
        if (direct is PatternIota) {
            return direct.pattern
        }

        val action = stack.get(HexDataComponents.ACTION.get())
        if (action != null) {
            PatternRegistryManifest.getCanonicalStrokesPerWorld(action, world.server.overworld())?.let {
                return it
            }
        }

        val legacyData = stack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return null
        val legacyPattern = decodeLegacyPattern(legacyData) ?: return null
        migrateLegacyComponents(stack, world, legacyData, legacyPattern)
        return legacyPattern
    }

    private fun decodeLegacyPattern(customData: CompoundTag): HexPattern? {
        if (!customData.contains(LEGACY_PATTERN, Tag.TAG_COMPOUND.toInt())) {
            return null
        }
        val patternTag = customData.getCompound(LEGACY_PATTERN)
        if (!patternTag.contains(LEGACY_START_DIR, Tag.TAG_BYTE.toInt())
            || !patternTag.contains(LEGACY_ANGLES, Tag.TAG_BYTE_ARRAY.toInt())
        ) {
            return null
        }

        val directions = HexDir.values()
        val startDirection = patternTag.getByte(LEGACY_START_DIR).toInt()
        if (startDirection !in directions.indices) {
            return null
        }

        val angleValues = HexAngle.values()
        val angles = patternTag.getByteArray(LEGACY_ANGLES).map { encoded ->
            val index = encoded.toInt()
            if (index !in angleValues.indices) {
                return null
            }
            angleValues[index]
        }
        return HexPattern(directions[startDirection], angles.toMutableList())
    }

    private fun migrateLegacyComponents(
        stack: ItemStack,
        world: ServerLevel,
        customData: CompoundTag,
        pattern: HexPattern
    ) {
        stack.set(HexDataComponents.PATTERN.get(), pattern)

        if (customData.contains(LEGACY_ACTION, Tag.TAG_STRING.toInt())) {
            val actionId = ResourceLocation.tryParse(customData.getString(LEGACY_ACTION))
            val registry = world.registryAccess().registryOrThrow(HexRegistries.ACTION)
            if (actionId != null && registry.containsKey(actionId)) {
                stack.set(HexDataComponents.ACTION.get(), ResourceKey.create(HexRegistries.ACTION, actionId))
            }
        }

        customData.remove(LEGACY_PATTERN)
        customData.remove(LEGACY_ACTION)
        if (customData.isEmpty) {
            stack.remove(DataComponents.CUSTOM_DATA)
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData))
        }
    }
}
