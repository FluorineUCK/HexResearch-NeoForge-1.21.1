package name.dashkal.minecraft.hexresearch.forge.effect;

import name.dashkal.minecraft.hexresearch.effect.MindFatigueEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;

import java.util.Set;

/**
 * Forge specific MindFatigueEffect that does not allow any curative items.
 */
public class MindFatigueEffectImpl extends MindFatigueEffect {
    public static final MindFatigueEffectImpl INSTANCE = new MindFatigueEffectImpl();

    public MindFatigueEffectImpl() {
        super();
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        // Deliberately leave the set empty: Mind Fatigue cannot be removed by normal cures.
    }
}
