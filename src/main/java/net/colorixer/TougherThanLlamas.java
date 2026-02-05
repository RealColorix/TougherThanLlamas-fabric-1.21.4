package net.colorixer;

import net.colorixer.block.FallingSlabBlock;
import net.colorixer.block.ModBlockEntities;
import net.colorixer.block.ModBlocks;
import net.colorixer.component.ModDataComponentTypes;
import net.colorixer.entity.ModEntities;
import net.colorixer.item.ItemsThatCanHitAndBreak;
import net.colorixer.item.ModItems;
import net.colorixer.player.Chopable;
import net.colorixer.recipe.ModRecipeSerializers;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TougherThanLlamas implements ModInitializer {
	public static final String MOD_ID = "ttll";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);



	@Override
	public void onInitialize() {
		ModEntities.register();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModBlockEntities.register();
		ItemsThatCanHitAndBreak.register();
		Chopable.initialize();
		ModRecipeSerializers.register();
		ModDataComponentTypes.registerDataComponentTypes();


		// 	ServerLifecycleEvents.SERVER_STARTED.register(server -> server.getOverworld().getGameRules().get(GameRules.NATURAL_REGENERATION).set(false, server));
			ServerLifecycleEvents.SERVER_STARTED.register(server -> server.getOverworld().getGameRules().get(GameRules.DO_PATROL_SPAWNING).set(false, server));
			ServerLifecycleEvents.SERVER_STARTED.register(server -> server.getOverworld().getGameRules().get(GameRules.UNIVERSAL_ANGER).set(true, server));
			ServerLifecycleEvents.SERVER_STARTED.register(server -> server.getOverworld().getGameRules().get(GameRules.DO_TRADER_SPAWNING).set(false, server));








		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();


			// Öka block-reach
			EntityAttributeInstance reach = player.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
			if (reach != null) reach.setBaseValue(4.5D);
		});





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
				boolean isLooseCobblestone = fallingBlock == ModBlocks.LOOSE_COBBLESTONE;

				boolean isSandOnSandSlab = isSand && blockAt.getBlock() == ModBlocks.SAND_SLAB && blockAt.get(FallingSlabBlock.TYPE) == SlabType.BOTTOM;
				boolean isGravelOnGravelSlab = isGravel && blockAt.getBlock() == ModBlocks.GRAVEL_SLAB && blockAt.get(FallingSlabBlock.TYPE) == SlabType.BOTTOM;
				boolean isCobblestoneOnCobblestoneSlab = isLooseCobblestone && blockAt.getBlock() == ModBlocks.LOOSE_COBBLESTONE_SLAB && blockAt.get(FallingSlabBlock.TYPE) == SlabType.BOTTOM;


				if (isSandOnSandSlab || isGravelOnGravelSlab||isCobblestoneOnCobblestoneSlab) {
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