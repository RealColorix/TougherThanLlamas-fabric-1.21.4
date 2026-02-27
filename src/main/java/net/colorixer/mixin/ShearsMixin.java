package net.colorixer.mixin;

import net.colorixer.util.FarmlandHelper;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShearsItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsItem.class)
public class ShearsMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void handleShearing(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();

        // 1. Check if clicking Farmland directly or a crop above it
        BlockPos targetFarmland = null;
        if (world.getBlockState(pos).isOf(Blocks.FARMLAND)) {
            targetFarmland = pos;
        } else if (world.getBlockState(pos.down()).isOf(Blocks.FARMLAND)) {
            targetFarmland = pos.down();
        }

        if (targetFarmland != null) {
            // Call our helper logic
            ActionResult result = FarmlandHelper.cutWeeds(
                    world,
                    targetFarmland,
                    world.getBlockState(targetFarmland),
                    context.getPlayer(),
                    context.getStack()
            );

            if (result.isAccepted()) {
                cir.setReturnValue(result);
            } else {
                // If it failed (e.g., no weeds to cut), FAIL so it doesn't do vanilla stuff
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
        // Note: We don't return FAIL at the end here so shears still work on Sheep/Leaves
    }
}