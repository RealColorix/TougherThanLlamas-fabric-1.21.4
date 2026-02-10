package net.colorixer.block.campfire;

import net.colorixer.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import java.util.List;

public class CampfireRecipes {
    public static final List<CampfireRecipe> RECIPES = List.of(
            new CampfireRecipe(Items.BEEF, new ItemStack(Items.COOKED_BEEF), 6000, new ItemStack(Items.COOKED_BEEF), 1800),
            new CampfireRecipe(Items.PORKCHOP, new ItemStack(Items.COOKED_PORKCHOP), 6000, new ItemStack(Items.COOKED_BEEF), 1800),
            new CampfireRecipe(Items.MUTTON,new ItemStack(Items.COOKED_MUTTON), 6000, new ItemStack(Items.COOKED_MUTTON), 1800),
            new CampfireRecipe(Items.CHICKEN, new ItemStack(Items.COOKED_CHICKEN), 6000, new ItemStack(Items.COOKED_CHICKEN), 1800),
            new CampfireRecipe(Items.COD, new ItemStack(Items.COOKED_COD), 6000, new ItemStack(Items.COOKED_COD), 1800),
            new CampfireRecipe(Items.SALMON, new ItemStack(Items.COOKED_SALMON), 6000, new ItemStack(Items.COOKED_SALMON), 1800),
            new CampfireRecipe(Items.RABBIT, new ItemStack(Items.COOKED_RABBIT), 6000, new ItemStack(Items.COOKED_RABBIT), 1800)
    );

    public static CampfireRecipe get(ItemStack stack) {
        for (CampfireRecipe r : RECIPES) {
            if (stack.isOf(r.input())) return r;
        }
        return null;
    }
}