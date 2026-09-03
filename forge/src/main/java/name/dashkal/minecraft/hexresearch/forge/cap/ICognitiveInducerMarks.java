package name.dashkal.minecraft.hexresearch.forge.cap;

import net.minecraft.world.entity.npc.Villager;

import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.function.Function;

/** Persistent Cognitive Inducer marks attached to a villager's NeoForge entity data. */
public interface ICognitiveInducerMarks {
    static void with(Villager villager, Consumer<ICognitiveInducerMarks> consumer) {
        consumer.accept(new CognitiveInducerMarksImpl(villager));
    }

    static <T> T withF(Villager villager, Function<ICognitiveInducerMarks, T> function) {
        return function.apply(new CognitiveInducerMarksImpl(villager));
    }

    void mark(long gameTime);

    SortedSet<Long> getMarks();

    void pruneMarks(long gameTime);
}
