package net.colorixer.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.List;

public class ToolDamageCraftingRecipe extends SpecialCraftingRecipe {

    private final List<ToolDamageRule> rules;

    public ToolDamageCraftingRecipe(
            CraftingRecipeCategory category,
            List<ToolDamageRule> rules
    ) {
        super(category);
        this.rules = rules;
    }

    @Override
    public boolean matches(CraftingRecipeInput inv, World world) {
        ItemStack foundInput = ItemStack.EMPTY;
        ItemStack foundTool  = ItemStack.EMPTY;

        // Scan ALL slots, order-independent
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            // Try to classify stack as input or tool using rules
            boolean matched = false;

            for (ToolDamageRule rule : rules) {
                if (stack.isOf(rule.input()) && foundInput.isEmpty()) {
                    foundInput = stack;
                    matched = true;
                    break;
                }

                if (stack.isOf(rule.tool()) && foundTool.isEmpty()) {
                    foundTool = stack;
                    matched = true;
                    break;
                }
            }

            // If stack matches neither input nor tool → invalid
            if (!matched) {
                return false;
            }
        }

        if (foundInput.isEmpty() || foundTool.isEmpty()) {
            return false;
        }

        // Confirm there is a rule that matches BOTH
        for (ToolDamageRule rule : rules) {
            if (foundInput.isOf(rule.input())
                    && foundTool.isOf(rule.tool())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ItemStack craft(
            CraftingRecipeInput inv,
            RegistryWrapper.WrapperLookup registries
    ) {
        ItemStack input = ItemStack.EMPTY;
        ItemStack tool  = ItemStack.EMPTY;

        // Order-independent detection
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            for (ToolDamageRule rule : rules) {
                if (stack.isOf(rule.input())) {
                    input = stack;
                } else if (stack.isOf(rule.tool())) {
                    tool = stack;
                }
            }
        }

        for (ToolDamageRule rule : rules) {
            if (input.isOf(rule.input())
                    && tool.isOf(rule.tool())) {
                return rule.output().copy();
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public DefaultedList<ItemStack> getRecipeRemainders(
            CraftingRecipeInput inv
    ) {
        DefaultedList<ItemStack> remainders =
                DefaultedList.ofSize(inv.size(), ItemStack.EMPTY);

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            for (ToolDamageRule rule : rules) {
                if (stack.isOf(rule.tool())) {
                    ItemStack damaged = stack.copy();
                    damaged.setDamage(damaged.getDamage() + 1);

                    if (damaged.getDamage() < damaged.getMaxDamage()) {
                        remainders.set(i, damaged);
                    }
                }
            }
        }

        return remainders;
    }

    @Override
    public RecipeType<CraftingRecipe> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer() {
        return ModRecipeSerializers.TOOL_DAMAGE_CRAFTING_SERIALIZER;
    }
}
