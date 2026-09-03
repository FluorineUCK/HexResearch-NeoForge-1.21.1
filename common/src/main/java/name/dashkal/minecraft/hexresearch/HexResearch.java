package name.dashkal.minecraft.hexresearch;

import dev.architectury.platform.Platform;
import name.dashkal.minecraft.hexresearch.config.ClientConfig;
import name.dashkal.minecraft.hexresearch.config.CommonConfig;
import name.dashkal.minecraft.hexresearch.config.HexResearchConfig;
import name.dashkal.minecraft.hexresearch.config.ServerConfig;
import name.dashkal.minecraft.hexresearch.event.Events;
import name.dashkal.minecraft.hexresearch.interop.Interop;
import name.dashkal.minecraft.hexresearch.registry.*;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Common entry point for HexResearch.
 * <p>
 * See HexResearchForge and HexResearchFabric for the per-platform entry points.
 */
public class HexResearch {
    public static final String MOD_ID = "hexresearch";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("Hex Research {} Initializing", Platform.getMod(MOD_ID).getVersion());

        // Minecraft / Forge / Fabric Registries
        HRBlocks.init();
        HRBlockEntities.init();
        HRItems.init();
        HRIotaTypes.init();
        HRHexPatterns.init();
        HREffects.init();
        HRAdvancementTriggers.init();

        // Events
        Events.init();

        // HexResearch internals
        HRMindHarms.init();

        // Interop
        Interop.init();
    }

    public static ClientConfig getClientConfig() {
        return HexResearchConfig.getInstance().getClientConfig();
    }

    public static CommonConfig getCommonConfig() {
        return HexResearchConfig.getInstance().getCommonConfig();
    }

    public static ServerConfig getServerConfig() {
        return HexResearchConfig.getInstance().getServerConfig();
    }

    /**
     * Shortcut for identifiers specific to this mod.
     */
    public static ResourceLocation id(String string) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, string);
    }
}
