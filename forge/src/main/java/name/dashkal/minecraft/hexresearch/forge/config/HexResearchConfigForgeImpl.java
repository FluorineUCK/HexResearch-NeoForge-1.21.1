package name.dashkal.minecraft.hexresearch.forge.config;

import dev.architectury.platform.Platform;
import name.dashkal.minecraft.hexresearch.HexResearch;
import name.dashkal.minecraft.hexresearch.block.entity.CognitiveInducerBlockEntity;
import name.dashkal.minecraft.hexresearch.config.ClientConfig;
import name.dashkal.minecraft.hexresearch.config.CommonConfig;
import name.dashkal.minecraft.hexresearch.config.HexResearchConfig;
import name.dashkal.minecraft.hexresearch.config.ServerConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Forge implementation of Hex Research configuration
 */
public class HexResearchConfigForgeImpl extends HexResearchConfig {
    public static void init(IEventBus bus, ModContainer container) {
        HexResearchConfigForgeImpl config = new HexResearchConfigForgeImpl(container);
        HexResearchConfig.instance = config;

        bus.addListener(config::onModConfigLoad);
        bus.addListener(config::onModConfigReload);
    }

    private final ServerConfigSpec serverConfigSpec;

    private final AtomicReference<ServerConfig> serverConfig = new AtomicReference<>();

    public HexResearchConfigForgeImpl(ModContainer container) {
        this.serverConfigSpec = new ServerConfigSpec();

        container.registerConfig(ModConfig.Type.SERVER, serverConfigSpec.spec);
    }

    @Nullable
    @Override
    public ClientConfig getClientConfig() {
        return ClientConfig.getDefault();
    }

    @Nonnull
    @Override
    public CommonConfig getCommonConfig() {
        return CommonConfig.getDefault();
    }

    @Nullable
    @Override
    public ServerConfig getServerConfig() {
        return serverConfig.get();
    }

    public void onModConfigLoad(ModConfigEvent.Loading ev) {
        HexResearch.LOGGER.info("onModConfigLoad(): Side = {}, Type = {}", Platform.getEnvironment(), ev.getConfig().getType());
        switch (ev.getConfig().getType()) {
            case CLIENT -> {}
            case COMMON -> {}
            case SERVER -> serverConfig.set(serverConfigSpec.getServerConfig());
            default -> {}
        }
    }

    public void onModConfigReload(ModConfigEvent.Reloading ev) {
        HexResearch.LOGGER.info("onModConfigReload(): Side = {}, Type = {}", Platform.getEnvironment(), ev.getConfig().getType());
        switch (ev.getConfig().getType()) {
            case CLIENT -> {}
            case COMMON -> {}
            case SERVER -> serverConfig.set(serverConfigSpec.getServerConfig());
            default -> {}
        }
    }

    private static class ServerConfigSpec {
        private static final ServerConfig DEFAULT = ServerConfig.getDefault();
        private final ModConfigSpec.IntValue MIND_TRAINING_IMPRESSION_COST_DUST;
        private final ModConfigSpec.IntValue MIND_TRAINING_IMPRESSION_MARK_EXPIRATION_SECONDS;
        private final ModConfigSpec.IntValue MIND_TRAINING_NUM_IMPRESSIONS_NOVICE;
        private final ModConfigSpec.IntValue MIND_TRAINING_NUM_IMPRESSIONS_APPRENTICE;
        private final ModConfigSpec.IntValue MIND_TRAINING_NUM_IMPRESSIONS_JOURNEYMAN;
        private final ModConfigSpec.IntValue MIND_TRAINING_NUM_IMPRESSIONS_EXPERT;
        private final ModConfigSpec.IntValue MIND_TRAINING_NUM_IMPRESSIONS_MASTER;
        private final ModConfigSpec.BooleanValue PATTERNS_FORCE_RECALCULATE_MISSING;

        private final ModConfigSpec spec;

        private ServerConfigSpec() {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

            // Begin Server
            builder.comment("Hex Research Server Configuration").push("server");

            // Begin Mind Training
            builder.comment("Configuration for training minds in the Cognitive Inducer").push("mindTraining");
            builder.comment("Media cost to make a single mind impression in units of amethyst dust");
            MIND_TRAINING_IMPRESSION_COST_DUST = builder.defineInRange("impressionCostDust", DEFAULT.mindTrainingConfig().impressionCostDust(), 0, Integer.MAX_VALUE);

            builder.comment("Time in seconds before an impression mark from the Cognitive Inducer should expire");
            MIND_TRAINING_IMPRESSION_MARK_EXPIRATION_SECONDS = builder.defineInRange(
                    "impressionMarkExpirationTimeSeconds",
                    DEFAULT.mindTrainingConfig().impressionMarkExpirationTimeSeconds(),
                    CognitiveInducerBlockEntity.IMPRESSION_COOLDOWN_SECONDS,
                    Integer.MAX_VALUE
            );

            builder.push("numImpressions");
            builder.comment("Number of impressions required to train a novice mind");
            MIND_TRAINING_NUM_IMPRESSIONS_NOVICE = builder.defineInRange("novice", DEFAULT.mindTrainingConfig().requiredRankImpressions().get(1), 1, Integer.MAX_VALUE);
            builder.comment("Number of impressions required to train an apprentice mind");
            MIND_TRAINING_NUM_IMPRESSIONS_APPRENTICE = builder.defineInRange("apprentice", DEFAULT.mindTrainingConfig().requiredRankImpressions().get(2), 1, Integer.MAX_VALUE);
            builder.comment("Number of impressions required to train a journeyman mind");
            MIND_TRAINING_NUM_IMPRESSIONS_JOURNEYMAN = builder.defineInRange("journeyman", DEFAULT.mindTrainingConfig().requiredRankImpressions().get(3), 1, Integer.MAX_VALUE);
            builder.comment("Number of impressions required to train an expert mind");
            MIND_TRAINING_NUM_IMPRESSIONS_EXPERT = builder.defineInRange("expert", DEFAULT.mindTrainingConfig().requiredRankImpressions().get(4), 1, Integer.MAX_VALUE);
            builder.comment("Number of impressions required to train a master mind");
            MIND_TRAINING_NUM_IMPRESSIONS_MASTER = builder.defineInRange("master", DEFAULT.mindTrainingConfig().requiredRankImpressions().get(5), 1, Integer.MAX_VALUE);
            builder.pop();

            // End Mind Training
            builder.pop();

            // Begin Patterns
            builder.comment("Configuration for pattern logic").push("patterns");
            builder.comment("Force HexCasting to regenerate its pattern registry if our per-world patterns are missing");
            PATTERNS_FORCE_RECALCULATE_MISSING = builder.define("forceRecalculateMissing", DEFAULT.patternConfig().forceRecalculateMissing());
            // End Patterns
            builder.pop();

            // End Server
            builder.pop();
            spec = builder.build();
        }

        public ServerConfig getServerConfig() {
            HexResearch.LOGGER.info("getServerConfig()");
            if (spec.isLoaded()) {
                HexResearch.LOGGER.info("getServerConfig(): Loaded");
                ServerConfig cfg = new ServerConfig(
                        new ServerConfig.MindTrainingConfig(
                                MIND_TRAINING_IMPRESSION_COST_DUST.get(),
                                MIND_TRAINING_IMPRESSION_MARK_EXPIRATION_SECONDS.get(),
                                Map.of(
                                        1, MIND_TRAINING_NUM_IMPRESSIONS_NOVICE.get(),
                                        2, MIND_TRAINING_NUM_IMPRESSIONS_APPRENTICE.get(),
                                        3, MIND_TRAINING_NUM_IMPRESSIONS_JOURNEYMAN.get(),
                                        4, MIND_TRAINING_NUM_IMPRESSIONS_EXPERT.get(),
                                        5, MIND_TRAINING_NUM_IMPRESSIONS_MASTER.get()
                                )
                        ),
                        new ServerConfig.PatternConfig(
                                PATTERNS_FORCE_RECALCULATE_MISSING.get()
                        )
                );
                HexResearch.LOGGER.info("getServerConfig(): {}", cfg);
                return cfg;
            } else {
                return null;
            }
        }
    }
}
