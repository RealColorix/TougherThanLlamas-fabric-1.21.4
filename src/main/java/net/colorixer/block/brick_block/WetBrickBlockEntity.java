package net.colorixer.block.brick_block;

import net.colorixer.block.ModBlockEntities;
import net.colorixer.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WetBrickBlockEntity extends BlockEntity {
    // Drying progress counter; when it reaches 11000 the wet brick becomes dried
    private int tickCounter = 0;

    public WetBrickBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WET_BRICK_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, WetBrickBlockEntity entity) {
        if (world.isClient) return;

        boolean canDry = world.isSkyVisible(pos.up()) &&
                !world.isRaining() &&
                !world.isThundering() &&
                world.isDay();

        if (canDry) {
            entity.tickCounter++;
            if (entity.tickCounter >= 11000) {
                BlockState dried = ModBlocks.DRIED_BRICK.getDefaultState();
                if (state.contains(Properties.HORIZONTAL_FACING)) {
                    dried = dried.with(Properties.HORIZONTAL_FACING, state.get(Properties.HORIZONTAL_FACING));
                }
                world.setBlockState(pos, dried);
            }
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("TickCounter", tickCounter);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        tickCounter = nbt.getInt("TickCounter");
    }
}
