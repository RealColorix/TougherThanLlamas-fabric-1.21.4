package net.colorixer.mixin;

import net.colorixer.util.SkeletonConversionTracker; // Import the interface
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class SkeletonLivingEntityModifier implements SkeletonConversionTracker {

    @Unique
    private int ttll$arrowCount = 0;

    // Interface Implementations
    @Override public int getArrowCount() { return this.ttll$arrowCount; }
    @Override public void setArrowCount(int count) { this.ttll$arrowCount = count; }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void ttll$writeArrows(NbtCompound nbt, CallbackInfo ci) {
        if ((Object) this instanceof SkeletonEntity) {
            nbt.putInt("ttll_Arrows", this.ttll$arrowCount);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void ttll$readArrows(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("ttll_arrows")) {
            this.ttll$arrowCount = nbt.getInt("ttll_arrows");
        }
    }
}