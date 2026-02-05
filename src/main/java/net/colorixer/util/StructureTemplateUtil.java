package net.colorixer.util;

import net.colorixer.block.ModBlocks;
import net.colorixer.block.torch.CrudeTorchBlock;
import net.colorixer.mixin.StructureBlockInfoAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Random;

public class StructureTemplateUtil {
    private static final long RADIUS_SQR = 2500L * 2500L;
    private static final Random RANDOM = new Random();

    public static BlockState getReplacementState(BlockState original, BlockPos worldPos, List<StructureTemplate.StructureBlockInfo> allBlocks, NbtCompound nbt) {
        long x = worldPos.getX();
        long z = worldPos.getZ();
        boolean inRadius = (x * x + z * z) <= RADIUS_SQR;

        // --- 1. THE TORCH FIX (Underground Y < 62 or In Radius) ---
        if (original.isOf(Blocks.TORCH) || original.isOf(Blocks.WALL_TORCH)) {
            if (inRadius) {
                BlockState burned = ModBlocks.CRUDE_TORCH.getDefaultState().with(CrudeTorchBlock.BURNED, true);

                if (original.isOf(Blocks.WALL_TORCH)) {
                    Direction suggested = original.get(Properties.HORIZONTAL_FACING);
                    if (isSupportBlockPresent(worldPos, suggested, allBlocks)) {
                        return burned.with(CrudeTorchBlock.FACING, suggested);
                    }

                    for (Direction dir : Direction.Type.HORIZONTAL) {
                        if (isSupportBlockPresent(worldPos, dir, allBlocks)) {
                            return burned.with(CrudeTorchBlock.FACING, dir);
                        }
                    }
                }
                return burned.with(CrudeTorchBlock.FACING, Direction.UP);
            }
        }

        // --- 2. THE FURNACE FIX ---
        if (original.isOf(Blocks.FURNACE) || original.isOf(Blocks.SMOKER) || original.isOf(Blocks.BLAST_FURNACE)) {
            if (inRadius) {
                return Blocks.COBBLESTONE.getDefaultState();
            } else {
                Direction furnaceFacing = original.contains(Properties.HORIZONTAL_FACING)
                        ? original.get(Properties.HORIZONTAL_FACING) : Direction.NORTH;
                // Using ModBlocks.FURNACE as per your snippet
                return ModBlocks.FURNACE.getDefaultState().with(Properties.HORIZONTAL_FACING, furnaceFacing);
            }
        }

        // --- 3. RADIUS SPECIFIC ---
        if (inRadius) {

            if (original.isOf(Blocks.DIRT_PATH)) {
                int chance = RANDOM.nextInt(11);

                if (chance < 2) {
                    // 5/11 Chance for normal Dirt
                    return Blocks.DIRT.getDefaultState();
                } else if (chance < 5) {
                    // 5/11 Chance to stay/become Coarse Dirt
                    return Blocks.GRASS_BLOCK.getDefaultState();
                }else if (chance < 10) {
                    // 5/11 Chance to stay/become Coarse Dirt
                    return Blocks.COARSE_DIRT.getDefaultState();
                } else {
                    // 1/11 Chance for Rooted Dirt
                    return Blocks.ROOTED_DIRT.getDefaultState();
                }
            }
            // Targeted Chest Removal via LootTable
            if (original.isOf(Blocks.CHEST) && nbt != null && nbt.contains("LootTable", 8)) {
                String loot = nbt.getString("LootTable");
                if (loot.contains("village") ||
                        loot.contains("shipwreck") ||
                        loot.contains("ruined_portal") ||
                        loot.contains("desert_pyramid") ||
                        loot.contains("jungle_pyramid") ||
                        loot.contains("pillager_outpost") ||
                        loot.contains("buried_treasure")) {

                    return Blocks.AIR.getDefaultState();
                }
            }

            // Radius Global Deletion (Ghost Town cleanup)
            if (original.isIn(BlockTags.BEDS)  ||
                    original.isOf(Blocks.BREWING_STAND) || original.isOf(Blocks.CAULDRON) ||
                    original.isOf(Blocks.WATER_CAULDRON) || original.isIn(BlockTags.WOODEN_DOORS) ||
                    original.isOf(Blocks.CRAFTING_TABLE) || original.isOf(Blocks.BOOKSHELF) ||
                    original.isOf(Blocks.LECTERN) || original.isOf(Blocks.COMPOSTER) ||
                    original.isOf(Blocks.GRINDSTONE) || original.isOf(Blocks.SMITHING_TABLE) ||
                    original.isOf(Blocks.ANVIL) || original.isOf(Blocks.CHIPPED_ANVIL) ||
                    original.isOf(Blocks.DAMAGED_ANVIL) || original.isOf(Blocks.CAMPFIRE) ||
                    original.isOf(Blocks.HAY_BLOCK) || original.isOf(Blocks.STONECUTTER)||
                    original.isOf(Blocks.FLOWER_POT)||original.isOf(Blocks.LOOM)||
                    original.isOf(Blocks.FLETCHING_TABLE)||original.isOf(Blocks.CARTOGRAPHY_TABLE)||
                    original.isOf(Blocks.WATER)||original.isOf(Blocks.LANTERN)||
                    original.isOf(Blocks.BELL)||original.isIn(net.colorixer.block.BlockTags.GLASS)) {

                return Blocks.AIR.getDefaultState();
            }

            // Cobblestone Degradation
            if (original.isOf(Blocks.COBBLESTONE)) {
                // 50% chance to become Loose Cobblestone
                if (RANDOM.nextBoolean()) {
                    return ModBlocks.LOOSE_COBBLESTONE.getDefaultState();
                }
                // Of the remaining 50%, some stay Cobblestone, some become Andesite
                return RANDOM.nextInt(3) == 0 ? ModBlocks.WEATHERED_STONE.getDefaultState() : Blocks.COBBLESTONE.getDefaultState();
            }

            // Crops and Overgrowth
            if (original.isIn(BlockTags.CROPS) || original.isOf(Blocks.MELON) || original.isOf(Blocks.PUMPKIN)) {
                if (RANDOM.nextBoolean()) return Blocks.AIR.getDefaultState();
                int choice = RANDOM.nextInt(4);
                return switch (choice) {
                    case 0 -> Blocks.SHORT_GRASS.getDefaultState();
                    case 1 -> Blocks.POPPY.getDefaultState();
                    case 2 -> Blocks.DANDELION.getDefaultState();
                    default -> Blocks.FERN.getDefaultState();
                };
            }

            // Farmland Degradation
            if (original.isOf(Blocks.FARMLAND)) {
                return RANDOM.nextBoolean() ? Blocks.COARSE_DIRT.getDefaultState() : Blocks.DIRT.getDefaultState();
            }
        }

        return original;
    }

    private static boolean isSupportBlockPresent(BlockPos torchPos, Direction facing, List<StructureTemplate.StructureBlockInfo> allBlocks) {
        BlockPos wallPos = torchPos.offset(facing.getOpposite());
        for (StructureTemplate.StructureBlockInfo info : allBlocks) {
            StructureBlockInfoAccessor accessor = (StructureBlockInfoAccessor) (Object) info;
            if (accessor.getPos().equals(wallPos)) {
                return !accessor.getState().isAir();
            }
        }
        return false;
    }

    public static boolean shouldShowEntity(String id, BlockPos worldPos) {
        if (id == null) return true;
        long x = worldPos.getX();
        long z = worldPos.getZ();
        if ((x * x + z * z) <= RADIUS_SQR) {
            String lower = id.toLowerCase();
            if (lower.contains("villager") && !lower.contains("zombie")) return false;
            if (lower.contains("iron_golem") || lower.contains("cat")) return false;
            if (lower.contains("pig") || lower.contains("cow")) return false;
            if (lower.contains("sheep") || lower.contains("chicken")) return false;
            if (lower.contains("donkey") || lower.contains("horse")) return false;
        }
        return true;
    }
}