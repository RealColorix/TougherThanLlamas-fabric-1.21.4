package net.colorixer.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CropBlock.class)
public interface CropBlockInvoker {
    /**
     * This Invoker lets us call the protected vanilla moisture calculation
     * from our Mixin logic.
     */
    @Invoker("getAvailableMoisture")
    static float invokeGetAvailableMoisture(Block block, BlockView world, BlockPos pos) {
        throw new AssertionError(); // Mixin will replace this at runtime
    }
}