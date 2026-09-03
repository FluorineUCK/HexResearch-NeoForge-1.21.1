package name.dashkal.minecraft.hexresearch.forge.event;

import net.neoforged.bus.api.IEventBus;

public class Events {
    public static void init(IEventBus modBus) {
        // Capabilities
        CapabilityEventHandler.init(modBus);
    }
}
