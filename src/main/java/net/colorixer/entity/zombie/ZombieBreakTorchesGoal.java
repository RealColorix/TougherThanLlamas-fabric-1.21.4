package net.colorixer.entity.zombie;

import net.colorixer.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

public class ZombieBreakTorchesGoal extends Goal {
    private final ZombieEntity zombie;
    private BlockPos torchPos = null;
    private int breakTimer = 0;

    public ZombieBreakTorchesGoal(ZombieEntity zombie) {
        this.zombie = zombie;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {


        this.torchPos = findNearbyTorch();

        if (this.torchPos != null) {
            // findPathTo(BlockPos, distance) returns a Path object.
            // If it's null, the zombie can't reach it.
            net.minecraft.entity.ai.pathing.Path path = zombie.getNavigation().findPathTo(torchPos, 0);
            return path != null;
        }
        return false;
    }

    private BlockPos findNearbyTorch() {
        BlockPos center = zombie.getBlockPos();
        // Increase search range slightly
        for (BlockPos pos : BlockPos.iterate(center.add(-5, -2, -5), center.add(5, 2, 5))) {
            BlockState state = zombie.getWorld().getBlockState(pos);
            if (isLightSource(state)) {
                return pos.toImmutable();
            }
        }
        return null;
    }

    @Override
    public void tick() {
        if (torchPos == null) return;

        double targetX = torchPos.getX() + 0.5;
        double targetY = torchPos.getY();
        double targetZ = torchPos.getZ() + 0.5;

        double distSq = zombie.squaredDistanceTo(targetX, targetY, targetZ);
        zombie.getLookControl().lookAt(targetX, targetY, targetZ);

        if (distSq > 2.0) {
            zombie.getNavigation().startMovingTo(targetX, targetY, targetZ, 1.0);
        } else {
            zombie.getNavigation().stop();
            breakTimer++;

            if (breakTimer % 10 == 0) {
                zombie.swingHand(Hand.MAIN_HAND);
            }

            if (breakTimer >= 20) {
                // 1. Get the block state before breaking it
                BlockState state = zombie.getWorld().getBlockState(torchPos);

                // 2. Determine if it should drop based on the block type
                // If it's our Burning Crude Torch, drop = false. Otherwise, drop = true.
                boolean shouldDrop = !state.isOf(ModBlocks.BURNING_CRUDE_TORCH);

                // 3. Break the block
                zombie.getWorld().breakBlock(torchPos, shouldDrop);

                this.torchPos = null;
            }
        }
    }


    private boolean isLightSource(BlockState state) {
        return state.isOf(Blocks.TORCH) ||
                state.isOf(Blocks.WALL_TORCH) ||
                state.isOf(Blocks.SOUL_TORCH) ||
                state.isOf(Blocks.SOUL_WALL_TORCH) ||
                state.isOf(Blocks.REDSTONE_TORCH) ||
                state.isOf(Blocks.REDSTONE_WALL_TORCH) ||
                state.isOf(Blocks.LANTERN) || // Why not lanterns too?
                state.isOf(Blocks.SOUL_LANTERN) ||
                state.isOf(ModBlocks.BURNING_CRUDE_TORCH);
    }



    @Override
    public void start() {
        breakTimer = 0;
    }



    @Override
    public boolean shouldContinue() {
        if (torchPos == null) return false;

        BlockState state = zombie.getWorld().getBlockState(torchPos);
        double dist = zombie.squaredDistanceTo(torchPos.getX() + 0.5, torchPos.getY(), torchPos.getZ() + 0.5);

        // Keep going if it's still a torch and we are within 8 blocks
        return isLightSource(state) && dist < 64;
    }

    @Override
    public void stop() {
        this.torchPos = null;
        this.breakTimer = 0;
    }
}