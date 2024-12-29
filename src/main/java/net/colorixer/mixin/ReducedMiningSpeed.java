package net.colorixer.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to modify the block breaking speed based on tool requirements.
 * - Always reduces mining speed to 25% of the original value.
 * - If the block requires a tool and the player lacks the correct tool,
 *   further reduces mining speed to 1% of the original value.
 */
@Mixin(PlayerEntity.class)
public abstract class ReducedMiningSpeed {

    private static final Logger LOGGER = LoggerFactory.getLogger("colorixer-mixin");

    /**
     * Injects into the getBlockBreakingSpeed method to modify mining speed.
     * - Always reduces mining speed to 25% of original.
     * - If the block requires a tool and the player lacks the correct tool,
     *   further reduces mining speed to 1% of original.
     *
     * @param blockState The state of the block being mined.
     * @param cir        CallbackInfoReturnable containing the original mining speed.
     */
    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyMiningSpeed(BlockState blockState, CallbackInfoReturnable<Float> cir) {
        // Retrieve the original mining speed
        Float originalSpeed = cir.getReturnValue();

        if (originalSpeed == null) {
            // If for some reason the original speed is null, do not modify it
            return;
        }

        // Apply the base reduction of 0.25x
        float modifiedSpeed = originalSpeed * 0.25f;

        // Cast 'this' to PlayerEntity to access player-specific methods
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Check if the block requires a tool to be mined
        boolean toolRequired = blockState.isToolRequired();

        if (toolRequired) {
            // Get the tool the player is currently holding in their main hand
            ItemStack heldItem = player.getMainHandStack();

            // Determine if the held item is suitable for mining the block
            boolean hasCorrectTool = heldItem.isSuitableFor(blockState);

            if (!hasCorrectTool) {
                // If the player does not have the correct tool, apply additional reduction
                modifiedSpeed *= 0.04f; // Now, modifiedSpeed = originalSpeed * 0.25 * 0.04 = 0.01x


            }
        }

        // Set the modified mining speed as the new return value
        cir.setReturnValue(modifiedSpeed);
    }
}
