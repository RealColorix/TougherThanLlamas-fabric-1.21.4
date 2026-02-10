package net.colorixer.block.campfire;

import net.colorixer.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import java.util.List;

public class CampfireRecipes {
    public static final List<CampfireRecipe> RECIPES = List.of(
            new CampfireRecipe(Items.BEEF, new ItemStack(Items.COOKED_BEEF), 600, new ItemStack(Items.COOKED_BEEF), 300),
            new CampfireRecipe(Items. MUTTON,new ItemStack(Items.COOKED_MUTTON), 600, new ItemStack(Items.COOKED_MUTTON), 600),
            new CampfireRecipe(Items.CHICKEN, new ItemStack(Items.COOKED_CHICKEN), 600, new ItemStack(Items.COOKED_CHICKEN), 600),
            new CampfireRecipe(Items.COD, new ItemStack(Items.COOKED_COD), 600, new ItemStack(Items.COOKED_COD), 600),
            new CampfireRecipe(Items.SALMON, new ItemStack(Items.COOKED_SALMON), 600, new ItemStack(Items.COOKED_SALMON), 600),
            new CampfireRecipe(Items.RABBIT, new ItemStack(Items.COOKED_RABBIT), 600, new ItemStack(Items.COOKED_RABBIT), 600)
    );

    public static CampfireRecipe get(ItemStack stack) {
        for (CampfireRecipe r : RECIPES) {
            if (stack.isOf(r.input())) return r;
        }
        return null;
    }
}