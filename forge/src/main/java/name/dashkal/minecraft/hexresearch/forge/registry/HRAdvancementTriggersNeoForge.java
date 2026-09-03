package name.dashkal.minecraft.hexresearch.forge.registry;

import name.dashkal.minecraft.hexresearch.HexResearch;
import name.dashkal.minecraft.hexresearch.registry.HRAdvancementTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge lifecycle registration for Hex Research criterion trigger types. */
public final class HRAdvancementTriggersNeoForge {
    private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(Registries.TRIGGER_TYPE, HexResearch.MOD_ID);

    static {
        TRIGGERS.register("advancement", () -> HRAdvancementTriggers.ADVANCEMENT_TRIGGER);
    }

    private HRAdvancementTriggersNeoForge() {
    }

    public static void init(IEventBus modBus) {
        TRIGGERS.register(modBus);
    }
}
