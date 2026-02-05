package net.colorixer.entity.passive.cow;

import net.colorixer.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;

public class CowEatGrassGoal extends Goal {
    private final CowEntity cow;
    private final World world;
    private int timer;

    public CowEatGrassGoal(CowEntity cow) {
        this.cow = cow;
        this.world = cow.getWorld();
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
    }

    @Override
    public boolean canStart() {
        if (((CowHungerAccessor) cow).ttll$getHunger() >= 4) return false;

        // ADD THIS: Small random delay so they don't react instantly
        // This makes it feel more natural.
        if (this.cow.getRandom().nextInt(100) != 0) return false;

        BlockPos pos = cow.getBlockPos().down();
        return this.world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK);
    }

    @Override
    public void start() {
        this.timer = 40;
        ((CowHungerAccessor) this.cow).ttll$setEatAnimTicks(40); // START ANIMATION
        this.cow.getNavigation().stop();
    }

    @Override
    public boolean shouldContinue() { return this.timer > 0; }

    @Override
    public void tick() {
        this.timer = Math.max(0, this.timer - 1);
        if (this.timer == 4) {
            BlockPos pos = cow.getBlockPos().down();
            BlockState state = this.world.getBlockState(pos);

            // Check for Vanilla Grass
            if (state.isOf(Blocks.GRASS_BLOCK)) {
                this.world.setBlockState(pos, Blocks.DIRT.getDefaultState());
                this.applyHungerAndEffects();
            }
            // Check for Custom Grass Slab
            else if (state.isOf(ModBlocks.GRASS_SLAB)) {
                // Convert to Loose Dirt Slab while keeping the same TYPE and WATERLOGGED status
                this.world.setBlockState(pos, ModBlocks.LOOSE_DIRT_SLAB.getDefaultState()
                        .with(SlabBlock.TYPE, state.get(SlabBlock.TYPE))
                        .with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED)));

                this.applyHungerAndEffects();
            }
        }
    }

    // Helper method to keep the code clean since the logic is identical for both blocks
    private void applyHungerAndEffects() {
        CowHungerAccessor data = (CowHungerAccessor) cow;
        int currentHunger = data.ttll$getHunger();
        data.ttll$setHunger(currentHunger + 1);

        this.cow.onEatingGrass();
    }
}