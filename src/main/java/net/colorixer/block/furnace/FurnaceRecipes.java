package net.colorixer.block.furnace;

import net.colorixer.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

public class FurnaceRecipes {
    public static final List<FurnaceRecipe> RECIPES = List.of(
            new FurnaceRecipe(Items.RAW_IRON, new ItemStack(Items.IRON_NUGGET), 12000),
            new FurnaceRecipe(Items.RAW_COPPER, new ItemStack(ModItems.COPPER_DUST), 12000),
            new FurnaceRecipe(Items.RAW_GOLD, new ItemStack(Items.GOLD_NUGGET), 12000),

            //FOOD
            new FurnaceRecipe(Items.BEEF, new ItemStack(Items.COOKED_BEEF), 1800),
            new FurnaceRecipe(Items.PORKCHOP, new ItemStack(Items.COOKED_PORKCHOP), 1800),
            new FurnaceRecipe(Items.MUTTON, new ItemStack(Items.COOKED_MUTTON), 1800),
            new FurnaceRecipe(Items.CHICKEN, new ItemStack(Items.COOKED_CHICKEN), 1800),
            new FurnaceRecipe(Items.COD, new ItemStack(Items.COOKED_COD), 1800),
            new FurnaceRecipe(Items.SALMON, new ItemStack(Items.COOKED_SALMON), 1800),
            new FurnaceRecipe(Items.RABBIT, new ItemStack(Items.COOKED_RABBIT), 1800)

    );

    public static FurnaceRecipe get(ItemStack stack) {
        for (FurnaceRecipe r : RECIPES) {
            if (stack.isOf(r.input())) return r;
        }
        return null;
    }
}
