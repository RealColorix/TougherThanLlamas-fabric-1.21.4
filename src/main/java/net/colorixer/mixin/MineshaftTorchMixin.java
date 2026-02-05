package net.colorixer.mixin;

import net.colorixer.block.ModBlocks;
import net.colorixer.block.torch.CrudeTorchBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StructurePiece.class)
public abstract class MineshaftTorchMixin {

    @ModifyVariable(method = "addBlock", at = @At("HEAD"), argsOnly = true)
    private BlockState swapTorchState(BlockState original) {
        // If it's a torch or wall torch, swap it for our burned version
        if (original.isOf(Blocks.TORCH) || original.isOf(Blocks.WALL_TORCH)) {
            BlockState burned = ModBlocks.CRUDE_TORCH.getDefaultState().with(CrudeTorchBlock.BURNED, true);

            if (original.isOf(Blocks.WALL_TORCH)) {
                Direction facing = original.get(Properties.HORIZONTAL_FACING);
                return burned.with(CrudeTorchBlock.FACING, facing);
            }
            return burned.with(CrudeTorchBlock.FACING, Direction.UP);
        }

        // Return original if it's not a torch (like wood, cobwebs, etc.)
        return original;
    }
}