package name.dashkal.minecraft.hexresearch.mixin;

import name.dashkal.minecraft.hexresearch.registry.HRAdvancementTriggers;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hooks advancement completion for the {@code hexresearch:advancement} criterion. */
@Mixin(PlayerAdvancements.class)
public abstract class AdvancementTriggerMixin {
    @Shadow
    private ServerPlayer player;

    @Shadow
    public abstract AdvancementProgress getOrStartProgress(AdvancementHolder advancement);

    @Inject(
            method = "award(Lnet/minecraft/advancements/AdvancementHolder;Ljava/lang/String;)Z",
            at = @At("RETURN")
    )
    private void hexresearch$onAward(
            AdvancementHolder advancement,
            String criterion,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (cir.getReturnValue() && getOrStartProgress(advancement).isDone()) {
            HRAdvancementTriggers.ADVANCEMENT_TRIGGER.trigger(player, advancement.id());
        }
    }
}
