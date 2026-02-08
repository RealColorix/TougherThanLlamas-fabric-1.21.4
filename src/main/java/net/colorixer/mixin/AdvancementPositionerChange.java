package net.colorixer.mixin;

import net.minecraft.advancement.AdvancementPositioner;
import net.minecraft.advancement.PlacedAdvancement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AdvancementPositioner.class)
public abstract class AdvancementPositionerChange {

    @Shadow abstract void apply();

    /**
     * @author YourName
     * @reason Bypass vanilla tree math and go straight to our manual positioning
     */
    @Overwrite
    public static void arrangeForTree(PlacedAdvancement root) {
        if (root.getAdvancement().display().isEmpty()) {
            throw new IllegalArgumentException("Can't position children of an invisible root!");
        } else {
            // We still create the positioner so the tree structure exists
            AdvancementPositioner positioner = new AdvancementPositioner(root, null, null, 1, 0);

            // WE SKIP: calculateRecursively()
            // WE SKIP: findMinRowRecursively()
            // WE SKIP: increaseRowRecursively()

            // GO STRAIGHT TO APPLY: This triggers your Redirect in the other Mixin
            ((AdvancementPositionerAccessor)positioner).invokeApply();
        }
    }
}