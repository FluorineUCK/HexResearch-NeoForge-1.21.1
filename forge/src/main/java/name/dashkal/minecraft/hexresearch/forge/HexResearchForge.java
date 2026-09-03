package name.dashkal.minecraft.hexresearch.forge;

import at.petrak.hexcasting.common.lib.HexRegistries;
import name.dashkal.minecraft.hexresearch.HexResearch;
import name.dashkal.minecraft.hexresearch.forge.config.HexResearchConfigForgeImpl;
import name.dashkal.minecraft.hexresearch.forge.event.Events;
import name.dashkal.minecraft.hexresearch.forge.network.HexResearchPayloadHandler;
import name.dashkal.minecraft.hexresearch.forge.registry.HRAdvancementTriggersNeoForge;
import name.dashkal.minecraft.hexresearch.forge.xplat.ForgeXPlatAPIImpl;
import name.dashkal.minecraft.hexresearch.registry.HRHexPatterns;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * This is your loading entrypoint on forge, in case you need to initialize
 * something platform-specific.
 */
@Mod(HexResearch.MOD_ID)
public class HexResearchForge {
    public HexResearchForge(IEventBus bus, ModContainer container) {
        bus.addListener(HexResearchClientForge::init);
        bus.addListener(HexResearchForge::registerHexActions);

        ForgeXPlatAPIImpl.init();
        HexResearchPayloadHandler.init(bus);
        HRAdvancementTriggersNeoForge.init(bus);
        HexResearchConfigForgeImpl.init(bus, container);
        HexResearch.init();
        Events.init(bus);
        DevelopmentProbeBootstrap.register(
            "name.dashkal.minecraft.hexresearch.forge.probe.HexResearchProbeValidation",
            "hexresearch.probe.validatePerWorldPatternResolver"
        );
    }

    private static void registerHexActions(RegisterEvent event) {
        if (!event.getRegistryKey().equals(HexRegistries.ACTION)) {
            return;
        }

        HRHexPatterns.registerAll((id, entry) -> event.register(HexRegistries.ACTION, id, () -> entry));
    }
}
