package name.dashkal.minecraft.hexresearch.advancements.trigger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import name.dashkal.minecraft.hexresearch.HexResearch;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.Optional;

/** Advancement trigger that fires when a configured advancement is completed. */
public final class AdvancementTrigger extends SimpleCriterionTrigger<AdvancementTrigger.Instance> {
    public static final ResourceLocation ID = HexResearch.id("advancement");
    public static final String TAG_ADVANCEMENT_ID = "advancement";

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceLocation advancement) {
        HexResearch.LOGGER.debug("AdvancementTrigger.trigger({}, {})", player.getName().getString(), advancement);
        super.trigger(player, instance -> instance.matches(advancement));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            ResourceLocation advancement
    ) implements SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                ResourceLocation.CODEC.fieldOf(TAG_ADVANCEMENT_ID).forGetter(Instance::advancement)
        ).apply(instance, Instance::new));

        public Instance {
            Objects.requireNonNull(player, "player");
            Objects.requireNonNull(advancement, TAG_ADVANCEMENT_ID);
        }

        private boolean matches(ResourceLocation completedAdvancement) {
            HexResearch.LOGGER.debug(
                    "AdvancementTrigger.Instance({}).matches({})",
                    advancement,
                    completedAdvancement
            );
            return advancement.equals(completedAdvancement);
        }
    }
}
