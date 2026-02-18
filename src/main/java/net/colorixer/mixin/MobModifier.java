package net.colorixer.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobModifier {

    @Inject(method = "isInAttackRange", at = @At("HEAD"), cancellable = true)
    private void ttll$increaseZombieAttackRange(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof ZombieEntity zombie) {
            ItemStack stack = zombie.getMainHandStack();

            // 1. ELITE REACH: Iron or Better Sword/Shovel (3.0 blocks)
            if (isIronOrBetter(stack) && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof ShovelItem)) {
                double distanceSq = zombie.squaredDistanceTo(target);
                // 3.0 squared = 9.0
                cir.setReturnValue(distanceSq <= 4.0);
            }


        }
    }

    @Inject(method = "equipLootStack", at = @At("TAIL"))
    private void ttll$forceDropChanceOnPickup(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        // Force the 2.0F drop chance whenever ANY mob picks up an item
        ((MobEntityAccessor) this).callUpdateDropChances(slot);
    }

    private boolean isIronOrBetter(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof MiningToolItem || item instanceof SwordItem) {
            String name = item.toString();
            return name.contains("iron_sword")||
                    name.contains("iron_shovel");
        }
        return false;
    }


}