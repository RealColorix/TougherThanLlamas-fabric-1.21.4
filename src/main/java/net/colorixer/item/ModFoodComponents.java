package net.colorixer.item;

import net.minecraft.component.type.FoodComponent;

public class ModFoodComponents {
    public static final FoodComponent BURNED_MEAT = new FoodComponent.Builder()
            .nutrition(3)
            .saturationModifier(0.0f)
            .build();
}