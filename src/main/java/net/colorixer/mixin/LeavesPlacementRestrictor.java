package net.colorixer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPlacementContext.class)
public class LeavesPlacementRestrictor {

    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    private void preventPlacingOnLeaves(CallbackInfoReturnable<Boolean> cir) {
        ItemPlacementContext context = (ItemPlacementContext) (Object) this;
        World world = context.getWorld();
        BlockPos targetPos = context.getBlockPos();
        Direction side = context.getSide();

        // If the block you're clicking on is leaves
        BlockState clickedBlock = world.getBlockState(targetPos.offset(side.getOpposite()));

        if (clickedBlock.getBlock() instanceof LeavesBlock) {
            cir.setReturnValue(false); // cancel placement
        }
    }
}
