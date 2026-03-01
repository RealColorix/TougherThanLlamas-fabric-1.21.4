package net.colorixer.mixin;

import java.util.List;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputSlotFiller.class)
public abstract class RecipeBookAutoFillMixin {

    @Shadow private List<Slot> inputSlots;
    @Shadow private boolean craftAll;
    @Shadow private PlayerInventory inventory;

    // Redirect the max count check to match your 127 limit
    @Redirect(
            method = "fill(Lnet/minecraft/recipe/RecipeEntry;Lnet/minecraft/recipe/RecipeFinder;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I")
    )
    private int ttll$nukeSanityCheck(ItemStack stack) {
        if (!stack.isEmpty() && stack.isDamageable()) {
            return 127; // MATCH THE 127 LIMIT
        }
        return stack.getMaxCount();
    }

    @Inject(method = "clampToMaxCount(ILjava/util/List;)I", at = @At("HEAD"), cancellable = true)
    private static void ttll$unclampTools(int count, List<RegistryEntry<Item>> entries, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(count); // Return whatever the book requested (up to 127)
    }

    @Inject(method = "calculateCraftAmount", at = @At("HEAD"), cancellable = true)
    private void ttll$ignoreToolsForAmount(int forCraftAll, boolean match, CallbackInfoReturnable<Integer> cir) {
        if (this.craftAll) {
            cir.setReturnValue(forCraftAll);
            return;
        }

        if (match) {
            int currentMin = Integer.MAX_VALUE;
            boolean foundLogs = false;

            for (Slot slot : this.inputSlots) {
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty() && !stack.isDamageable()) {
                    currentMin = Math.min(currentMin, stack.getCount());
                    foundLogs = true;
                }
            }

            if (foundLogs && currentMin != Integer.MAX_VALUE) {
                cir.setReturnValue(currentMin + 1);
                return;
            }
        }
    }

    @Inject(method = "fillInputSlot", at = @At("HEAD"), cancellable = true)
    private void ttll$pullOnlyOneTool(Slot slot, RegistryEntry<Item> item, int count, CallbackInfoReturnable<Integer> cir) {
        if (item.value().getDefaultStack().isDamageable()) {
            if (slot.getStack().isEmpty()) {
                int invSlot = this.inventory.getMatchingSlot(item, ItemStack.EMPTY);
                if (invSlot != -1) {
                    slot.setStackNoCallbacks(this.inventory.removeStack(invSlot, 1));
                }
            }
            cir.setReturnValue(0);
        }
    }
}