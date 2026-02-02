package net.colorixer.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
public class TTLLToolMaterial {

    //Durability Speed Extra-damage Enchantability

    /** ALL DIFFERENT TOOL MATERIALS */


    public static final ToolMaterial FLINT = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 2,0.666F,  0F, 1, ItemTags.WOODEN_TOOL_MATERIALS);

    public static final ToolMaterial SHARP_ROCK = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 8,1F,  0F, 1, ItemTags.WOODEN_TOOL_MATERIALS);

    public static final ToolMaterial STONE_AXE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 64,4F,  0F, 1, ItemTags.WOODEN_TOOL_MATERIALS);
    public static final ToolMaterial STONE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 64,1.5F,  0F, 1, ItemTags.WOODEN_TOOL_MATERIALS);





    //  WEAPONS  ----------------------------------------------------------------------------------------------------------------------------------------------------
                    //Durability Speed Extra-damage Enchantability

    public static final ToolMaterial WOODEN_CLUB = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 13,1F,  0.0F, 1, ItemTags.WOODEN_TOOL_MATERIALS);
    public static final ToolMaterial ZOMBIE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 32,1F,  0.0F, 1, ItemTags.WOODEN_TOOL_MATERIALS);
    public static final ToolMaterial BONE = new ToolMaterial(
            BlockTags.INCORRECT_FOR_WOODEN_TOOL, 71,1F,  0.0F, 1, ItemTags.WOODEN_TOOL_MATERIALS);





}
