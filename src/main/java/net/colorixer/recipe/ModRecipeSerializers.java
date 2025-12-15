package net.colorixer.recipe;

import net.colorixer.TougherThanLlamas;
import net.colorixer.util.IdentifierUtil;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModRecipeSerializers {

    public static RecipeSerializer<? extends SpecialCraftingRecipe>
            TOOL_DAMAGE_CRAFTING_SERIALIZER;

    public static void register() {

        TOOL_DAMAGE_CRAFTING_SERIALIZER =
                new SpecialCraftingRecipe.SpecialRecipeSerializer<>(
                        (CraftingRecipeCategory category) ->
                                ToolDamageCraftingRecipeFactory.create(category)
                );

        Registry.register(
                Registries.RECIPE_SERIALIZER,
                IdentifierUtil.createIdentifier(
                        TougherThanLlamas.MOD_ID,
                        "tool_damage_crafting"
                ),
                TOOL_DAMAGE_CRAFTING_SERIALIZER
        );
    }
}
