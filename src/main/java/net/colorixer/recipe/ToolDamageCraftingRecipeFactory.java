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
                                new ItemStack(Items.STICK, 4)
                        ),
                        new ToolDamageRule(
                                ModItems.BIRCH_FIREWOOD,
                                ModItems.FLINT_AXE,
                                new ItemStack(Items.STICK, 4)
                        ),
                        new ToolDamageRule(
                                ModItems.SPRUCE_FIREWOOD,
                                ModItems.FLINT_AXE,
                                new ItemStack(Items.STICK, 4)
                        ),
                        new ToolDamageRule(
                                ModItems.JUNGLE_FIREWOOD,
                                ModItems.FLINT_AXE,
                                new ItemStack(Items.STICK, 4)
                        )

                )
        );
    }
}
