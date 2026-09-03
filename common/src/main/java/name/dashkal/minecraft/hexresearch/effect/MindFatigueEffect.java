package name.dashkal.minecraft.hexresearch.effect;

import name.dashkal.minecraft.hexresearch.HexResearch;
import name.dashkal.minecraft.hexresearch.registry.HREffects;
import name.dashkal.minecraft.hexresearch.xplat.XPlatAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Marker potion effect used to indicate "mental fatigue".  Attempting to Thought Sieve a villager who has this
 * effect active will cause additional mind harm.
 */
public abstract class MindFatigueEffect extends MobEffect {
    public static MindFatigueEffect getInstance() {
        return XPlatAPI.getInstance().getMindFatigueEffect();
    }

    public static final ResourceLocation ID = HexResearch.id("mind_fatigue");

    public MindFatigueEffect() {
        super(MobEffectCategory.HARMFUL, 0x404040);
    }

    public static MobEffectInstance effectInstance(int durationTicks) {
        return new MobEffectInstance(HREffects.EFFECT_MIND_FATIGUE, durationTicks);
    }

    @Override
    public boolean applyEffectTick(@Nonnull LivingEntity livingEntity, int i) {
        return false;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int i, int j) {
        return false;
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity entity, @Nullable Entity entity2, @Nonnull LivingEntity livingEntity, int i, double d) {}
}
