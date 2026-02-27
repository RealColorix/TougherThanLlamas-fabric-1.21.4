package net.colorixer.mixin;

import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileLeavesMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void ttll$applyLeafSlowdown(CallbackInfo ci) {
        ProjectileEntity projectile = (ProjectileEntity)(Object)this;

        if (projectile.getWorld().isClient || projectile.isOnGround()) return;

        BlockPos pos = new BlockPos(
                (int)Math.floor(projectile.getX()),
                (int)Math.floor(projectile.getY()),
                (int)Math.floor(projectile.getZ())
        );
        if (projectile.getWorld().getBlockState(pos).isIn(BlockTags.LEAVES)) {
            Vec3d velocity = projectile.getVelocity();
            double newY = velocity.y > 0 ? velocity.y * 0.75 : velocity.y;
            projectile.setVelocity(new Vec3d(velocity.x * 0.75, newY, velocity.z * 0.75));
            projectile.velocityDirty = true;
        }
    }
}