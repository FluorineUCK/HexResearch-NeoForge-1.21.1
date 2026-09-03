package name.dashkal.minecraft.hexresearch.forge.cap;

import name.dashkal.minecraft.hexresearch.HexResearch;
import net.minecraft.world.entity.npc.Villager;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/** Villager-persistent storage for Cognitive Inducer impression marks. */
public final class CognitiveInducerMarksImpl implements ICognitiveInducerMarks {
    private static final String TAG_MARKS = HexResearch.MOD_ID + ":cognitive_inducer_marks";

    private final Villager villager;

    public CognitiveInducerMarksImpl(Villager villager) {
        this.villager = villager;
    }

    @Override
    public void mark(long gameTime) {
        SortedSet<Long> marks = readMarks();
        if (marks.add(gameTime)) {
            writeMarks(marks);
        }
    }

    @Override
    public SortedSet<Long> getMarks() {
        return readMarks();
    }

    @Override
    public void pruneMarks(long gameTime) {
        long expirationTimeSeconds = HexResearch.getServerConfig()
                .mindTrainingConfig()
                .impressionMarkExpirationTimeSeconds();
        long expiration = gameTime - (expirationTimeSeconds * 20L);

        SortedSet<Long> marks = readMarks();
        boolean changed = marks.removeIf(mark -> mark <= expiration);
        if (changed) {
            writeMarks(marks);
        }
    }

    private SortedSet<Long> readMarks() {
        return Arrays.stream(villager.getPersistentData().getLongArray(TAG_MARKS))
                .boxed()
                .collect(java.util.stream.Collectors.toCollection(TreeSet::new));
    }

    private void writeMarks(SortedSet<Long> marks) {
        villager.getPersistentData().putLongArray(
                TAG_MARKS,
                marks.stream().mapToLong(Long::longValue).toArray()
        );
    }
}
