package net.colorixer.mixin.options.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ShapelessRecipe.class)
public interface ShapelessRecipeAccessor {

    @Accessor("result")
    ItemStack ttll$getResult();

    @Accessor("ingredients")
    List<Ingredient> ttll$getIngredients();
}