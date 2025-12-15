package net.colorixer.mixin;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.FoodComponents;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Rebalance vanilla food values (BTW-style).
 *
 * This modifies the FoodComponent passed into
 * Item.Settings.food(FoodComponent).
 */
@Mixin(Item.Settings.class)
public abstract class FoodNutrition {

    @ModifyVariable(
            method = "food",
            at = @At("HEAD"),
            argsOnly = true
    )
    private FoodComponent ttll$modifyFood(FoodComponent original) {

        if (original == FoodComponents.SWEET_BERRIES) {
            return new FoodComponent.Builder()
                    .nutrition(1)
                    .saturationModifier(0.0F)
                    .build();
        }
        if (original == FoodComponents.GLOW_BERRIES) {
            return new FoodComponent.Builder()
                    .nutrition(1)
                    .saturationModifier(0.0F)
                    .build();
        }

        // COW

        if (original == FoodComponents.BEEF) {
            return new FoodComponent.Builder()
                    .nutrition(3)
                    .saturationModifier(0.1666F) // 1 saturation
                    .build();
        }
        if (original == FoodComponents.COOKED_BEEF) {
            return new FoodComponent.Builder()
                    .nutrition(5)
                    .saturationModifier(0.25F) // 2 saturation
                    .build();
        }

        // PIG

        if (original == FoodComponents.PORKCHOP) {
            return new FoodComponent.Builder()
                    .nutrition(3)
                    .saturationModifier(0.1666F) // 1 saturation
                    .build();
        }

        if (original == FoodComponents.COOKED_PORKCHOP) {
            return new FoodComponent.Builder()
                    .nutrition(5)
                    .saturationModifier(0.25F) // 2 saturation
                    .build();
        }

        // MUTTON

        if (original == FoodComponents.MUTTON) {
            return new FoodComponent.Builder()
                    .nutrition(2)
                    .saturationModifier(0.25F) // 1 saturation
                    .build();
        }

        if (original == FoodComponents.COOKED_MUTTON) {
            return new FoodComponent.Builder()
                    .nutrition(4)
                    .saturationModifier(0.25F) // 2 saturation
                    .build();
        }

        return original;
    }
}
