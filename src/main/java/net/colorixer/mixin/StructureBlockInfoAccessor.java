package net.colorixer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureTemplate.StructureBlockInfo.class)
public interface StructureBlockInfoAccessor {
    @Accessor("pos")
    BlockPos getPos();

    @Accessor("state")
    BlockState getState();

    @Accessor("nbt")
    NbtCompound getNbt();
}