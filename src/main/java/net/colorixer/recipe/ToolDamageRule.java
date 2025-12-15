package net.colorixer.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public record ToolDamageRule(
        Item input,
        Item tool,
        ItemStack output
) {}
