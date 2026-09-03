package name.dashkal.minecraft.hexresearch.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import name.dashkal.minecraft.hexresearch.HexResearch;
import name.dashkal.minecraft.hexresearch.effect.MindFatigueEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;

public class HREffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(HexResearch.MOD_ID, Registries.MOB_EFFECT);

    public static void init() {
        EFFECTS.register();
    }

    public static final RegistrySupplier<MobEffect> EFFECT_MIND_FATIGUE = EFFECTS.register(MindFatigueEffect.ID.getPath(), () -> MindFatigueEffect.getInstance());
}
