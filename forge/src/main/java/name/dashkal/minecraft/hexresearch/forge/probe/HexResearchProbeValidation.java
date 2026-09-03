package name.dashkal.minecraft.hexresearch.forge.probe;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.SpellAction;
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.common.lib.HexItems;
import io.netty.buffer.Unpooled;
import name.dashkal.minecraft.hexresearch.HexResearch;
import name.dashkal.minecraft.hexresearch.casting.patterns.spells.OpThoughtSieve;
import name.dashkal.minecraft.hexresearch.hexcompat.PerWorldPatternResolver;
import name.dashkal.minecraft.hexresearch.hexcompat.ScrollPatternReader;
import name.dashkal.minecraft.hexresearch.network.MindImpressionPacket;
import name.dashkal.minecraft.hexresearch.network.ScrollSyncPacket;
import name.dashkal.minecraft.hexresearch.registry.HRHexPatterns;
import name.dashkal.minecraft.hexresearch.util.HexPatternMatch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.List;

/** Opt-in integrated validation used by the port's dedicated-server smoke test. */
public final class HexResearchProbeValidation {
    private static final String PROPERTY = "hexresearch.probe.validatePerWorldPatternResolver";

    private HexResearchProbeValidation() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(HexResearchProbeValidation::onServerStarted);
    }

    public static void onServerStarted(ServerStartedEvent event) {
        if (!Boolean.getBoolean(PROPERTY)) {
            return;
        }

        try {
            ServerLevel level = event.getServer().overworld();
            Registry<ActionRegistryEntry> registry = level.registryAccess().registryOrThrow(HexRegistries.ACTION);
            int taggedCount = 0;
            int externalCount = 0;
            ResourceKey<ActionRegistryEntry> testedKey = null;
            HexPattern testedPattern = null;

            for (Holder<ActionRegistryEntry> holder : registry.getTagOrEmpty(HexTags.Actions.PER_WORLD_PATTERN)) {
                ResourceKey<ActionRegistryEntry> key = holder.unwrapKey().orElse(null);
                if (key == null) {
                    continue;
                }
                HexPattern canonical = PatternRegistryManifest.getCanonicalStrokesPerWorld(key, level);
                if (canonical == null) {
                    continue;
                }
                taggedCount++;
                if (!HexResearch.MOD_ID.equals(key.location().getNamespace())) {
                    externalCount++;
                    if (testedPattern == null) {
                        testedKey = key;
                        testedPattern = canonical;
                    }
                }
            }

            HexPattern resolved = testedPattern == null ? null : PerWorldPatternResolver.find(level, testedPattern);
            ServerLevel alternateLevel = event.getServer().getLevel(Level.NETHER);
            HexPattern crossDimensionResolved = testedPattern == null || alternateLevel == null
                ? null
                : PerWorldPatternResolver.find(alternateLevel, testedPattern);
            ItemStack crossDimensionScroll = testedKey == null || alternateLevel == null
                ? ItemStack.EMPTY
                : ItemScroll.withPerWorldPattern(new ItemStack(HexItems.SCROLL_SMOL.get()), testedKey);
            HexPattern crossDimensionRead = crossDimensionScroll.isEmpty()
                ? null
                : ScrollPatternReader.read(crossDimensionScroll, alternateLevel);
            boolean crossDimension = crossDimensionResolved != null
                && crossDimensionRead != null
                && HexPatternMatch.shapeMatches(testedPattern, crossDimensionResolved)
                && HexPatternMatch.shapeMatches(testedPattern, crossDimensionRead);
            boolean payloadCodecs = testedKey != null && validatePayloadCodecs(level, testedKey);
            ScrollValidation scrollValidation = testedPattern == null
                ? ScrollValidation.failed()
                : validateScrollExecution(level, testedKey, testedPattern);
            boolean passed = taggedCount > 0
                && externalCount > 0
                && resolved != null
                && HexPatternMatch.shapeMatches(testedPattern, resolved)
                && crossDimension
                && payloadCodecs
                && scrollValidation.passed();

            if (passed) {
                HexResearch.LOGGER.info(
                    "[HEXRESEARCH-PROBE] per_world_pattern_resolver=PASS scroll_read=PASS "
                        + "item_entity=PASS wall_scroll=PASS written_scroll=PASS wall_persistence=PASS "
                        + "alternate_strokes=PASS legacy_scroll=PASS cross_dimension=PASS payload_codecs=PASS "
                        + "tagged={} external={} tested_key={} "
                        + "item_cost={} wall_cost={}",
                    taggedCount,
                    externalCount,
                    testedKey.location(),
                    scrollValidation.itemCost(),
                    scrollValidation.wallCost()
                );
            } else {
                HexResearch.LOGGER.error(
                    "[HEXRESEARCH-PROBE] per_world_pattern_resolver=FAIL tagged={} external={} tested_key={} "
                        + "resolved={} scroll_read={} item_entity={} wall_scroll={} written_scroll={} "
                        + "wall_persistence={} alternate_strokes={} legacy_scroll={} cross_dimension={} "
                        + "payload_codecs={} item_cost={} wall_cost={}",
                    taggedCount,
                    externalCount,
                    testedKey == null ? "none" : testedKey.location(),
                    resolved,
                    scrollValidation.itemRoundTrip(),
                    scrollValidation.itemEntityExecution(),
                    scrollValidation.wallScrollExecution(),
                    scrollValidation.writtenScrollRead(),
                    scrollValidation.wallPersistence(),
                    scrollValidation.alternateStrokes(),
                    scrollValidation.legacyScrollRead(),
                    crossDimension,
                    payloadCodecs,
                    scrollValidation.itemCost(),
                    scrollValidation.wallCost()
                );
            }
        } catch (Throwable throwable) {
            HexResearch.LOGGER.error("[HEXRESEARCH-PROBE] per_world_pattern_resolver=FAIL exception", throwable);
        } finally {
            event.getServer().halt(false);
        }
    }

    private static boolean validatePayloadCodecs(
        ServerLevel level,
        ResourceKey<ActionRegistryEntry> patternKey
    ) {
        ItemStack scroll = ItemScroll.withPerWorldPattern(new ItemStack(HexItems.SCROLL_SMOL.get()), patternKey);
        ScrollSyncPacket scrollPayload = new ScrollSyncPacket(37, scroll);
        ScrollSyncPacket decodedScroll;
        RegistryFriendlyByteBuf scrollBuffer =
            new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
        try {
            ScrollSyncPacket.STREAM_CODEC.encode(scrollBuffer, scrollPayload);
            decodedScroll = ScrollSyncPacket.STREAM_CODEC.decode(scrollBuffer);
        } finally {
            scrollBuffer.release();
        }

        BlockPos inducerPos = level.getSharedSpawnPos().above();
        MindImpressionPacket impressionPayload = new MindImpressionPacket(
            level.dimension().location(),
            41,
            inducerPos,
            true
        );
        MindImpressionPacket decodedImpression;
        RegistryFriendlyByteBuf impressionBuffer =
            new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
        try {
            MindImpressionPacket.STREAM_CODEC.encode(impressionBuffer, impressionPayload);
            decodedImpression = MindImpressionPacket.STREAM_CODEC.decode(impressionBuffer);
        } finally {
            impressionBuffer.release();
        }

        return decodedScroll.getEntityId() == scrollPayload.getEntityId()
            && ItemStack.isSameItemSameComponents(decodedScroll.getNewScroll(), scrollPayload.getNewScroll())
            && decodedScroll.getNewScroll().getCount() == scrollPayload.getNewScroll().getCount()
            && decodedImpression.getDimensionId().equals(impressionPayload.getDimensionId())
            && decodedImpression.getEntityId() == impressionPayload.getEntityId()
            && decodedImpression.getInducerPos().equals(impressionPayload.getInducerPos())
            && decodedImpression.isSuccessful() == impressionPayload.isSuccessful();
    }

    /** Exercises the same public operation path used by a player for both supported scroll entity forms. */
    private static ScrollValidation validateScrollExecution(
        ServerLevel level,
        ResourceKey<ActionRegistryEntry> patternKey,
        HexPattern pattern
    ) {
        ItemScroll scrollItem = HexItems.SCROLL_SMOL.get();
        // Loot-generated ancient scrolls initially carry only ACTION. Hex Casting fills PATTERN
        // lazily from an inventory tick, so a dropped or immediately placed scroll exercises a
        // different path from writeDatum and reproduces the real player-reported failure.
        ItemStack scrollStack = ItemScroll.withPerWorldPattern(new ItemStack(scrollItem), patternKey);

        HexPattern read = ScrollPatternReader.read(scrollStack, level);
        boolean itemRoundTrip = read != null && HexPatternMatch.shapeMatches(pattern, read);

        // Old HexResearch/Hex Casting worlds and saved creative hotbars store the scroll payload in
        // the former item tag. Minecraft 1.21 upgrades that opaque mod tag to CUSTOM_DATA, so exercise
        // the exact 0.10.x format here instead of assuming every player stack was freshly created.
        CompoundTag legacyPatternTag = new CompoundTag();
        legacyPatternTag.putByte("start_dir", (byte) pattern.getStartDir().ordinal());
        byte[] legacyAngles = new byte[pattern.getAngles().size()];
        for (int index = 0; index < pattern.getAngles().size(); index++) {
            legacyAngles[index] = (byte) pattern.getAngles().get(index).ordinal();
        }
        legacyPatternTag.putByteArray("angles", legacyAngles);
        CompoundTag legacyCustomData = new CompoundTag();
        legacyCustomData.put("pattern", legacyPatternTag);
        legacyCustomData.putString("op_id", patternKey.location().toString());
        legacyCustomData.putString("hexresearch_probe_marker", "preserve");
        ItemStack legacyStack = new ItemStack(scrollItem);
        legacyStack.set(DataComponents.CUSTOM_DATA, CustomData.of(legacyCustomData));
        HexPattern legacyPattern = ScrollPatternReader.read(legacyStack, level);
        HexPattern migratedPattern = legacyStack.get(HexDataComponents.PATTERN.get());
        ResourceKey<ActionRegistryEntry> migratedAction = legacyStack.get(HexDataComponents.ACTION.get());
        CustomData migratedCustomData = legacyStack.get(DataComponents.CUSTOM_DATA);
        CompoundTag migratedCustomTag = migratedCustomData == null ? null : migratedCustomData.copyTag();
        HexPattern rereadLegacyPattern = ScrollPatternReader.read(legacyStack, level);
        boolean legacyScrollRead = legacyPattern != null
            && HexPatternMatch.shapeMatches(pattern, legacyPattern)
            && migratedPattern != null
            && HexPatternMatch.shapeMatches(pattern, migratedPattern)
            && patternKey.equals(migratedAction)
            && migratedCustomTag != null
            && "preserve".equals(migratedCustomTag.getString("hexresearch_probe_marker"))
            && !migratedCustomTag.contains("pattern")
            && !migratedCustomTag.contains("op_id")
            && rereadLegacyPattern != null
            && HexPatternMatch.shapeMatches(pattern, rereadLegacyPattern);

        // A normal player-authored scroll stores PATTERN directly. Use HexResearch's own prototype
        // and compare it to this world's alternate drawing of Imbue Mind: this is the actual feature
        // seam Thought Sieve promises, rather than merely re-reading an already canonical scroll.
        ItemStack writtenStack = new ItemStack(scrollItem);
        scrollItem.writeDatum(writtenStack, new PatternIota(HRHexPatterns.IMBUE_MIND));
        HexPattern writtenPattern = ScrollPatternReader.read(writtenStack, level);
        ResourceKey<ActionRegistryEntry> imbueMindKey = ResourceKey.create(
            HexRegistries.ACTION,
            HexResearch.id("imbue_mind")
        );
        HexPattern imbueMindCanonical = PatternRegistryManifest.getCanonicalStrokesPerWorld(imbueMindKey, level);
        boolean writtenScrollRead = writtenPattern != null;
        boolean alternateStrokes = writtenPattern != null
            && imbueMindCanonical != null
            && HexPatternMatch.shapeMatches(writtenPattern, imbueMindCanonical);

        // ServerStartedEvent does not make arbitrary chunks entity-accessible.
        // Use the prepared spawn chunk so EntityIota resolves through the same
        // visible entity index as it does during a real player cast.
        BlockPos probeOrigin = level.getSharedSpawnPos().above(2);
        level.getChunkAt(probeOrigin);
        double probeX = probeOrigin.getX() + 0.5;
        double probeY = probeOrigin.getY();
        double probeZ = probeOrigin.getZ() + 0.5;
        ServerPlayer player = FakePlayerFactory.getMinecraft(level);
        player.setPos(probeX, probeY, probeZ);
        Villager villager = new Villager(EntityType.VILLAGER, level);
        villager.setPos(probeX, probeY, probeZ);
        ItemEntity itemEntity = new ItemEntity(level, probeX + 1.0, probeY, probeZ, scrollStack.copy());
        EntityWallScroll wallScroll = new EntityWallScroll(
            level,
            probeOrigin.offset(2, 0, 0),
            Direction.NORTH,
            scrollStack.copy(),
            false,
            scrollItem.blockSize
        );
        CompoundTag savedWallScroll = new CompoundTag();
        wallScroll.save(savedWallScroll);
        EntityWallScroll loadedWallScroll = EntityType.loadEntityRecursive(savedWallScroll, level, entity -> entity)
            instanceof EntityWallScroll loaded ? loaded : null;
        HexPattern loadedWallPattern = loadedWallScroll == null
            ? null
            : ScrollPatternReader.read(loadedWallScroll.scroll, level);
        boolean wallPersistence = loadedWallPattern != null
            && HexPatternMatch.shapeMatches(pattern, loadedWallPattern);

        boolean villagerAdded = level.addFreshEntity(villager);
        boolean itemEntityAdded = level.addFreshEntity(itemEntity);
        boolean wallScrollAdded = level.addFreshEntity(wallScroll);
        long itemCost = -1L;
        long wallCost = -1L;
        boolean itemEntityExecution = false;
        boolean wallScrollExecution = false;

        try {
            StaffCastEnv environment = new StaffCastEnv(player, InteractionHand.MAIN_HAND);
            OpThoughtSieve operation = new OpThoughtSieve();
            if (villagerAdded && itemEntityAdded) {
                SpellAction.Result result = operation.execute(
                    List.<Iota>of(new EntityIota(villager), new EntityIota(itemEntity)),
                    environment
                );
                itemCost = result.getCost();
                itemEntityExecution = result.getEffect() != null && itemCost == operation.getCost();
            }
            if (villagerAdded && wallScrollAdded) {
                SpellAction.Result result = operation.execute(
                    List.<Iota>of(new EntityIota(villager), new EntityIota(wallScroll)),
                    environment
                );
                wallCost = result.getCost();
                wallScrollExecution = result.getEffect() != null && wallCost == operation.getCost();
            }
        } finally {
            wallScroll.discard();
            itemEntity.discard();
            villager.discard();
        }

        return new ScrollValidation(
            itemRoundTrip
                && itemEntityExecution
                && wallScrollExecution
                && writtenScrollRead
                && wallPersistence
                && alternateStrokes
                && legacyScrollRead,
            itemRoundTrip,
            itemEntityExecution,
            wallScrollExecution,
            writtenScrollRead,
            wallPersistence,
            alternateStrokes,
            legacyScrollRead,
            itemCost,
            wallCost
        );
    }

    private record ScrollValidation(
        boolean passed,
        boolean itemRoundTrip,
        boolean itemEntityExecution,
        boolean wallScrollExecution,
        boolean writtenScrollRead,
        boolean wallPersistence,
        boolean alternateStrokes,
        boolean legacyScrollRead,
        long itemCost,
        long wallCost
    ) {
        private static ScrollValidation failed() {
            return new ScrollValidation(false, false, false, false, false, false, false, false, -1L, -1L);
        }
    }
}
