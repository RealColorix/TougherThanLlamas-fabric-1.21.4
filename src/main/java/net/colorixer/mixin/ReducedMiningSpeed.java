package net.colorixer.mixin;

import net.colorixer.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
        if (originalSpeed == null) return;

        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack heldItem = player.getMainHandStack();

        if (heldItem.getItem() == ModItems.STONE_HOE &&
                (blockState.isOf(Blocks.DIRT) || blockState.isOf(Blocks.GRASS_BLOCK)|| blockState.isOf(Blocks.COARSE_DIRT))) {


            HitResult hitResult = player.raycast(5.0D, 0.0F, false);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos miningPos = ((BlockHitResult) hitResult).getBlockPos();
                BlockState aboveState = player.getWorld().getBlockState(miningPos.up());

                // If the block above is air, increase mining speed
                if (aboveState.isAir()) {
                    cir.setReturnValue(originalSpeed * 0.5f);
                    return;
                }
            }
        }

        if (heldItem.getItem() == ModItems.SHARP_ROCK &&
                (blockState.isOf(Blocks.OAK_LOG) ||blockState.isOf(Blocks.SPRUCE_LOG) ||blockState.isOf(Blocks.BIRCH_LOG) ||blockState.isOf(Blocks.JUNGLE_LOG))) {
                    cir.setReturnValue(originalSpeed * 0.5f);
            return;
        }


        // Normal slowdown handling
        float modifiedSpeed = originalSpeed * 0.25f;
        boolean toolRequired = blockState.isToolRequired();

        if (toolRequired) {
            boolean hasCorrectTool = heldItem.isSuitableFor(blockState);

            if (!hasCorrectTool && !EXCEPTION_BLOCKS.contains(blockState.getBlock())) {
                modifiedSpeed *= 0.04f;
            }
        }

        cir.setReturnValue(modifiedSpeed);
    }

    private static final List<Block> EXCEPTION_BLOCKS = List.of(
            Blocks.SNOW,
            Blocks.SNOW_BLOCK,
            Blocks.POWDER_SNOW
    );
}
