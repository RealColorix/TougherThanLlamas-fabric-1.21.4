package net.colorixer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerEntity.class)
public abstract class ReducedMiningSpeed {

    private static final Logger LOGGER = LoggerFactory.getLogger("colorixer-mixin");

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyMiningSpeed(BlockState blockState, CallbackInfoReturnable<Float> cir) {
        Float originalSpeed = cir.getReturnValue();

        if (originalSpeed == null) {
            return;
        }

        float modifiedSpeed = originalSpeed * 0.25f;

        PlayerEntity player = (PlayerEntity) (Object) this;

        boolean toolRequired = blockState.isToolRequired();

        if (toolRequired) {
            ItemStack heldItem = player.getMainHandStack();

            boolean hasCorrectTool = heldItem.isSuitableFor(blockState);

            if (!hasCorrectTool) {
                modifiedSpeed *= 0.04f;


            }
        }

        cir.setReturnValue(modifiedSpeed);
    }
}
