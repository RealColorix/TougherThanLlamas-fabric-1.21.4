package net.colorixer.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class ModFallingBlock extends FallingBlock {
    // This allows the game to serialize any block using this class
    public static final MapCodec<ModFallingBlock> CODEC = createCodec(ModFallingBlock::new);

    public ModFallingBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends FallingBlock> getCodec() {
        return CODEC;
    }

    // Optional: Controls the color of the dust particles when it lands/falls
    @Override
    public int getColor(BlockState state, BlockView world, BlockPos pos) {
        return state.getMapColor(world, pos).color;
    }
}