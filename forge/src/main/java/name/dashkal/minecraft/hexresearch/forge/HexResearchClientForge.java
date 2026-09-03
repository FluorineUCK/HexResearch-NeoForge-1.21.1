package name.dashkal.minecraft.hexresearch.forge;

import name.dashkal.minecraft.hexresearch.HexResearchClient;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Forge client loading entrypoint.
 */
public class HexResearchClientForge {
    public static void init(FMLClientSetupEvent event) {
        HexResearchClient.init();
        DevelopmentProbeBootstrap.register(
            "name.dashkal.minecraft.hexresearch.forge.probe.HexResearchClientValidation",
            "hexresearch.probe.validatePatchouliLang",
            "hexresearch.probe.exitAfterClientStartup",
            "hexresearch.probe.validateNetworkDispatch"
        );
    }
}
