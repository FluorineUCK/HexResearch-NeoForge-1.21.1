package name.dashkal.minecraft.hexresearch.forge.event;

import name.dashkal.minecraft.hexresearch.block.entity.AbstractMediaContainerBlockEntity;
import name.dashkal.minecraft.hexresearch.forge.cap.MediaContainerItemHandler;
import name.dashkal.minecraft.hexresearch.registry.HRBlockEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class CapabilityEventHandler {
    public static void init(IEventBus modBus) {
        modBus.addListener(CapabilityEventHandler::registerCapabilities);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent ev) {
        ev.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                HRBlockEntities.ARTIFICIAL_MIND.get(),
                (AbstractMediaContainerBlockEntity be, net.minecraft.core.Direction direction) -> new MediaContainerItemHandler(be)
        );
    }
}
