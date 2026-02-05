package net.colorixer.mixin;

import net.colorixer.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class DirtIntoLooseVariant {

    @Shadow @Final protected ServerWorld world; // In 1.21.4, this is usually shadowed as 'world' or 'serverWorld' depending on mappings

    @Inject(method = "tryBreakBlock", at = @At(value = "RETURN"))
    private void onBlockMined(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // cir.getReturnValue() ensures the block was actually broken successfully
        if (cir.getReturnValue()) {
            triggerErosion(pos, world.getRandom());
        }
    }

    @Unique
    private void triggerErosion(BlockPos pos, Random random) {
        BlockPos abovePos = pos.up();
        BlockState aboveState = world.getBlockState(abovePos);

        boolean isGrass = aboveState.isOf(Blocks.GRASS_BLOCK);
        boolean isDirt = aboveState.isOf(Blocks.DIRT);

        if (isGrass || isDirt) {
            // 50/50 chance to collapse
            if (random.nextBoolean()) {
                BlockState newState;

                if (isGrass) {
                    newState = ModBlocks.GRASS_SLAB.getDefaultState()
                            .with(SlabBlock.TYPE, SlabType.DOUBLE);
                } else {
                    newState = ModBlocks.LOOSE_DIRT_SLAB.getDefaultState()
                            .with(SlabBlock.TYPE, SlabType.DOUBLE);
                }

                world.setBlockState(abovePos, newState, Block.NOTIFY_ALL);

                // Recursive call to check the next block up
                triggerErosion(abovePos, random);
            }
        }
    }
}