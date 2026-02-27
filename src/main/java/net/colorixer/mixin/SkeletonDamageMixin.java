package net.colorixer.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class SkeletonDamageMixin {

    @Shadow public abstract void setDamage(double damage);
    @Shadow public abstract double getDamage();

    @Inject(method = "applyDamageModifier", at = @At("TAIL"))
    private void ttll$reduceGlobalSkeletonDamage(float damageModifier, CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;
        Entity owner = arrow.getOwner();

        if (owner instanceof AbstractSkeletonEntity) {
            double nerfedDamage = this.getDamage() * 0.666;
            this.setDamage(nerfedDamage);
        }
    }
}