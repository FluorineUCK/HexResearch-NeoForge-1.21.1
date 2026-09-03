package name.dashkal.minecraft.hexresearch.util;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Compare two HexPatterns to see if they describe the same shape, without regard to stroke order.
 */
public class HexPatternMatch {
    /**
     * Compares two patterns, and returns <code>true</code> if they describe the same graph of line segments.
     *
     * <p>Use case is to compare a pattern to a per-world pattern (great spells), to see if they match without regard
     * to stroke order.</p>
     *
     * @param pattern1 the first pattern to compare
     * @param pattern2 the second pattern to compare
     * @return <code>true</code> if they describe a graph with the same segments.
     */
    public static boolean shapeMatches(@Nonnull HexPattern pattern1, @Nonnull HexPattern pattern2) {
        // Compare sizes for a fast-fail
        if (pattern1.getAngles().size() != pattern2.getAngles().size()) {
            // Trivially different. They have a different number of strokes.
            return false;
        } else if (pattern1.getAngles().size() == 0) {
            // Trivially the same. These are both zero length patterns.
            // Impossible, but this avoids a potential crash if that assumption is ever violated.
            return true;
        } else {
            for (HexDir dir : HexDir.values()) {
                // Check against each rotation of pattern2, fast succeeding if true
                if (match(pattern1, new HexPattern(dir, pattern2.getAngles()))) {
                    return true;
                }
            }
            return false;
        }
    }

    private static boolean match(@Nonnull HexPattern pattern1, @Nonnull HexPattern pattern2) {
        // We can assume at this point that both have non-zero and equal sizes

        // Reduce both patterns to a list of positions.
        List<HexCoord> coords1 = new ArrayList<>(pattern1.positions());
        List<HexCoord> coords2 = new ArrayList<>(pattern2.positions());

        // Closed loop special case
        // For a pattern that ends where it started, remove the duplicated coordinate.
        // This allows closed loops where different start/end locations were selected to match.
        if (coords1.size() >= 2) {
            boolean firstMatched = false;
            if (coords1.get(0).equals(coords1.get(coords1.size() - 1))) {
                coords1.remove(coords1.size() - 1);
                firstMatched = true;
            }
            if (coords2.get(0).equals(coords2.get(coords2.size() - 1))) {
                if (firstMatched) {
                    coords2.remove(coords2.size() - 1);
                } else {
                    // First was closed and second open - not equal
                    return false;
                }
            } else {
                if (firstMatched) {
                    // First was open and second closed - not equal
                    return false;
                }
            }
        }

        // Sort both lists and re-origin them
        coords1.sort(hcc);
        coords2.sort(hcc);
        reOrigin(coords1);
        reOrigin(coords2);

        // Now normalized, we can simply compare
        return coords1.equals(coords2);
    }

    private static void reOrigin(List<HexCoord> positions) {
        HexCoord offset = positions.get(0);
        positions.replaceAll(hexCoord -> hexCoord.minus(offset));
    }

    // lexicographical sort on Q, then R.
    private static class HexCoordComparator implements Comparator<HexCoord> {
        @Override
        public int compare(HexCoord o1, HexCoord o2) {
            int c1 = Integer.compare(o1.getQ(), o2.getQ());
            if (c1 == 0) {
                return Integer.compare(o1.getR(), o2.getR());
            }
            return c1;
        }

        @Override
        public boolean equals(Object obj) {
            return obj.getClass() == getClass();
        }
    }

    private static final HexCoordComparator hcc = new HexCoordComparator();
}
