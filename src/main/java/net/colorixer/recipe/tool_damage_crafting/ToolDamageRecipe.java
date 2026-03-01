package net.colorixer.recipe.tool_damage_crafting;

import net.colorixer.recipe.ModRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.util.collection.DefaultedList;

import java.util.List; // Make sure to import java.util.List

public class ToolDamageRecipe extends ShapelessRecipe {

    // Change DefaultedList to standard List here
    public ToolDamageRecipe(String group, CraftingRecipeCategory category, ItemStack result, List<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    @Override
    public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input) {
        // Keep this as DefaultedList! getRecipeRemainders still expects it.
        DefaultedList<ItemStack> remainders = DefaultedList.ofSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (stack.isDamageable()) {
                ItemStack damaged = stack.copy();
                damaged.setCount(1);
                damaged.setDamage(damaged.getDamage() + 1);

                if (damaged.getDamage() < damaged.getMaxDamage()) {
                    remainders.set(i, damaged);
                    continue;
                }
            }

            ItemStack remainder = stack.getRecipeRemainder();
            if (!remainder.isEmpty()) {
                remainders.set(i, remainder);
            }
        }
        return remainders;
    }

    @Override
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return ModRecipeSerializers.TOOL_DAMAGE_CRAFTING_SERIALIZER;
    }
}