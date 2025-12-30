package net.colorixer.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.LeavesBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class LeavesPathPassable {

    @Inject(
            method = "blocksMovement",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ttll$leavesDoNotBlockMovement(CallbackInfoReturnable<Boolean> cir) {
        AbstractBlock.AbstractBlockState self =
                (AbstractBlock.AbstractBlockState)(Object)this;

        if (self.getBlock() instanceof LeavesBlock) {
            // Leaves should NEVER block entity movement
            cir.setReturnValue(false);
        }
    }
}
