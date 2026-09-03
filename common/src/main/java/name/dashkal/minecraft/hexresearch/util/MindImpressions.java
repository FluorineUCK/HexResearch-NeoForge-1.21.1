package name.dashkal.minecraft.hexresearch.util;

import com.mojang.datafixers.util.Pair;
import name.dashkal.minecraft.hexresearch.HexResearch;
import name.dashkal.minecraft.hexresearch.config.ServerConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class MindImpressions {
    public static final String TAG_PROFESSION = "profession";
    public static final String TAG_BIOME = "biome";
    public static final String TAG_RANK = "rank";
    public static final String TAG_COUNT = "count";

    // List of minds with counts. Used to save to NBT.
    private final Map<Mind, Integer> impressions = new HashMap<>();
    // Map of professions to a map of ranks to impression counts
    private final Map<ResourceLocation, Map<Integer, Integer>> professions = new HashMap<>();
    // Map of biomes (Villager type) to impression counts
    private final Map<ResourceLocation, Integer> biomes = new HashMap<>();

    public Optional<Mind> getImpressedMind() {
        if (impressions.isEmpty()) {
            return Optional.empty();
        }

        // Profession is the one with the most total impressions
        // Since we count an impression as all ranks from 1 to rank inclusive, we can just check rank 1 (Novice) and be sure we got the total
        ResourceLocation profession = getMaxValue(professions.entrySet(), Map.Entry::getKey, e -> e.getValue().getOrDefault(1, 0));

        // Biome is the one with the most impressions
        ResourceLocation biome = getMaxValue(biomes.entrySet(), Map.Entry::getKey, Map.Entry::getValue);

        // Rank is the highest rank of the chosen profession that meets the configured threshold
        ServerConfig.MindTrainingConfig cfg = HexResearch.getServerConfig().mindTrainingConfig();
        List<Map.Entry<Integer, Integer>> filteredRanks = professions.get(profession).entrySet().stream().filter(m ->
                m.getValue() >= cfg.requiredRankImpressions().getOrDefault(m.getKey(), 1)
        ).toList();
        Integer maxRank = getMaxValue(filteredRanks, Map.Entry::getKey, Map.Entry::getKey);

        if (maxRank == null || maxRank <= 0) {
            return Optional.empty();
        } else {
            return Optional.of(new Mind(profession, biome, maxRank));
        }
    }

    public void impressMind(@Nonnull Mind mind) {
        impressMind(mind, 1);
    }

    public void impressMind(@Nonnull Mind mind, @Nonnegative int count) {
        // Remember the impression
        update(impressions, mind, () -> 0, c -> c + count);

        // Update the professions accumulator
        update(professions, mind.profession(), HashMap::new, m -> {
            // An impression of a rank counts as an impression of all lower ranks too
            for (int r = 1; r <= mind.rank(); r++) {
                update(m, r, () -> 0, c -> c + count);
            }
            return m;
        });

        // Update the biomes accumulator
        update(biomes, mind.biome(), () -> 0, c -> c + count);
    }

    public void clear() {
        impressions.clear();
        professions.clear();
        biomes.clear();
    }

    public static MindImpressions load(ListTag impressionsTag) {
        MindImpressions impressions = new MindImpressions();

        for (int i = 0; i < impressionsTag.size(); i++) {
            CompoundTag impressionTag = impressionsTag.getCompound(i);
            ResourceLocation profession = ResourceLocation.parse(impressionTag.getString(TAG_PROFESSION));
            ResourceLocation biome = ResourceLocation.parse(impressionTag.getString(TAG_BIOME));
            int rank = impressionTag.getInt(TAG_RANK);
            int count = impressionTag.getInt(TAG_COUNT);
            impressions.impressMind(new Mind(profession, biome, rank), count);
        }

        return impressions;
    }

    public ListTag save() {
        ListTag impressionsTag = new ListTag();

        for (Map.Entry<Mind, Integer> impression : impressions.entrySet()) {
            CompoundTag impressionTag = new CompoundTag();
            impressionTag.putString(TAG_PROFESSION, impression.getKey().profession().toString());
            impressionTag.putString(TAG_BIOME, impression.getKey().biome().toString());
            impressionTag.putInt(TAG_RANK, impression.getKey().rank());
            impressionTag.putInt(TAG_COUNT, impression.getValue());
            impressionsTag.add(impressionTag);
        }

        return impressionsTag;
    }

    @SuppressWarnings("UnusedReturnValue")
    private <K, V> Map<K, V> update(Map<K, V> map, K k, Supplier<V> init, Function<V, V> update) {
        if (map.containsKey(k)) {
            map.put(k, update.apply(map.get(k)));
        } else {
            map.put(k, update.apply(init.get()));
        }
        return map;
    }

    private <A, B, C extends Comparable<C>> B getMaxValue(Collection<A> from, Function<A, B> extractValue, Function<A, C> extractComparable) {
        Pair<B, C> max = Pair.of(null, null);
        for (A a : from) {
            if (max.getFirst() == null) {
                max = Pair.of(extractValue.apply(a), extractComparable.apply(a));
            } else {
                if (max.getSecond().compareTo(extractComparable.apply(a)) < 0) {
                    max = Pair.of(extractValue.apply(a), extractComparable.apply(a));
                }
            }
        }
        return max.getFirst();
    }
}
