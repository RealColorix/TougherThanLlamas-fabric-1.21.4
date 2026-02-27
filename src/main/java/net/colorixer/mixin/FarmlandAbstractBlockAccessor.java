package net.colorixer.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractBlock.class)
public interface FarmlandAbstractBlockAccessor {
    @Invoker("randomTick")
    void invokeRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random);
}