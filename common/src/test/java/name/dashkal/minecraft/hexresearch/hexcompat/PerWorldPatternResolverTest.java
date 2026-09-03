package name.dashkal.minecraft.hexresearch.hexcompat;

import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PerWorldPatternResolverTest {
    @Test
    void selectsMatchingCandidateOutsideAddonOwnedSet() {
        HexPattern unrelated = HexPattern.fromAngles("aq", HexDir.EAST);
        HexPattern canonicalGreatSpell = HexPattern.fromAngles("eeeee", HexDir.NORTH_EAST);
        HexPattern rotatedScrollCopy = HexPattern.fromAngles("eeeee", HexDir.WEST);

        assertSame(
            canonicalGreatSpell,
            PerWorldPatternResolver.findInCandidates(
                rotatedScrollCopy,
                List.of(unrelated, canonicalGreatSpell)
            )
        );
    }

    @Test
    void returnsNullWhenScrollIsNotAnyTaggedGreatSpell() {
        HexPattern target = HexPattern.fromAngles("aq", HexDir.EAST);
        HexPattern candidate = HexPattern.fromAngles("eeeee", HexDir.NORTH_EAST);

        assertNull(PerWorldPatternResolver.findInCandidates(target, List.of(candidate)));
    }
}
