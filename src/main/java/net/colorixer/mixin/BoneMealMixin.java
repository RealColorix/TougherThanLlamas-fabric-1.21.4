package net.colorixer.mixin;

import net.colorixer.util.FarmlandHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public class BoneMealMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void handleBoneMeal(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        // 1. Direct Farmland click -> PASS so FarmlandMixin's onUseWithItem takes it
        if (state.isOf(Blocks.FARMLAND)) {
            cir.setReturnValue(ActionResult.PASS);
            return;
        }

        // 2. Clicked block ABOVE farmland (like a crop)
        if (world.getBlockState(pos.down()).isOf(Blocks.FARMLAND)) {
            BlockPos farmlandPos = pos.down();
            BlockState farmlandState = world.getBlockState(farmlandPos);

            ActionResult result = FarmlandHelper.fertilize(world, farmlandPos, farmlandState, context.getPlayer(), context.getStack());

            if (result.isAccepted()) {
                cir.setReturnValue(result);
            } else {
                // If it's already fertilized, FAIL so we don't trigger vanilla growth
                cir.setReturnValue(ActionResult.FAIL);
            }
            return;
        }

        // 3. Not farmland? FAIL everything else (disables vanilla bonemeal)
        cir.setReturnValue(ActionResult.FAIL);
    }
}