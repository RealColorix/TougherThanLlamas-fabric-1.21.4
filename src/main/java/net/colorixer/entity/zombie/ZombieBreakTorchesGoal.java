package net.colorixer.entity.zombie;

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
        if (zombie.getWorld().getTime() % 20 != 0) return false;

        this.torchPos = findNearbyTorch();
        return this.torchPos != null;
    }

    private BlockPos findNearbyTorch() {
        BlockPos center = zombie.getBlockPos();
        // Scanning 4 blocks out and 2 up/down
        for (BlockPos pos : BlockPos.iterate(center.add(-4, -2, -4), center.add(4, 2, 4))) {
            if (zombie.getWorld().getBlockState(pos).isOf(Blocks.TORCH) ||
                    zombie.getWorld().getBlockState(pos).isOf(Blocks.WALL_TORCH)) {
                return pos.toImmutable();
            }
        }
        return null;
    }

    @Override
    public void start() {
        breakTimer = 0;
    }

    @Override
    public void tick() {
        if (torchPos == null) return;

        // FIXED METHOD NAME HERE
        double dist = zombie.squaredDistanceTo(torchPos.getX() + 0.5, torchPos.getY(), torchPos.getZ() + 0.5);
        zombie.getLookControl().lookAt(torchPos.getX() + 0.5, torchPos.getY(), torchPos.getZ() + 0.5);

        if (dist > 2.0) {
            zombie.getNavigation().startMovingTo(torchPos.getX() + 0.5, torchPos.getY(), torchPos.getZ() + 0.5, 1.0);
        } else {
            zombie.getNavigation().stop();
            breakTimer++;

            if (breakTimer % 10 == 0) {
                zombie.swingHand(Hand.MAIN_HAND);
            }

            if (breakTimer >= 20) {
                zombie.getWorld().breakBlock(torchPos, false);
                this.torchPos = null;
            }
        }
    }

    @Override
    public boolean shouldContinue() {
        if (torchPos == null) return false;
        // FIXED METHOD NAME HERE AS WELL
        return (zombie.getWorld().getBlockState(torchPos).isOf(Blocks.TORCH) ||
                zombie.getWorld().getBlockState(torchPos).isOf(Blocks.WALL_TORCH)) &&
                zombie.squaredDistanceTo(torchPos.getX() + 0.5, torchPos.getY(), torchPos.getZ() + 0.5) < 64;
    }

    @Override
    public void stop() {
        this.torchPos = null;
        this.breakTimer = 0;
    }
}