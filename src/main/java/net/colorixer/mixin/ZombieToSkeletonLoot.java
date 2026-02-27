package net.colorixer.mixin;

import net.colorixer.util.SkeletonConversionTracker;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ZombieToSkeletonLoot implements SkeletonConversionTracker {

    @Unique
    private boolean isConverting = false;

    @Override public void setConvertingToSkeleton(boolean converting) { this.isConverting = converting; }
    @Override public boolean isConvertingToSkeleton() { return this.isConverting; }

    // This handles the conversion logic
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void ttll$checkForZombieConversion(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof ZombieEntity zombie && !zombie.getWorld().isClient) {
            ServerWorld world = (ServerWorld) zombie.getWorld();
            //boolean inSunlight = world.isDay() && world.isSkyVisible(zombie.getBlockPos());

            if (zombie.isOnFire()
                   // && inSunlight
            ) {
                this.isConverting = true;
                ttll$performConversion(world, zombie);
            }
        }
    }

    @Unique
    private void ttll$performConversion(ServerWorld world, ZombieEntity zombie) {
        SkeletonEntity skeleton = EntityType.SKELETON.create(world, SpawnReason.CONVERSION);
        if (skeleton != null) {
            skeleton.refreshPositionAndAngles(zombie.getX(), zombie.getY(), zombie.getZ(), zombie.getYaw(), zombie.getPitch());
            skeleton.setHealth(skeleton.getMaxHealth() * 0.5f);

            // Flag the NEW skeleton as "Converted" so it follows the bone-only loot rule
            ((SkeletonConversionTracker) skeleton).setConvertingToSkeleton(true);

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = zombie.getEquippedStack(slot);
                if (!stack.isEmpty()) {
                    skeleton.equipStack(slot, stack.copy());
                    ((MobEntityAccessor)skeleton).callUpdateDropChances(slot);
                }
            }
            world.spawnEntityAndPassengers(skeleton);
            zombie.discard();
        }
    }

    // BLOCK ALL STANDARD LOOT
    @Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
    private void blockLootTable(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (this.isConverting) {
            // If it's a converted skeleton, drop 0-2 bones manually
            if ((Object)this instanceof SkeletonEntity) {
                int count = world.getRandom().nextBetween(0, 2);
                if (count > 0) {
                    ((LivingEntity)(Object)this).dropStack(world, new ItemStack(Items.BONE, count));
                }
            }
            ci.cancel(); // Kill the JSON loot table call
        }
    }

    // BLOCK ALL STANDARD EQUIPMENT (Already handled, but kept for safety)
    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void blockEquipmentDrop(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (this.isConverting) {
            ci.cancel();
        }
    }
}