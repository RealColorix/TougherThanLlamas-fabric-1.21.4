package net.colorixer.mixin;

import net.minecraft.advancement.AdvancementPositioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AdvancementPositioner.class)
public interface AdvancementPositionerAccessor {
    @Invoker("apply")
    void invokeApply();
}