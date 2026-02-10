package net.colorixer;

import net.colorixer.advancements.DestroyBlockCriterion;
import net.colorixer.advancements.InVicinityCriterion;
import net.colorixer.block.FallingSlabBlock;
import net.colorixer.block.ModBlockEntities;
import net.colorixer.block.ModBlocks;
import net.colorixer.component.ModDataComponentTypes;
import net.colorixer.entity.ModEntities;
import net.colorixer.entity.spiders.JungleSpiderEntity;
import net.colorixer.item.ItemsThatCanHitAndBreak;
import net.colorixer.item.ModItems;
import net.colorixer.player.Chopable;
import net.colorixer.recipe.ModRecipeSerializers;
import net.colorixer.sounds.ModSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TougherThanLlamas implements ModInitializer {
	public static final String MOD_ID = "ttll";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private int getSyncIndex(ServerPlayerEntity player) {
		for (String tag : player.getCommandTags()) {
			if (tag.startsWith("recipe_index_")) {
				return Integer.parseInt(tag.substring(13));
			}
		}
		return 0;
	}

	public static final DestroyBlockCriterion DESTROY_BLOCK = Criteria.register("ttll:destroy_block", new DestroyBlockCriterion());
	public static final InVicinityCriterion IN_VICINITY = Criteria.register("ttll:in_vicinity", new InVicinityCriterion());

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
		ModSounds.registerSounds();

		// 	ServerLifecycleEvents.SERVER_STARTED.register(server -> server.getOverworld().getGameRules().get(GameRules.NATURAL_REGENERATION).set(false, server));
			ServerLifecycleEvents.SERVER_STARTED.register(server -> server.getOverworld().getGameRules().get(GameRules.DO_PATROL_SPAWNING).set(false, server));
			ServerLifecycleEvents.SERVER_STARTED.register(server -> server.getOverworld().getGameRules().get(GameRules.UNIVERSAL_ANGER).set(true, server));
			ServerLifecycleEvents.SERVER_STARTED.register(server -> server.getOverworld().getGameRules().get(GameRules.DO_TRADER_SPAWNING).set(false, server));






		FabricDefaultAttributeRegistry.register(ModEntities.JUNGLE_SPIDER, JungleSpiderEntity.createJungleSpiderAttributes());


		BiomeModifications.addSpawn(
				BiomeSelectors.includeByKey(BiomeKeys.JUNGLE, BiomeKeys.BAMBOO_JUNGLE),
				SpawnGroup.MONSTER,
				ModEntities.JUNGLE_SPIDER, // Changed from EntityType.CAVE_SPIDER
				200, 3, 3
		);

// 2. Low Weight (20) for Sparse Jungle - USING JUNGLE SPIDER
		BiomeModifications.addSpawn(
				BiomeSelectors.includeByKey(BiomeKeys.SPARSE_JUNGLE),
				SpawnGroup.MONSTER,
				ModEntities.JUNGLE_SPIDER, // Changed from EntityType.CAVE_SPIDER
				20, 3, 3
		);

// 3. Register Spawn Rules for JUNGLE SPIDER
		SpawnRestriction.register(
				ModEntities.JUNGLE_SPIDER,
				SpawnLocationTypes.ON_GROUND,
				Heightmap.Type.MOTION_BLOCKING,
				(type, world, spawnReason, pos, random) -> {
					boolean isSpawnableBlock = world.getBlockState(pos.down()).isSolidBlock(world, pos.down()) ||
							world.getBlockState(pos.down()).isIn(BlockTags.LEAVES);

					return isSpawnableBlock && HostileEntity.canSpawnIgnoreLightLevel(type, world, spawnReason, pos, random);
				}
		);





		// 1. In your JOIN event, just mark the player
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			handler.getPlayer().addCommandTag("needs_recipe_sync");
		});

// 2. In your END_SERVER_TICK, process them in chunks
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			// Only run this logic every 10 ticks to keep it "slow" and safe
			if (server.getTicks() % 10 != 0) return;

			var recipeManager = server.getRecipeManager();
			var allRecipes = recipeManager.values().stream().toList();
			int batchSize = 50; // Adjust this number if you still get errors

			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (player.getCommandTags().contains("needs_recipe_sync")) {
					// Use a custom data component or a simple counter via tags to track progress
					// For simplicity, we'll use a tag with the current index
					int currentIndex = getSyncIndex(player);

					int toIndex = Math.min(currentIndex + batchSize, allRecipes.size());
					var batch = allRecipes.subList(currentIndex, toIndex);

					player.unlockRecipes(batch);

					if (toIndex >= allRecipes.size()) {
						player.removeCommandTag("needs_recipe_sync");
						player.removeCommandTag("recipe_index_" + currentIndex);
						LOGGER.info("Finished syncing all recipes for " + player.getName().getString());
					} else {
						// Update the index tag
						player.removeCommandTag("recipe_index_" + currentIndex);
						player.addCommandTag("recipe_index_" + toIndex);
					}
				}
			}
		});



		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				// Check once per second (every 20 ticks)
				if (player.age % 20 != 0) continue;

				World world = player.getWorld();
				BlockPos pPos = player.getBlockPos();
				int r = 2;

				// Scan 3x3x3 area
				for (BlockPos targetPos : BlockPos.iterate(pPos.add(-r, -r, -r), pPos.add(r, r, r))) {
					if (isBlockExposed(world, targetPos)) {
						// Pass the player and the specific block position found
						IN_VICINITY.trigger(player, targetPos);
					}
				}
			}
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

	public static boolean isBlockExposed(World world, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			if (!world.getBlockState(pos.offset(direction)).isFullCube(world, pos.offset(direction))) {
				return true;
			}
		}
		return false;
	}

}