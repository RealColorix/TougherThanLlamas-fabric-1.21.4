package net.colorixer.block.campfire;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public record CampfireRecipe(Item input, ItemStack output, int cookTime, ItemStack burnOutput, int burnTime) {}