package name.dashkal.minecraft.hexresearch.xplat;

import name.dashkal.minecraft.hexresearch.effect.MindFatigueEffect;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;

import java.util.SortedSet;

/**
 * Interface that houses platform-specific logic.
 * <p>
 * Most of the time this doesn't need to be used directly. Classes with platform specific logic will delegate though
 * this interface to the platform implementations in the forge and fabric modules.
 */
public abstract class XPlatAPI {
    // Initialized in the forge and fabric mod initializers
    protected static XPlatAPI instance = null;

    /** Returns an instance of this API appropriate for the current platform */
    public static XPlatAPI getInstance() { return instance; }

    /** Returns the Mind Fatigue potion effect. */
    public abstract MindFatigueEffect getMindFatigueEffect();

    /**
     * Marks the villager indicating that a cognitive inducer tried to take an impression.
     *
     * @param villager The villager to mark
     * @param gameTime The current game time as per `Level.getGameTime()`
     */
    public abstract void cognitiveInducerMarkVillager(Villager villager, long gameTime);

    /**
     * Returns a sorted set of all marks currently on the given villager.
     *
     * @param villager the villager to check marks for
     * @return a sorted set of all active marks on the villager
     */
    public abstract SortedSet<Long> cognitiveInducerGetMarks(Villager villager);

    /**
     * Prunes the marks on the villager, removing any that should have expired as per the server configuration.
     *
     * @param villager the villager to check marks for
     * @param gameTime the current game time as per `Level.getGameTime()`
     */
    public abstract void cognitiveInducerPruneMarks(Villager villager, long gameTime);

    /** Sends a clientbound payload using the active loader implementation. */
    public abstract void sendPacketToPlayer(ServerPlayer player, CustomPacketPayload payload);
}
