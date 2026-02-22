package net.colorixer;

import net.colorixer.advancements.DestroyBlockCriterion;
import net.colorixer.advancements.InVicinityCriterion;
import net.colorixer.block.falling_slabs.FallingSlabBlock;
import net.colorixer.block.ModBlockEntities;
import net.colorixer.block.ModBlocks;
import net.colorixer.component.ModDataComponentTypes;
import net.colorixer.entity.ModEntities;
import net.colorixer.entity.creeper.firecreeper.FireCreeperEntity;
import net.colorixer.entity.spiders.JungleSpiderEntity;
import net.colorixer.item.ItemsThatCanHitAndBreak;
import net.colorixer.item.ModItems;
import net.colorixer.player.Chopable;
import net.colorixer.recipe.ModRecipeSerializers;
import net.colorixer.sounds.ModSounds;
import net.colorixer.util.GloomHelper;
import net.colorixer.worldgen.OreWorldGen;
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
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
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

	private static void enforceDefaultGamerules(ServerWorld world, MinecraftServer server) {
		GameRules rules = world.getGameRules();

		rules.get(GameRules.DO_TRADER_SPAWNING).set(false, server);
		rules.get(GameRules.DO_PATROL_SPAWNING).set(false, server);
		rules.get(GameRules.UNIVERSAL_ANGER).set(false, server);
		rules.get(GameRules.NATURAL_REGENERATION).set(true, server);
		rules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(true, server);
		rules.get(GameRules.BLOCK_EXPLOSION_DROP_DECAY).set(true, server);
		rules.get(GameRules.COMMAND_BLOCK_OUTPUT).set(false, server);
		rules.get(GameRules.DISABLE_ELYTRA_MOVEMENT_CHECK).set(false, server);
		rules.get(GameRules.DISABLE_PLAYER_MOVEMENT_CHECK).set(false, server);
		rules.get(GameRules.DISABLE_RAIDS).set(false, server);
		rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
		rules.get(GameRules.DO_ENTITY_DROPS).set(true, server);
		rules.get(GameRules.DO_FIRE_TICK).set(true, server);
		rules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(false, server);
		rules.get(GameRules.DO_INSOMNIA).set(true, server);
		rules.get(GameRules.DO_LIMITED_CRAFTING).set(false, server);
		rules.get(GameRules.DO_MOB_GRIEFING).set(true, server);
		rules.get(GameRules.DO_MOB_LOOT).set(true, server);
		rules.get(GameRules.DO_MOB_SPAWNING).set(true, server);
		rules.get(GameRules.DO_TILE_DROPS).set(true, server);
		rules.get(GameRules.DO_VINES_SPREAD).set(true, server);
		rules.get(GameRules.DO_WARDEN_SPAWNING).set(true, server);
		rules.get(GameRules.DO_WEATHER_CYCLE).set(true, server);
		rules.get(GameRules.DROWNING_DAMAGE).set(true, server);
		rules.get(GameRules.ENDER_PEARLS_VANISH_ON_DEATH).set(false, server);
		rules.get(GameRules.FALL_DAMAGE).set(true, server);
		rules.get(GameRules.FIRE_DAMAGE).set(true, server);
		rules.get(GameRules.FORGIVE_DEAD_PLAYERS).set(true, server);
		rules.get(GameRules.FREEZE_DAMAGE).set(true, server);
		rules.get(GameRules.GLOBAL_SOUND_EVENTS).set(true, server);
		rules.get(GameRules.KEEP_INVENTORY).set(false, server);
		rules.get(GameRules.LAVA_SOURCE_CONVERSION).set(false, server);
		rules.get(GameRules.MOB_EXPLOSION_DROP_DECAY).set(true, server);
		rules.get(GameRules.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY).set(80, server); // example value
		rules.get(GameRules.PLAYERS_NETHER_PORTAL_CREATIVE_DELAY).set(0 , server);
		rules.get(GameRules.PLAYERS_SLEEPING_PERCENTAGE).set(1, server);
		rules.get(GameRules.PROJECTILES_CAN_BREAK_BLOCKS).set(true, server);
		rules.get(GameRules.RANDOM_TICK_SPEED).set(3, server);
		rules.get(GameRules.SHOW_DEATH_MESSAGES).set(true, server);
		rules.get(GameRules.SNOW_ACCUMULATION_HEIGHT).set(8, server);
		rules.get(GameRules.SPECTATORS_GENERATE_CHUNKS).set(true, server);
		rules.get(GameRules.WATER_SOURCE_CONVERSION).set(false, server);
		rules.get(GameRules.TNT_EXPLOSION_DROP_DECAY).set(true, server);
		rules.get(GameRules.SPAWN_RADIUS).set(1000, server);
		rules.get(GameRules.SPAWN_CHUNK_RADIUS).set(1, server);
	}


	public static final DestroyBlockCriterion DESTROY_BLOCK = Criteria.register("ttll:destroy_block", new DestroyBlockCriterion());
	public static final InVicinityCriterion IN_VICINITY = Criteria.register("ttll:in_vicinity", new InVicinityCriterion());

	@Override
	public void onInitialize() {
		ModEntities.registerEntities();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModBlockEntities.register();
		ItemsThatCanHitAndBreak.register();
		Chopable.initialize();
		ModRecipeSerializers.register();
		ModDataComponentTypes.registerDataComponentTypes();
		ModSounds.registerSounds();
		OreWorldGen.registerWorldGen();


		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (server.getDefaultGameMode() == GameMode.SURVIVAL || server.isHardcore()) {
				for (ServerWorld world : server.getWorlds()) {
					enforceDefaultGamerules(world, server); // sets FALL_DAMAGE, etc.
				}
			}
		});

		FabricDefaultAttributeRegistry.register(ModEntities.FIRE_CREEPER, FireCreeperEntity.createCreeperAttributes());
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

		SpawnRestriction.register(
				ModEntities.FIRE_CREEPER,
				SpawnLocationTypes.ON_GROUND,
				Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
				HostileEntity::canSpawnIgnoreLightLevel
		);

		// Inside TougherThanLlamas.java -> onInitialize()



// 2. Inject into Biomes
		BiomeModifications.addSpawn(
				// Selector: Spawns in any Overworld biome that isn't a Mushroom Island
				BiomeSelectors.foundInOverworld().and(BiomeSelectors.excludeByKey(BiomeKeys.MUSHROOM_FIELDS)),
				SpawnGroup.MONSTER,
				ModEntities.FIRE_CREEPER,
				25, // Weight (1/4 of vanilla Creeper's 100)
				1,  // Min group size
				2   //ze
		);

		// If using Fabric API
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				GloomHelper.updateGloom(player);
			}
		});



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