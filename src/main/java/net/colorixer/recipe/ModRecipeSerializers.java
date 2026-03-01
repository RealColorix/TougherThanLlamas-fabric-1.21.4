package net.colorixer.recipe;

import com.mojang.serialization.MapCodec;
import net.colorixer.mixin.ShapelessRecipeAccessor;
import net.colorixer.recipe.tool_damage_crafting.ToolDamageRecipe;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ModRecipeSerializers {

    public static final RecipeSerializer<ShapelessRecipe> TOOL_DAMAGE_CRAFTING_SERIALIZER = new RecipeSerializer<ShapelessRecipe>() {

        private final MapCodec<ShapelessRecipe> CODEC = RecipeSerializer.SHAPELESS.codec().xmap(
                base -> new ToolDamageRecipe(
                        base.getGroup(),
                        base.getCategory(),
                        ((ShapelessRecipeAccessor) base).ttll$getResult(),
                        ((ShapelessRecipeAccessor) base).ttll$getIngredients()
                ),
                custom -> custom
        );

        private final PacketCodec<RegistryByteBuf, ShapelessRecipe> PACKET_CODEC = RecipeSerializer.SHAPELESS.packetCodec().xmap(
                base -> new ToolDamageRecipe(
                        base.getGroup(),
                        base.getCategory(),
                        ((ShapelessRecipeAccessor) base).ttll$getResult(),
                        DefaultedList.copyOf(null, ((ShapelessRecipeAccessor) base).ttll$getIngredients().toArray(new net.minecraft.recipe.Ingredient[0]))
                ),
                custom -> custom
        );

        @Override
        public MapCodec<ShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, ShapelessRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    };

    public static void register() {
        Registry.register(Registries.RECIPE_SERIALIZER,
                Identifier.of("ttll", "tool_damage"),
                TOOL_DAMAGE_CRAFTING_SERIALIZER);
    }
}