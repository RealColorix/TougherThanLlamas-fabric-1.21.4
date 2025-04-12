package net.colorixer.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class ReducedMiningSpeed {

    private static final Logger LOGGER = LoggerFactory.getLogger("colorixer-mixin");

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyMiningSpeed(BlockState blockState, CallbackInfoReturnable<Float> cir) {
        Float originalSpeed = cir.getReturnValue();
        if (originalSpeed == null) {
            return;
        }

        // Always apply a base slowdown of 0.25
        float modifiedSpeed = originalSpeed * 0.25f;

        PlayerEntity player = (PlayerEntity) (Object) this;
        boolean toolRequired = blockState.isToolRequired();

        if (toolRequired) {
            ItemStack heldItem = player.getMainHandStack();
            boolean hasCorrectTool = heldItem.isSuitableFor(blockState);

            // Apply extra slowdown only if the block isn't in the exception list
            if (!hasCorrectTool && !EXCEPTION_BLOCKS.contains(blockState.getBlock())) {
                modifiedSpeed *= 0.04f;
            }
        }

        cir.setReturnValue(modifiedSpeed);
    }

    // List of blocks that should be excluded from the extra slowdown multiplier
    private static final List<Block> EXCEPTION_BLOCKS = List.of(
            Blocks.SNOW,
            Blocks.SNOW_BLOCK,
            Blocks.POWDER_SNOW
    );
}
