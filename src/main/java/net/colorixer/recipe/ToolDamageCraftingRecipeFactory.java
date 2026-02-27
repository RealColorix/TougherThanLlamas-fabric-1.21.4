package net.colorixer.recipe;

import net.colorixer.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.CraftingRecipeCategory;

import java.util.List;

public class ToolDamageCraftingRecipeFactory {

    public static ToolDamageCraftingRecipe create(
            CraftingRecipeCategory category
    ) {
        return new ToolDamageCraftingRecipe(
                category,
                List.of(
                        new ToolDamageRule(
                                Items.OAK_LOG,
                                ModItems.FLINT_AXE,
                                new ItemStack(ModItems.OAK_FIREWOOD, 2)
                        ),
                        new ToolDamageRule(
                                Items.BIRCH_LOG,
                                ModItems.FLINT_AXE,
                                new ItemStack(ModItems.BIRCH_FIREWOOD, 2)
                        ),
                        new ToolDamageRule(
                                Items.SPRUCE_LOG,
                                ModItems.FLINT_AXE,
                                new ItemStack(ModItems.SPRUCE_FIREWOOD, 2)
                        ),
                        new ToolDamageRule(
                                Items.JUNGLE_LOG,
                                ModItems.FLINT_AXE,
                                new ItemStack(ModItems.JUNGLE_FIREWOOD, 2)
                        ),
                        new ToolDamageRule(
                                ModItems.OAK_FIREWOOD,
                                ModItems.FLINT_AXE,
                                new ItemStack(Items.STICK, 2)
                        ),
                        new ToolDamageRule(
                                ModItems.BIRCH_FIREWOOD,
                                ModItems.FLINT_AXE,
                                new ItemStack(Items.STICK, 2)
                        ),
                        new ToolDamageRule(
                                ModItems.SPRUCE_FIREWOOD,
                                ModItems.FLINT_AXE,
                                new ItemStack(Items.STICK, 2)
                        ),
                        new ToolDamageRule(
                                ModItems.JUNGLE_FIREWOOD,
                                ModItems.FLINT_AXE,
                                new ItemStack(Items.STICK, 2)
                        ),
                        new ToolDamageRule(
                                ModItems.OAK_FIREWOOD,
                                ModItems.SHARP_ROCK,
                                new ItemStack(Items.STICK, 1)
                        ),
                        new ToolDamageRule(
                                ModItems.BIRCH_FIREWOOD,
                                ModItems.SHARP_ROCK,
                                new ItemStack(Items.STICK, 1)
                        ),
                        new ToolDamageRule(
                                ModItems.SPRUCE_FIREWOOD,
                                ModItems.SHARP_ROCK,
                                new ItemStack(Items.STICK, 1)
                        ),
                        new ToolDamageRule(
                                ModItems.JUNGLE_FIREWOOD,
                                ModItems.SHARP_ROCK,
                                new ItemStack(Items.STICK, 1)
                        ),
                        new ToolDamageRule(
                                ModItems.BRANCH,
                                ModItems.SHARP_ROCK,
                                new ItemStack(Items.STICK, 1)
                        ),
                        new ToolDamageRule(
                                ModItems.BRANCH,
                                ModItems.FLINT_AXE,
                                new ItemStack(Items.STICK, 1)
                        ),
                        new ToolDamageRule(
                                Items.STICK,
                                ModItems.SHARP_ROCK,
                                new ItemStack(ModItems.POINTY_STICK, 1)
                        ),
                        new ToolDamageRule(
                                Items.STICK,
                                ModItems.FLINT_AXE,
                                new ItemStack(ModItems.POINTY_STICK, 1)
                        ),
                        new ToolDamageRule(
                                Items.OAK_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.DARK_OAK_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.DARK_OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.PALE_OAK_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.PALE_OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.BIRCH_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.BIRCH_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.SPRUCE_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.SPRUCE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.JUNGLE_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.JUNGLE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.ACACIA_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.ACACIA_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.CHERRY_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.CHERRY_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.MANGROVE_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.MANGROVE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_OAK_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_DARK_OAK_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.DARK_OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_PALE_OAK_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.PALE_OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_BIRCH_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.BIRCH_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_SPRUCE_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.SPRUCE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_JUNGLE_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.JUNGLE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_ACACIA_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.ACACIA_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_CHERRY_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.CHERRY_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_MANGROVE_LOG,
                                Items.IRON_AXE,
                                new ItemStack(Items.MANGROVE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.OAK_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.DARK_OAK_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.DARK_OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.PALE_OAK_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.PALE_OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.BIRCH_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.BIRCH_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.SPRUCE_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.SPRUCE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.JUNGLE_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.JUNGLE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.ACACIA_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.ACACIA_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.CHERRY_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.CHERRY_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.MANGROVE_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.MANGROVE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_OAK_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_DARK_OAK_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.DARK_OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_PALE_OAK_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.PALE_OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_BIRCH_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.BIRCH_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_SPRUCE_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.SPRUCE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_JUNGLE_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.JUNGLE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_ACACIA_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.ACACIA_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_CHERRY_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.CHERRY_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_MANGROVE_LOG,
                                Items.DIAMOND_AXE,
                                new ItemStack(Items.MANGROVE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.OAK_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.DARK_OAK_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.DARK_OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.PALE_OAK_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.PALE_OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.BIRCH_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.BIRCH_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.SPRUCE_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.SPRUCE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.JUNGLE_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.JUNGLE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.ACACIA_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.ACACIA_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.CHERRY_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.CHERRY_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.MANGROVE_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.MANGROVE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_OAK_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_DARK_OAK_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.DARK_OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_PALE_OAK_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.PALE_OAK_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_BIRCH_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.BIRCH_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_SPRUCE_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.SPRUCE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_JUNGLE_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.JUNGLE_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_ACACIA_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.ACACIA_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_CHERRY_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.CHERRY_PLANKS, 1)
                        ),
                        new ToolDamageRule(
                                Items.STRIPPED_MANGROVE_LOG,
                                Items.NETHERITE_AXE,
                                new ItemStack(Items.MANGROVE_PLANKS, 1)
                        )
//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
                        ,
                        new ToolDamageRule(
                                ModItems.TANGLED_WEB,
                                Items.SHEARS,
                                new ItemStack(Items.STRING, 1)
                        ),
                        new ToolDamageRule(
                                Items.COBWEB,
                                Items.SHEARS,
                                new ItemStack(Items.STRING, 2)
                        ),
                        new ToolDamageRule(
                                Items.SUGAR_CANE,
                                ModItems.SHARP_ROCK,
                                new ItemStack(ModItems.SUGAR_CANE_MASH, 1)
                        ),
                        new ToolDamageRule(
                                Items.SUGAR_CANE,
                                ModItems.FLINT,
                                new ItemStack(ModItems.SUGAR_CANE_MASH, 1)
                        )


                )
        );
    }
}
