package net.colorixer.player;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public final class ArmorWeight {

    public static int get(Item item) {
        if (item == Items.LEATHER_HELMET) return 1;
        if (item == Items.LEATHER_CHESTPLATE) return 2;
        if (item == Items.LEATHER_LEGGINGS) return 1;
        if (item == Items.LEATHER_BOOTS) return 1; //   5

        if (item == Items.GOLDEN_HELMET) return 3;
        if (item == Items.GOLDEN_CHESTPLATE) return 8;
        if (item == Items.GOLDEN_LEGGINGS) return 6;
        if (item == Items.GOLDEN_BOOTS) return 3; //    20

        if (item == Items.CHAINMAIL_HELMET) return 1;
        if (item == Items.CHAINMAIL_CHESTPLATE) return 3;
        if (item == Items.CHAINMAIL_LEGGINGS) return 2;
        if (item == Items.CHAINMAIL_BOOTS) return 2; // 8

        if (item == Items.IRON_HELMET) return 3;
        if (item == Items.IRON_CHESTPLATE) return 5;
        if (item == Items.IRON_LEGGINGS) return 4;
        if (item == Items.IRON_BOOTS) return 3;//       15

        if (item == Items.DIAMOND_HELMET) return 1;
        if (item == Items.DIAMOND_CHESTPLATE) return 4;
        if (item == Items.DIAMOND_LEGGINGS) return 3;
        if (item == Items.DIAMOND_BOOTS) return 2;//    10

        if (item == Items.NETHERITE_HELMET) return 3;
        if (item == Items.NETHERITE_CHESTPLATE) return 8;
        if (item == Items.NETHERITE_LEGGINGS) return 6;
        if (item == Items.NETHERITE_BOOTS) return 3;//  20


        return 0;
    }
}
