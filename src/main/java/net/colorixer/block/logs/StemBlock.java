package net.colorixer.block.logs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class StemBlock extends Block {

    // Define an EnumProperty for the block's axis (Y, X, Z)
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;

    // Define the voxel shapes for each axis
    private static final VoxelShape Y_AXIS_SHAPE = VoxelShapes.cuboid(0.3125, 0.0, 0.3125, 0.6875, 1.0, 0.6875); // 5/16 to 11/16 on X and Z
    private static final VoxelShape X_AXIS_SHAPE = VoxelShapes.cuboid(0.0, 0.3125, 0.3125, 1.0, 0.6875, 0.6875); // Full X, 5/16 to 11/16 on Y and Z
    private static final VoxelShape Z_AXIS_SHAPE = VoxelShapes.cuboid(0.3125, 0.3125, 0.0, 0.6875, 0.6875, 1.0);

    public StemBlock(Settings settings) {
        super(settings);
        // Set the default state with the Y axis
        this.setDefaultState(this.stateManager.getDefaultState().with(AXIS, Direction.Axis.Y));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Determine the axis based on the placement direction
        Direction.Axis axis = ctx.getSide().getAxis();
        return this.getDefaultState().with(AXIS, axis);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // Return the appropriate shape based on the block's axis
        switch (state.get(AXIS)) {
            case X:
                return X_AXIS_SHAPE;
            case Z:
                return Z_AXIS_SHAPE;
            case Y:
            default:
                return Y_AXIS_SHAPE;
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // Use the same shape for collision
        return this.getOutlineShape(state, world, pos, context);
    }
}
