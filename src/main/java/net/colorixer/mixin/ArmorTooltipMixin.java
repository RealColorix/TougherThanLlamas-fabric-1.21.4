package net.colorixer.mixin;

import net.colorixer.player.ArmorWeight;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ArmorTooltipMixin {

    // Target the specific method you found in your decompile
    @Inject(
            method = "appendAttributeModifiersTooltip",
            at = @At("TAIL")
    )
    private void ttll$injectWeightAttribute(Consumer<Text> textConsumer, @Nullable PlayerEntity player, CallbackInfo ci) {
        // 'this' is the ItemStack
        ItemStack stack = (ItemStack)(Object)this;

        // Get weight from your map class
        int weight = ArmorWeight.get(stack.getItem());

        if (weight > 0) {
            // Adds a blank line for spacing (vanilla style)

            // Adds the "Weight" line formatted to look like an attribute
            // Using DARK_GREEN makes it stand out from blue Armor stats
            textConsumer.accept(Text.translatable("tooltip.ttll.weight", weight)
                    .formatted(Formatting.BLUE));
        }
    }
}