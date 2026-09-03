package name.dashkal.minecraft.hexresearch.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import name.dashkal.minecraft.hexresearch.HexResearch;
import name.dashkal.minecraft.hexresearch.block.CognitiveInducerBlock;
import name.dashkal.minecraft.hexresearch.block.entity.CognitiveInducerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

public class HRBlockEntities {
    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(HexResearch.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static void init() {
        BLOCK_ENTITIES.register();
    }

    public static RegistrySupplier<BlockEntityType<CognitiveInducerBlockEntity>> ARTIFICIAL_MIND = register(
            CognitiveInducerBlock.ID,
            CognitiveInducerBlockEntity::new,
            () -> List.of(HRBlocks.COGNITIVE_INDUCER.get())
    );

    private static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(
            ResourceLocation id,
            BlockEntityType.BlockEntitySupplier<T> blockEntitySupplier,
            Supplier<List<Block>> blocks) {

        // Turns out null works fine here
        //noinspection DataFlowIssue
        Supplier<BlockEntityType<T>> supplier = () -> new BlockEntityType<>(
                blockEntitySupplier,
                new HashSet<>(blocks.get()),
                null
        );

        return BLOCK_ENTITIES.register(id, supplier);
    }
}
