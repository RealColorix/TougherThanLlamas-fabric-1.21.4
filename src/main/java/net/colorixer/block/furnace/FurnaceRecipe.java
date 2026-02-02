package net.colorixer.block.furnace;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public record FurnaceRecipe(Item input, ItemStack output, int cookTime) {}
