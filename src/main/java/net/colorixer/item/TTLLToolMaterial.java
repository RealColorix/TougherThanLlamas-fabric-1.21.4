package net.colorixer.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
public class TTLLToolMaterial {

    //Durability Speed Extra-damage Enchantability

    /** ALL DIFFERENT TOOL MATERIALS */

    //  FLINT   --------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final ToolMaterial FLINT_FOR_AXE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 7,2.0F,  0.5F, 8, ItemTags.WOODEN_TOOL_MATERIALS);
    public static final ToolMaterial FLINT_TWINE_FOR_AXE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 29,2.0F,  0.5F, 8, ItemTags.WOODEN_TOOL_MATERIALS);
    public static final ToolMaterial FLINT_STRING_FOR_AXE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 53,2.0F,  0.5F, 8, ItemTags.WOODEN_TOOL_MATERIALS);
    public static final ToolMaterial FLINT_LEATHER_GRIP_FOR_AXE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 10,2.5F,  0.5F, 8, ItemTags.WOODEN_TOOL_MATERIALS);
    public static final ToolMaterial FLINT_TWINE_LEATHER_GRIP_FOR_AXE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 35,2.5F,  0.5F, 8, ItemTags.WOODEN_TOOL_MATERIALS);
    public static final ToolMaterial FLINT_STRING_LEATHER_GRIP_FOR_AXE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 60,2.5F,  0.5F, 8, ItemTags.WOODEN_TOOL_MATERIALS);

    //  CRUDE   --------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final ToolMaterial CRUDE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 12,1.5F,  0.0F, 8, ItemTags.WOODEN_TOOL_MATERIALS);

}
