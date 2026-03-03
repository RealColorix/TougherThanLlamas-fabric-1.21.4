package net.colorixer.mixin.entities.hostile;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobPickupLogic {

    @Inject(method = "loot", at = @At("HEAD"), cancellable = true)
    private void ttll$tagPickedUpItems(ServerWorld world, ItemEntity item, CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;
        ItemStack stack = item.getStack();
        if (stack.isEmpty()) return;

        // 1. STRICT ARMOR CHECK: If slot is full, don't even touch it
        if (stack.getItem() instanceof ArmorItem) {
            EquipmentSlot slot = mob.getPreferredEquipmentSlot(stack);
            if (!mob.getEquippedStack(slot).isEmpty()) {
                ci.cancel();
                return;
            }
        }

        // 2. WEAPON SWAP DROP: If picking up a new item for the hand, drop the old one
        // This ensures nothing is deleted when they swap weapons
        ItemStack currentHand = mob.getMainHandStack();
        if (!currentHand.isEmpty() && !(stack.getItem() instanceof ArmorItem)) {
            // Only drop if the mob is actually going to pick this new item up
            // (Standard Minecraft prefersNewEquipment logic will run after this)
            mob.dropStack(world, currentHand);
            mob.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        // 3. Tagging logic
        boolean isFromPlayer = item.getOwner() != null;
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt ->
                nbt.putBoolean("belongsToPlayer", isFromPlayer));

        item.setStack(stack);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void ttll$universalItemHandler(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;
        if (mob.getWorld().isClient) return;

        if (!(mob instanceof AbstractSkeletonEntity || mob instanceof ZombieEntity)) return;

        if (!mob.canPickUpLoot()) mob.setCanPickUpLoot(true);

        ItemStack handStack = mob.getMainHandStack();
        if (!handStack.isEmpty() && handStack.getItem() instanceof ArmorItem) {
            EquipmentSlot slot = mob.getPreferredEquipmentSlot(handStack);
            ItemStack currentArmor = mob.getEquippedStack(slot);

            if (currentArmor.isEmpty()) {
                ItemStack newStack = handStack.copy();
                NbtComponent.set(DataComponentTypes.CUSTOM_DATA, newStack, nbt ->
                        nbt.putBoolean("belongsToPlayer", true));

                mob.equipStack(slot, newStack);
                mob.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                ((MobEntityAccessor) mob).callUpdateDropChances(slot);
            }
        }
    }

    @Inject(method = "canPickupItem", at = @At("HEAD"), cancellable = true)
    private void ttll$allowPickup(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        MobEntity mob = (MobEntity) (Object) this;
        if (!(mob instanceof AbstractSkeletonEntity || mob instanceof ZombieEntity)) return;

        if (stack.getItem() instanceof ArmorItem) {
            EquipmentSlot slot = mob.getPreferredEquipmentSlot(stack);
            if (!mob.getEquippedStack(slot).isEmpty()) {
                cir.setReturnValue(false);
                return;
            }
        }
        cir.setReturnValue(true);
    }
}