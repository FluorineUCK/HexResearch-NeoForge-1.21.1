package name.dashkal.minecraft.hexresearch.forge.probe;

import at.petrak.hexcasting.common.entities.EntityWallScroll;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.client.particles.ConjureParticle;
import name.dashkal.minecraft.hexresearch.HexResearch;
import name.dashkal.minecraft.hexresearch.block.entity.CognitiveInducerBlockEntity;
import name.dashkal.minecraft.hexresearch.network.MindImpressionPacket;
import name.dashkal.minecraft.hexresearch.network.ScrollSyncPacket;
import name.dashkal.minecraft.hexresearch.registry.HRBlocks;
import name.dashkal.minecraft.hexresearch.xplat.XPlatAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import vazkii.patchouli.client.book.BookContents;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/** Opt-in client validation for the translation keys used by the injected Patchouli entries. */
public final class HexResearchClientValidation {
    private static final String VALIDATE_PROPERTY = "hexresearch.probe.validatePatchouliLang";
    private static final String EXIT_PROPERTY = "hexresearch.probe.exitAfterClientStartup";
    private static final String REQUIRE_WORLD_PROPERTY = "hexresearch.probe.requireClientWorld";
    private static final String NETWORK_DISPATCH_PROPERTY = "hexresearch.probe.validateNetworkDispatch";
    private static final String NETWORK_MARKER = "hexresearch-network-probe";
    private static final int VALIDATION_TICK = 160;
    private static final int WORLD_TIMEOUT_TICK =
        Integer.getInteger("hexresearch.probe.worldTimeoutTicks", 1_200);
    private static final String QUICK_PLAY_WORLD = "HexResearchProbePre2";
    private static final ResourceLocation HEX_BOOK =
        ResourceLocation.fromNamespaceAndPath("hexcasting", "thehexbook");
    private static final Set<ResourceLocation> PATCHOULI_ENTRIES = Set.of(
        bookEntry("greatwork/enlightened_thought_sieve"),
        bookEntry("items/cognitive_inducer"),
        bookEntry("patterns/villager_actions"),
        bookEntry("patterns/great_spells/imbue_mind"),
        bookEntry("patterns/spells/thought_sieve")
    );
    private static final List<String> PATCHOULI_KEYS = List.of(
        "hexresearch.entry.greatwork.on_thought_sifting",
        "hexresearch.entry.imbue_mind",
        "hexresearch.entry.items.cognitive_inducer",
        "hexresearch.entry.thought_sieve",
        "hexresearch.entry.villager_actions",
        "hexresearch.page.greatwork.on_thought_sifting.1",
        "hexresearch.page.greatwork.on_thought_sifting.2",
        "hexresearch.page.items.cognitive_inducer.1",
        "hexresearch.page.items.cognitive_inducer.2",
        "hexresearch.page.items.cognitive_inducer.3",
        "hexresearch.page.items.cognitive_inducer.4",
        "hexresearch.page.spells.imbue_mind.1",
        "hexresearch.page.spells.imbue_mind.2",
        "hexresearch.page.thought_sieve.intro.1",
        "hexresearch.page.thought_sieve.intro.2",
        "hexresearch.page.thought_sieve.spell.1",
        "hexresearch.page.thought_sieve.spell.2",
        "hexresearch.page.thought_sieve.spell.3",
        "hexresearch.page.villager_actions.intro.1",
        "hexresearch.page.villager_actions.villager_popularity",
        "hexresearch.page.villager_actions.villager_rank",
        "hexcasting.action.hexresearch:thought_sieve",
        "hexcasting.action.hexresearch:imbue_mind",
        "hexcasting.action.hexresearch:villager_rank",
        "hexcasting.action.hexresearch:villager_popularity",
        "hexcasting.action.book.hexresearch:thought_sieve",
        "hexcasting.action.book.hexresearch:imbue_mind",
        "hexcasting.action.book.hexresearch:villager_rank",
        "hexcasting.action.book.hexresearch:villager_popularity"
    );

    private static int ticks;
    private static boolean registered;
    private static boolean finished;
    private static boolean networkDispatchVerified;
    private static volatile int expectedScrollEntityId = -1;
    private static volatile int expectedVillagerEntityId = -1;
    private static volatile boolean networkDispatchSent;
    private static volatile String networkDispatchFailure;
    private static volatile boolean clientEntityReady;
    private static volatile UUID pendingPlayerId;
    private static volatile BlockPos pendingInducerPosition;
    private static volatile ItemStack pendingUpdatedScroll;
    private static int initialConjureParticleCount = -1;
    private static int latestConjureParticleCount = -1;
    private static boolean mindImpressionEffectObserved;

    private HexResearchClientValidation() {
    }

    public static void register() {
        if (!registered && (
            Boolean.getBoolean(VALIDATE_PROPERTY)
                || Boolean.getBoolean(EXIT_PROPERTY)
                || Boolean.getBoolean(NETWORK_DISPATCH_PROPERTY)
        )) {
            registered = true;
            NeoForge.EVENT_BUS.addListener(HexResearchClientValidation::onClientTick);
            if (Boolean.getBoolean(NETWORK_DISPATCH_PROPERTY)) {
                NeoForge.EVENT_BUS.addListener(HexResearchClientValidation::onPlayerLoggedIn);
                NeoForge.EVENT_BUS.addListener(HexResearchClientValidation::onServerTick);
            }
        }
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!Boolean.getBoolean(NETWORK_DISPATCH_PROPERTY)
            || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        try {
            ServerLevel level = player.serverLevel();
            ItemScroll scrollItem = HexItems.SCROLL_SMOL.get();
            BlockPos scrollPosition = player.blockPosition().above(2);
            EntityWallScroll wallScroll = new EntityWallScroll(
                level,
                scrollPosition,
                Direction.NORTH,
                new ItemStack(scrollItem),
                false,
                scrollItem.blockSize
            );
            if (!level.addFreshEntity(wallScroll)) {
                throw new IllegalStateException("Could not spawn network probe wall scroll");
            }

            BlockPos inducerPosition = player.blockPosition().offset(2, 0, 0);
            level.setBlockAndUpdate(
                inducerPosition,
                HRBlocks.COGNITIVE_INDUCER.get().defaultBlockState()
            );
            if (!(level.getBlockEntity(inducerPosition) instanceof CognitiveInducerBlockEntity)) {
                throw new IllegalStateException(
                    "Could not place network probe cognitive inducer at " + inducerPosition
                );
            }

            Villager villager = EntityType.VILLAGER.create(level);
            if (villager == null) {
                throw new IllegalStateException("Could not create network probe villager");
            }
            villager.moveTo(
                inducerPosition.getX() + 0.5,
                inducerPosition.getY(),
                inducerPosition.getZ() + 2.5,
                0.0F,
                0.0F
            );
            if (!level.addFreshEntity(villager)) {
                throw new IllegalStateException("Could not spawn network probe villager");
            }

            ItemStack updatedScroll = new ItemStack(scrollItem);
            updatedScroll.set(DataComponents.CUSTOM_NAME, Component.literal(NETWORK_MARKER));
            pendingPlayerId = player.getUUID();
            pendingInducerPosition = inducerPosition;
            pendingUpdatedScroll = updatedScroll;
            expectedVillagerEntityId = villager.getId();
            expectedScrollEntityId = wallScroll.getId();
            HexResearch.LOGGER.info(
                "[HEXRESEARCH-PROBE] payload_dispatch_prepared=PASS "
                    + "scroll_entity_id={} villager_entity_id={} inducer_pos={}",
                expectedScrollEntityId,
                expectedVillagerEntityId,
                pendingInducerPosition
            );
        } catch (Throwable throwable) {
            networkDispatchFailure = throwable.toString();
            HexResearch.LOGGER.error(
                "[HEXRESEARCH-PROBE] payload_dispatch_sent=FAIL",
                throwable
            );
        }
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        if (!Boolean.getBoolean(NETWORK_DISPATCH_PROPERTY)
            || networkDispatchSent
            || networkDispatchFailure != null
            || !clientEntityReady) {
            return;
        }

        try {
            ServerPlayer player = pendingPlayerId == null
                ? null
                : event.getServer().getPlayerList().getPlayer(pendingPlayerId);
            if (player == null) {
                throw new IllegalStateException("Network probe player is no longer online");
            }
            Entity entity = player.serverLevel().getEntity(expectedScrollEntityId);
            if (!(entity instanceof EntityWallScroll)) {
                throw new IllegalStateException(
                    "Server wall scroll disappeared before payload dispatch: " + expectedScrollEntityId
                );
            }

            XPlatAPI.getInstance().sendPacketToPlayer(
                player,
                new ScrollSyncPacket(expectedScrollEntityId, pendingUpdatedScroll)
            );
            XPlatAPI.getInstance().sendPacketToPlayer(
                player,
                new MindImpressionPacket(
                    player.serverLevel().dimension().location(),
                    expectedVillagerEntityId,
                    pendingInducerPosition,
                    false
                )
            );
            networkDispatchSent = true;
            HexResearch.LOGGER.info(
                "[HEXRESEARCH-PROBE] payload_dispatch_sent=PASS scroll_entity_id={} "
                    + "client_entity_ready=true payloads=2",
                expectedScrollEntityId
            );
        } catch (Throwable throwable) {
            networkDispatchFailure = throwable.toString();
            HexResearch.LOGGER.error(
                "[HEXRESEARCH-PROBE] payload_dispatch_sent=FAIL",
                throwable
            );
        }
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        if (finished) {
            return;
        }
        ticks++;
        detectClientEntityReady();
        observeMindImpressionEffect();

        boolean requireWorld = Boolean.getBoolean(REQUIRE_WORLD_PROPERTY);
        if (ticks < VALIDATION_TICK || (requireWorld && Minecraft.getInstance().level == null)) {
            if (requireWorld && ticks >= WORLD_TIMEOUT_TICK) {
                finished = true;
                Minecraft client = Minecraft.getInstance();
                Path expectedWorld = client.getLevelSource().getLevelPath(QUICK_PLAY_WORLD);
                HexResearch.LOGGER.error(
                    "[HEXRESEARCH-PROBE] patchouli_lang=FAIL patchouli_book=FAIL "
                        + "client_world=FAIL timeout_ticks={} screen={} saves={} "
                        + "expected_world={} expected_world_is_directory={}",
                    ticks,
                    client.screen == null ? "null" : client.screen.getClass().getName(),
                    client.getLevelSource().getBaseDir(),
                    expectedWorld,
                    Files.isDirectory(expectedWorld)
                );
                if (Boolean.getBoolean(EXIT_PROPERTY)) {
                    Minecraft.getInstance().stop();
                }
            }
            return;
        }

        if (Boolean.getBoolean(NETWORK_DISPATCH_PROPERTY) && !networkDispatchVerified) {
            if (networkDispatchFailure != null) {
                finished = true;
                HexResearch.LOGGER.error(
                    "[HEXRESEARCH-PROBE] payload_dispatch=FAIL reason={}",
                    networkDispatchFailure
                );
                if (Boolean.getBoolean(EXIT_PROPERTY)) {
                    Minecraft.getInstance().stop();
                }
                return;
            }

            if (!networkDispatchSent
                || !hasReceivedScrollSync()
                || !mindImpressionEffectObserved) {
                if (ticks >= WORLD_TIMEOUT_TICK) {
                    finished = true;
                    HexResearch.LOGGER.error(
                        "[HEXRESEARCH-PROBE] payload_dispatch=FAIL sent={} entity_id={} "
                            + "timeout_ticks={} final_entity_state={} "
                            + "mind_effect={} particle_baseline={} particle_latest={}",
                        networkDispatchSent,
                        expectedScrollEntityId,
                        ticks,
                        describeScrollEntityState(),
                        mindImpressionEffectObserved,
                        initialConjureParticleCount,
                        latestConjureParticleCount
                    );
                    if (Boolean.getBoolean(EXIT_PROPERTY)) {
                        Minecraft.getInstance().stop();
                    }
                }
                return;
            }

            networkDispatchVerified = true;
            HexResearch.LOGGER.info(
                "[HEXRESEARCH-PROBE] payload_dispatch=PASS scroll_sync=PASS "
                    + "mind_impression_dispatch=PASS mind_impression_effect=PASS payloads=2"
            );
        }

        if (Boolean.getBoolean(VALIDATE_PROPERTY)) {
            try {
                List<String> missing = PATCHOULI_KEYS.stream().filter(key -> !I18n.exists(key)).toList();
                if (!missing.isEmpty()) {
                    throw new IllegalStateException("Missing translations: " + String.join(",", missing));
                }

                Book book = BookRegistry.INSTANCE.books.get(HEX_BOOK);
                if (book == null) {
                    throw new IllegalStateException("Hex Casting book was not loaded");
                }
                BookContents contents = book.getContents();
                if (contents == null) {
                    throw new IllegalStateException("Hex Casting book contents are null");
                }
                if (contents.isErrored()) {
                    throw new IllegalStateException("Patchouli failed to build " + HEX_BOOK, contents.getException());
                }
                Set<ResourceLocation> missingEntries = new LinkedHashSet<>(PATCHOULI_ENTRIES);
                missingEntries.removeAll(contents.entries.keySet());
                if (!missingEntries.isEmpty()) {
                    throw new IllegalStateException("Missing Patchouli entries: " + missingEntries);
                }
                List<ResourceLocation> emptyEntries = PATCHOULI_ENTRIES.stream()
                    .filter(id -> contents.entries.get(id).getPages().isEmpty())
                    .toList();
                if (!emptyEntries.isEmpty()) {
                    throw new IllegalStateException("Patchouli entries without pages: " + emptyEntries);
                }

                HexResearch.LOGGER.info(
                    "[HEXRESEARCH-PROBE] patchouli_lang=PASS patchouli_book=PASS "
                        + "locale={} keys={} entries={} errored=false",
                    Minecraft.getInstance().getLanguageManager().getSelected(),
                    PATCHOULI_KEYS.size(),
                    PATCHOULI_ENTRIES.size()
                );
            } catch (Throwable throwable) {
                HexResearch.LOGGER.error("[HEXRESEARCH-PROBE] patchouli_lang=FAIL patchouli_book=FAIL", throwable);
            }
        }

        finished = true;
        if (Boolean.getBoolean(EXIT_PROPERTY)) {
            Minecraft.getInstance().stop();
        }
    }

    private static void detectClientEntityReady() {
        if (clientEntityReady || expectedScrollEntityId < 0) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.level != null
            && client.level.getEntity(expectedScrollEntityId) instanceof EntityWallScroll
            && client.level.getEntity(expectedVillagerEntityId) instanceof Villager
            && client.level.getBlockEntity(pendingInducerPosition)
                instanceof CognitiveInducerBlockEntity) {
            initialConjureParticleCount = countConjureParticles();
            latestConjureParticleCount = initialConjureParticleCount;
            clientEntityReady = true;
            HexResearch.LOGGER.info(
                "[HEXRESEARCH-PROBE] payload_client_fixtures_ready=PASS "
                    + "scroll_entity_id={} villager_entity_id={} inducer=PASS client_tick={}",
                expectedScrollEntityId,
                expectedVillagerEntityId,
                ticks
            );
        }
    }

    private static void observeMindImpressionEffect() {
        if (initialConjureParticleCount < 0 || mindImpressionEffectObserved) {
            return;
        }
        latestConjureParticleCount = countConjureParticles();
        if (latestConjureParticleCount > initialConjureParticleCount) {
            mindImpressionEffectObserved = true;
            HexResearch.LOGGER.info(
                "[HEXRESEARCH-PROBE] mind_impression_effect=PASS "
                    + "particle_baseline={} particle_observed={}",
                initialConjureParticleCount,
                latestConjureParticleCount
            );
        }
    }

    private static int countConjureParticles() {
        AtomicInteger count = new AtomicInteger();
        Minecraft.getInstance().particleEngine.iterateParticles(particle -> {
            if (particle instanceof ConjureParticle) {
                count.incrementAndGet();
            }
        });
        return count.get();
    }

    private static boolean hasReceivedScrollSync() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || expectedScrollEntityId < 0) {
            return false;
        }
        Entity entity = client.level.getEntity(expectedScrollEntityId);
        if (!(entity instanceof EntityWallScroll wallScroll)) {
            return false;
        }
        Component marker = wallScroll.scroll.get(DataComponents.CUSTOM_NAME);
        return marker != null && NETWORK_MARKER.equals(marker.getString());
    }

    private static String describeScrollEntityState() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return "level-missing";
        }
        Entity entity = client.level.getEntity(expectedScrollEntityId);
        if (!(entity instanceof EntityWallScroll wallScroll)) {
            return entity == null ? "entity-missing" : entity.getClass().getName();
        }
        Component marker = wallScroll.scroll.get(DataComponents.CUSTOM_NAME);
        return "wall-scroll(marker=" + (marker == null ? "null" : marker.getString()) + ")";
    }

    private static ResourceLocation bookEntry(String path) {
        return ResourceLocation.fromNamespaceAndPath("hexcasting", path);
    }
}
