package net.colorixer.item.items;

import net.colorixer.block.BlockTags;
import net.minecraft.item.Item;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;

public class DraxItem extends MiningToolItem {
    public DraxItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Settings settings) {
        super(material, BlockTags.DRAX_MINEABLE, attackDamage, attackSpeed, settings);
    }
}
