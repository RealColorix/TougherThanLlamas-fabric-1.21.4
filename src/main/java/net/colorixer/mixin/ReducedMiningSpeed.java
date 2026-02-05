package net.colorixer.mixin;

import net.colorixer.block.ModBlocks;
import net.colorixer.item.ModItems;
import net.colorixer.util.IdentifierUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class ReducedMiningSpeed {


    private static final TagKey<Item> BEGINNER_MINING_TOOLS = TagKey.of(
            RegistryKeys.ITEM,
            IdentifierUtil.createIdentifier("ttll", "beginner_stone_mining_tools")
    );

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyMiningSpeed(BlockState blockState, CallbackInfoReturnable<Float> cir) {
        Float originalSpeed = cir.getReturnValue();
        if (originalSpeed == null || originalSpeed <= 0.0f) return;

        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack heldItem = player.getMainHandStack();
        Item tool = heldItem.getItem();

        float baseSpeedMod = 0.4f;

        /* ============================= */
        /* HEALTH & HUNGER PENALTY     */
        /* ============================= */
        float health = player.getHealth();
        int hunger = player.getHungerManager().getFoodLevel();
        float penaltyMultiplier = 1.0f;

        // Health Tiers: 15, 10, 5
        if (health <= 12.1) penaltyMultiplier *= 0.875f;
        if (health <= 8.1) penaltyMultiplier *= 0.75f;
        if (health <= 4.1)  penaltyMultiplier *= 0.625f;
        if (health <= 2.1)  penaltyMultiplier *= 0.5f;

        // Hunger Tiers: 15, 10, 5
        if (hunger <= 12.1) penaltyMultiplier *= 0.9f;
        if (hunger <= 8.1) penaltyMultiplier *= 0.8f;
        if (hunger <= 4.1)  penaltyMultiplier *= 0.7f;
        if (hunger <= 2.1)  penaltyMultiplier *= 0.6f;
        // Combine the base 0.4 with the calculated penalties
        float finalSpeedMod = baseSpeedMod * penaltyMultiplier;
        /* ============================= */
        /*   SPECIAL TOOL RULES          */
        /* ============================= */

        /* ============================= */
        /* SPECIAL TOOL RULES          */
        /* ============================= */

        // Now checks for any tool in the beginner tag when mining logs
        if (heldItem.isIn(BEGINNER_MINING_TOOLS) && blockState.isOf(Blocks.IRON_ORE) || blockState.isOf(Blocks.COPPER_ORE)) {
            cir.setReturnValue(originalSpeed * finalSpeedMod);
            return;
        }

        if (heldItem.isOf(ModItems.IRON_CHISEL))
         {
             if(blockState.isOf(Blocks.IRON_ORE)
                     || blockState.isOf(Blocks.COPPER_ORE)
                     || blockState.isOf(Blocks.GOLD_ORE)
                     || blockState.isOf(Blocks.LAPIS_ORE)
                     || blockState.isOf(Blocks.REDSTONE_ORE)
                     || blockState.isOf(Blocks.EMERALD_ORE)
                     || blockState.isOf(Blocks.DIAMOND_ORE)
                     || blockState.isOf(Blocks.DEEPSLATE_IRON_ORE)
                     || blockState.isOf(Blocks.DEEPSLATE_COPPER_ORE)
                     || blockState.isOf(Blocks.DEEPSLATE_GOLD_ORE)
                     || blockState.isOf(Blocks.DEEPSLATE_LAPIS_ORE)
                     || blockState.isOf(Blocks.DEEPSLATE_REDSTONE_ORE)
                     || blockState.isOf(Blocks.DEEPSLATE_EMERALD_ORE)
                     || blockState.isOf(Blocks.DEEPSLATE_DIAMOND_ORE)){
             cir.setReturnValue(originalSpeed * finalSpeedMod);
            return;
        }else if (blockState.isOf(ModBlocks.BIRCH_BOTTOM_LOG)||blockState.isOf(ModBlocks.OAK_BOTTOM_LOG)||blockState.isOf(ModBlocks.BIRCH_BOTTOM_LOG_CHISELED)||blockState.isOf(ModBlocks.OAK_BOTTOM_LOG_CHISELED))
             {
                 cir.setReturnValue(originalSpeed * finalSpeedMod * 0.5f);
                 return;
             }
         }


        if (tool == ModItems.SHARP_ROCK && blockState.isIn(BlockTags.LOGS)) {
            cir.setReturnValue(originalSpeed * finalSpeedMod * 4f);
            return;
        }


        /* ============================= */
        /*   TIER ENFORCEMENT (FIX)      */
        /* ============================= */

        if (blockState.isToolRequired()) {

            boolean correctToolType = heldItem.isSuitableFor(blockState);

            // Wrong tool entirely
            if (!correctToolType && !EXCEPTION_BLOCKS.contains(blockState.getBlock())) {
                cir.setReturnValue(originalSpeed * 0.01f);
                return;
            }

            // Correct tool type but tier too low
            if (correctToolType && !toolMeetsTierRequirement(tool, blockState)) {
                cir.setReturnValue(originalSpeed * 0.04f);
                return;
            }
        }

        /* ============================= */
        /*   GLOBAL SLOWDOWN             */
        /* ============================= */

        cir.setReturnValue(originalSpeed * finalSpeedMod);
    }

    /* ============================= */
    /*   TIER CHECK (1.21.4 SAFE)    */
    /* ============================= */

    private boolean toolMeetsTierRequirement(Item tool, BlockState state) {



        // Diamond-required blocks
        if (state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return tool == Items.DIAMOND_PICKAXE || tool == Items.NETHERITE_PICKAXE;
        }

        // Iron-required blocks
        if (state.isIn(BlockTags.NEEDS_IRON_TOOL)) {
            return tool == Items.IRON_PICKAXE
                    || tool == Items.DIAMOND_PICKAXE
                    || tool == Items.NETHERITE_PICKAXE;
        }

        // Stone-required blocks
        if (state.isIn(BlockTags.NEEDS_STONE_TOOL)) {
            return tool == Items.STONE_PICKAXE
                    || tool == Items.IRON_PICKAXE
                    || tool == Items.DIAMOND_PICKAXE
                    || tool == Items.NETHERITE_PICKAXE;
        }

        return true;
    }

    private static final List<Block> EXCEPTION_BLOCKS = List.of(
            Blocks.SNOW,
            Blocks.SNOW_BLOCK,
            Blocks.POWDER_SNOW
    );
}