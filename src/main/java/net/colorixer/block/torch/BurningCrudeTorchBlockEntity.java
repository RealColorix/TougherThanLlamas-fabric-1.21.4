package net.colorixer.block.torch;

import net.colorixer.block.ModBlockEntities;
import net.colorixer.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BurningCrudeTorchBlockEntity extends BlockEntity {
    private int burnTime = 24000;
    private float rainPenalty = 0.0f;

    public BurningCrudeTorchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BURNING_CRUDE_TORCH, pos, state);
    }

    public void setBurnTime(int time) {
        this.burnTime = time;
        this.markDirty();
    }



    public int getBurnTime() { return this.burnTime; }

    public static void tick(World world, BlockPos pos, BlockState state, BurningCrudeTorchBlockEntity be) {
        if (world.isClient) return;

        // --- RAIN LOGIC ---
        float burnRate = 1.0f;
        if (world.isRaining() && world.isSkyVisible(pos.up())) {
            be.rainPenalty += 0.02f; // Growing penalty
            burnRate += be.rainPenalty;
        } else {
            be.rainPenalty = 0.0f; // Reset if dry or under roof
        }

        // Subtract the rate (minimum 1)
        be.burnTime -= (int)burnRate;

        // 1. Update visual 'low fuel' state
        if (be.burnTime <= 2000 && !state.get(BurningCrudeTorchBlock.LOW_FUEL)) {
            world.setBlockState(pos, state.with(BurningCrudeTorchBlock.LOW_FUEL, true), 3);
        }

        // 2. Extinguish Logic
        if (be.burnTime <= 0) {
            Direction currentFacing = state.get(BurningCrudeTorchBlock.FACING);
            world.setBlockState(pos, ModBlocks.CRUDE_TORCH.getDefaultState()
                    .with(CrudeTorchBlock.FACING, currentFacing)
                    .with(CrudeTorchBlock.BURNED, true), 3);

            world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_FIRE_EXTINGUISH,
                    net.minecraft.sound.SoundCategory.BLOCKS, 0.5f, 2.0f);
        }

        // Keep the markDirty frequency
        if (be.burnTime % 100 == 0 || be.rainPenalty > 0) {
            be.markDirty();
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("BurnTime", this.burnTime);
        nbt.putFloat("RainPenalty", this.rainPenalty);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        // Default to 24000 if the tag is missing (prevents immediate breaking)
        if (nbt.contains("BurnTime")) {
            this.burnTime = nbt.getInt("BurnTime");
        } else {
            this.burnTime = 24000;
        }
        this.rainPenalty = nbt.getFloat("RainPenalty"); // Load it
    }
}