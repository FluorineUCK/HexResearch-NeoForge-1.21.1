package name.dashkal.minecraft.hexresearch;

import name.dashkal.minecraft.hexresearch.client.scrying.ScryingLensOverlays;

/**
 * Common client loading entrypoint.
 */
public class HexResearchClient {
    public static void init() {
        ScryingLensOverlays.init();
    }
}
