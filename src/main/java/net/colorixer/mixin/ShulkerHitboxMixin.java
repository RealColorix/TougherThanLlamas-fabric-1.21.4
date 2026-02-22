package net.colorixer.mixin;

import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerEntity.class)
public class ShulkerHitboxMixin {

    @Inject(method = "calculateDefaultBoundingBox", at = @At("HEAD"), cancellable = true)
    private void shrinkHitbox(Vec3d pos, CallbackInfoReturnable<Box> cir) {
        ShulkerEntity self = (ShulkerEntity) (Object) this;

        if (self.isInvisible()) {
            // A standard block is 1.0.
            // We create a box that is 0.99 wide/tall, centered in the block space.
            double size = 0.9999/2; // Half of 0.99

            cir.setReturnValue(new Box(
                    pos.x - size, pos.y + 1 - (size*2), pos.z - size,
                    pos.x + size, pos.y +(size*2), pos.z + size
            ));
        }
    }
}