package name.dashkal.minecraft.hexresearch.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import name.dashkal.minecraft.hexresearch.HexResearch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class HRItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(HexResearch.MOD_ID, Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(HexResearch.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> HEX_RESEARCH_TAB = TABS.register("hex_research", () -> CreativeTabRegistry.create(builder -> builder
            .title(Component.translatable("itemGroup.hexresearch.hex_research"))
            .icon(() -> new ItemStack(HRBlocks.COGNITIVE_INDUCER.get()))
            .displayItems((parameters, output) -> output.accept(HRBlocks.COGNITIVE_INDUCER.get()))
    ));

    // Items
//    public static final RegistrySupplier<Item> DUMMY_ITEM = ITEMS.register("dummy_item", () -> new ItemDummy(new Item.Properties().tab(HEX_RESEARCH_TAB)));

    public static void init() {
        TABS.register();
        ITEMS.register();
    }

    public static <T extends Item> RegistrySupplier<T> item(ResourceLocation id, Supplier<T> item) {
        return ITEMS.register(id, item);
    }
}
