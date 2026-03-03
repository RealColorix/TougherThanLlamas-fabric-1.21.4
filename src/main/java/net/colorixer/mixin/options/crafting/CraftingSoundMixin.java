package net.colorixer.mixin.options.crafting;

import net.colorixer.item.items.DraxItem;
import net.colorixer.item.items.FlintAxeItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.world.World;
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
            at = @At("RETURN")
    )
    private void ttll$playToolDamageSound(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        World world = player.getWorld();
        if (world.isClient) return;


        for (int i = 0; i < this.input.size(); i++) {
            ItemStack slotStack = this.input.getStack(i);

            if (!slotStack.isEmpty() && slotStack.isDamageable()) {
                Item item = slotStack.getItem();
                SoundEvent soundToPlay = SoundEvents.BLOCK_ANVIL_USE;

                // Dynamically pick the sound based on tool type
                if (item instanceof AxeItem|| item instanceof FlintAxeItem) {
                    soundToPlay = SoundEvents.ITEM_AXE_STRIP;
                } else if (item instanceof ShearsItem) {
                    soundToPlay = SoundEvents.ENTITY_SHEEP_SHEAR;
                } else if (item instanceof ShovelItem) {
                    soundToPlay = SoundEvents.ITEM_SHOVEL_FLATTEN;
                } else if (item instanceof HoeItem) {
                    soundToPlay = SoundEvents.ITEM_HOE_TILL;
                } else if (item instanceof PickaxeItem) {
                    soundToPlay = SoundEvents.BLOCK_STONE_HIT;
                } else if (item instanceof SwordItem) {
                    soundToPlay = SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP;
                }else if (item instanceof DraxItem) {
                    soundToPlay = SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP;
                }

                world.playSound(
                        null,
                        player.getBlockPos(),
                        soundToPlay,
                        SoundCategory.PLAYERS,
                        0.8F,
                        1.0F
                );
            }
        }
    }
}