package name.dashkal.minecraft.hexresearch.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Random;

/**
 * A fabricated villager mind. Used by the Cognitive Inducer block and the Imbue Mind spell.
 * @param profession the profession of the villager.
 * @param biome the type of the villager.
 * @param rank the rank of the villager. Should be in the range 1..5 inclusive
 */
public record Mind(@Nonnull ResourceLocation profession, @Nonnull ResourceLocation biome, @Nonnegative int rank) {
    public static Mind fromVillager(@Nonnull Villager villager) {
        return new Mind(
                BuiltInRegistries.VILLAGER_PROFESSION.getKey(villager.getVillagerData().getProfession()),
                BuiltInRegistries.VILLAGER_TYPE.getKey(villager.getVillagerData().getType()),
                villager.getVillagerData().getLevel()
        );
    }

    /** Returns a combined `Component` describing the mind's profession and rank. */
    public Component toComponent() {
        return Component.translatable("hexresearch.misc.overlay.mind", getRankComponent(), getProfessionComponent());
    }

    /** Returns a translated `Component` for the mind's profession. */
    public Component getProfessionComponent() {
        return Component.translatable(EntityType.VILLAGER.getDescriptionId() + "." + profession.getPath());
    }

    /** Returns a translated `Component` for the mind's biome. */
    public Component getBiomeComponent() {
        return Component.translatable(String.format("biome.%s.%s", biome.getNamespace(), biome.getPath()));
    }

    /** Returns a translated `Component` for the mind's rank as a merchant level. */
    public Component getRankComponent() {
        return Component.translatable("merchant.level." + rank);
    }

    /** Returns a color gradient based on the villager's profession and biome. */
    public int[] getColorGradient() {
        // Gradient is from a random color based on profession to a random color based on biome
        // TODO consider hardcoding known professions
        // TODO consider making the biome color based on its gradient map

        return new int[]{ hashToColor(profession), hashToColor(biome) };
    }

    /** Generate a random color from the hash of the provided object. */
    private static int hashToColor(Object any) {
        Random r = new Random(any.hashCode());
        float h = r.nextFloat();
        float s = r.nextFloat(0.4f, 1.0f);
        float b = r.nextFloat(0.5f, 1.0f);
        return Color.HSBtoRGB(h, s, b);
    }
}
