package name.dashkal.minecraft.hexresearch.util;

import at.petrak.hexcasting.server.ScrungledPatternsSave;
import name.dashkal.minecraft.hexresearch.HexResearch;
import name.dashkal.minecraft.hexresearch.registry.HRHexPatterns;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class PatternFixer {
    private PatternFixer() {
    }

    public static void ensurePerWorldPatterns(ServerLevel serverLevel) {
        // SERVER_LEVEL_LOAD fires once for every dimension, but Hex Casting stores
        // the per-world pattern manifest in the overworld's data storage.
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        ServerLevel overworld = serverLevel.getServer().overworld();
        ScrungledPatternsSave patterns = ScrungledPatternsSave.open(overworld);

        boolean missingPattern = HRHexPatterns.PER_WORLD_PATTERN_KEYS.stream()
                .anyMatch(key -> patterns.lookupReverse(key) == null);
        if (!missingPattern) {
            return;
        }

        if (HexResearch.getServerConfig().patternConfig().forceRecalculateMissing()) {
            HexResearch.LOGGER.warn(
                    "Hex Casting's per-world pattern data is missing a Hex Research pattern; regenerating it."
            );
            overworld.getDataStorage().set(
                    ScrungledPatternsSave.TAG_SAVED_DATA,
                    ScrungledPatternsSave.createFromScratch(overworld.getSeed())
            );
        } else {
            HexResearch.LOGGER.warn(
                    "Hex Casting's per-world pattern data is missing a Hex Research pattern; "
                            + "run /hexcasting recalcPatterns to regenerate it."
            );
        }
    }
}
