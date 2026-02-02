package net.colorixer.mixin;

import net.colorixer.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.inventory.RecipeInputInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
public class CraftingSoundMixin {

    @Shadow @Final private PlayerEntity player;
    @Shadow @Final private RecipeInputInventory input;

    @Inject(
            method = "onTakeItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD")
    )
    private void ttll$playToolDamageSound(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        boolean isCustomToolPresent = false;

        // Manually check the grid for your tools
        for (int i = 0; i < this.input.size(); i++) {
            ItemStack slotStack = this.input.getStack(i);
            if (slotStack.isOf(ModItems.FLINT_AXE) || slotStack.isOf(ModItems.SHARP_ROCK)) {
                isCustomToolPresent = true;
                break;
            }
        }

        // If your tool was in the grid, play the sound
        if (isCustomToolPresent) {
            player.getWorld().playSound(
                    null,
                    player.getBlockPos(),
                    SoundEvents.ITEM_AXE_STRIP,
                    SoundCategory.PLAYERS,
                    1.0F, 1.0F
            );
        }
    }
}