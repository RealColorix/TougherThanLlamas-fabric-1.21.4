package net.colorixer;

import net.colorixer.block.FallingSlabBlock;
import net.colorixer.block.ModBlockEntities;
import net.colorixer.block.ModBlocks;
import net.colorixer.item.ItemsThatCanHitAndBreak;
import net.colorixer.item.ModItems;
import net.colorixer.player.Chopable;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TougherThanLlamas implements ModInitializer {
	public static final String MOD_ID = "ttll";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModBlockEntities.register();
		ItemsThatCanHitAndBreak.register();
		Chopable.initialize();






















		//TICKING CODE
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			for (var entity : world.iterateEntities()) {
				if (!(entity instanceof FallingBlockEntity falling)) continue;

				BlockState fallingState = falling.getBlockState();
				BlockPos pos = falling.getBlockPos();
				BlockState blockAt = world.getBlockState(pos);

				// ✅ Handle FallingSlab merging
				if (fallingState.getBlock() instanceof FallingSlabBlock slabBlock) {
					if (blockAt.getBlock() == slabBlock && blockAt.contains(FallingSlabBlock.TYPE) && blockAt.get(FallingSlabBlock.TYPE) == SlabType.BOTTOM) {
						boolean isDouble = fallingState.get(FallingSlabBlock.TYPE) == SlabType.DOUBLE;

						if (!isDouble) {
							world.setBlockState(pos, fallingState.with(FallingSlabBlock.TYPE, SlabType.DOUBLE), Block.NOTIFY_ALL);
							falling.discard();
						} else {
							world.setBlockState(pos, fallingState.with(FallingSlabBlock.TYPE, SlabType.DOUBLE), Block.NOTIFY_ALL);
							world.setBlockState(pos.up(), fallingState.with(FallingSlabBlock.TYPE, SlabType.BOTTOM), Block.NOTIFY_ALL);
							falling.discard();
						}
					}
					continue;
				}

				// ✅ Handle vanilla sand/gravel falling onto matching custom slabs
				Block fallingBlock = fallingState.getBlock();

				boolean isSand = fallingBlock == net.minecraft.block.Blocks.SAND;
				boolean isGravel = fallingBlock == net.minecraft.block.Blocks.GRAVEL;

				boolean isSandOnSandSlab = isSand && blockAt.getBlock() == ModBlocks.SAND_SLAB && blockAt.get(FallingSlabBlock.TYPE) == SlabType.BOTTOM;
				boolean isGravelOnGravelSlab = isGravel && blockAt.getBlock() == ModBlocks.GRAVEL_SLAB && blockAt.get(FallingSlabBlock.TYPE) == SlabType.BOTTOM;

				if (isSandOnSandSlab || isGravelOnGravelSlab) {
					// ✅ Upgrade the slab to double
					world.setBlockState(pos, blockAt.with(FallingSlabBlock.TYPE, SlabType.DOUBLE), Block.NOTIFY_ALL);

					// ✅ Spawn a falling slab block above
					BlockPos above = pos.up();
					BlockState slabToFall = blockAt.with(FallingSlabBlock.TYPE, SlabType.BOTTOM);
					FallingBlockEntity newSlab = FallingBlockEntity.spawnFromBlock(world, above, slabToFall);
					newSlab.setPosition(above.getX() + 0.5, above.getY(), above.getZ() + 0.5);

					// ✅ Remove original falling entity
					falling.discard();
				}
			}
		});

	}

}