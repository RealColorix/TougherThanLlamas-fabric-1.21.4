package net.colorixer.item.items;

import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;


public class HoeItem extends MiningToolItem {


    public HoeItem(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(material, BlockTags.HOE_MINEABLE, attackDamage, attackSpeed, settings);
    }


}
