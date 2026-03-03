package net.colorixer.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

public class EquipmentDropUtil {

    public static void dropAllMobEquipment(MobEntity mob, ServerWorld world, DamageSource source) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = mob.getEquippedStack(slot);
            if (stack.isEmpty()) continue;

            NbtComponent customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
            boolean belongsToPlayer = customData.contains("belongsToPlayer") && customData.copyNbt().getBoolean("belongsToPlayer");

            if (!belongsToPlayer) {
                if (stack.isDamageable()) {
                    applyExponentialDamage(stack, mob);
                }
            }


            if (isGear(stack)) {
                NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt ->
                        nbt.putBoolean("belongsToPlayer", true));
            } else {
                stack.remove(DataComponentTypes.CUSTOM_DATA);
            }

            mob.dropStack(world, stack);
            mob.equipStack(slot, ItemStack.EMPTY);
        }
    }

    // Helper method to determine if the item is actual equipment
    private static boolean isGear(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ArmorItem ||
                item instanceof SwordItem ||
                item instanceof MiningToolItem ||
                item instanceof BowItem ||
                item instanceof CrossbowItem ||
                item instanceof MaceItem ||
                item instanceof ShieldItem ||
                item instanceof TridentItem;
    }

    private static void applyExponentialDamage(ItemStack stack, MobEntity mob) {
        int maxDamage = stack.getMaxDamage();
        float remPerc = 0.1f + ((float) Math.pow(mob.getRandom().nextFloat(), 2.5) * 0.8f);
        stack.setDamage(MathHelper.clamp(Math.round(maxDamage * (1.0f - remPerc)), 0, maxDamage - 1));
    }
}