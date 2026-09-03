package name.dashkal.minecraft.hexresearch.registry;

import name.dashkal.minecraft.hexresearch.advancements.trigger.AdvancementTrigger;

public final class HRAdvancementTriggers {
    public static final AdvancementTrigger ADVANCEMENT_TRIGGER = new AdvancementTrigger();

    private HRAdvancementTriggers() {
    }

    public static void init() {
        // Trigger static initialization before platform registration.
    }
}
