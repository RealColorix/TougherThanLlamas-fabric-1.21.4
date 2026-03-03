package net.colorixer.mixin.items.tools;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Item.Settings.class)
public abstract class IronDurabilityMixin {

    // Intercept the 'maxDamage' integer right as the item is being built
    @ModifyVariable(method = "maxDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int ttll$buffIronDurability(int originalMaxDamage) {

        // In vanilla Minecraft, Iron Tools (Sword, Pickaxe, Axe, Shovel, Hoe)
        // are the only tools assigned exactly 250 max damage upon creation.
        if (originalMaxDamage == 250) {
            return 398; // Your new custom durability!
        }

        // Let everything else (like Diamond at 1561) stay normal
        return originalMaxDamage;
    }
}