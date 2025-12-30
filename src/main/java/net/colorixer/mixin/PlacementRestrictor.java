package net.colorixer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemPlacementContext.class)
public class PlacementRestrictor {

    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    private void ttll$preventPlacingOnLeavesAndIce(CallbackInfoReturnable<Boolean> cir) {
        ItemPlacementContext context = (ItemPlacementContext) (Object) this;

        World world = context.getWorld();
        BlockPos targetPos = context.getBlockPos();
        Direction side = context.getSide();

        // The block the player actually clicked on
        BlockPos clickedPos = targetPos.offset(side.getOpposite());
        BlockState clickedState = world.getBlockState(clickedPos);

        // Disallow placement on leaves
        if (clickedState.getBlock() instanceof LeavesBlock) {
            cir.setReturnValue(false);
            return;
        }

        // Disallow placement on any ice (ice, packed ice, blue ice, frosted ice)
        if (clickedState.isIn(BlockTags.ICE)) {
            cir.setReturnValue(false);
        }
    }
}
