package net.colorixer.mixin;

import it.unimi.dsi.fastutil.Stack;
import net.minecraft.advancement.AdvancementDisplays;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AdvancementDisplays.class)
public class AdvancementDisplaysMixin {

    /**
     * @author Colorixer
     * @reason Increase visibility depth from 2 to 3.
     */
    @Overwrite
    private static boolean shouldDisplay(Stack<?> statuses) {
        // We changed the loop from i <= 2 to i <= 3.
        // This allows the UI to show advancements that are 3 steps ahead.
        for (int i = 0; i <= 4; i++) {
            Object status = statuses.peek(i);

            // Since we can't name the private Enum 'Status', we check the string name.
            // This is a safe way to compare private Enum constants.
            if (status.toString().equals("SHOW")) {
                return true;
            }

            if (status.toString().equals("HIDE")) {
                return false;
            }
        }

        return false;
    }
}