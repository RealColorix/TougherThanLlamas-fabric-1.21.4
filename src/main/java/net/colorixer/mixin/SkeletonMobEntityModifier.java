package net.colorixer.mixin;

import net.colorixer.util.SkeletonConversionTracker;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class SkeletonMobEntityModifier {

    @Inject(method = "initialize", at = @At("TAIL"))
    private void ttll$initArrows(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        // Inside your initialize injection
        if ((Object) this instanceof SkeletonEntity skeleton) {
            // FIX: Use the interface
            ((SkeletonConversionTracker) skeleton).setArrowCount(world.getRandom().nextBetween(4, 7));
        }


        MobEntity entity = (MobEntity) (Object) this;

        // 1. Filter: Only normal Skeletons
        // 2. Filter: Skip if spawned via Spawn Egg or Command
        if (entity instanceof SkeletonEntity && entity.getType() == EntityType.SKELETON &&
                spawnReason != SpawnReason.SPAWN_ITEM_USE && spawnReason != SpawnReason.COMMAND) {

            // 3. Depth check
            if (world instanceof ServerWorld serverWorld && entity.getY() <= -20) {

                // 4. Air Gap Check (1x3x1)
                // Wither Skeletons are tall. We check the space 2 and 3 blocks above the feet.
                BlockPos pos = entity.getBlockPos();
                boolean hasSpace = world.getBlockState(pos.up(2)).getCollisionShape(world, pos.up(2)).isEmpty() &&
                        world.getBlockState(pos.up(3)).getCollisionShape(world, pos.up(3)).isEmpty();

                // 5. 5% Chance (Corrected your 1.05f to 0.05f!)
                if (hasSpace && entity.getRandom().nextFloat() < 0.05f) {

                    EntityConversionContext context = EntityConversionContext.create(entity, true, true);

                    entity.convertTo(EntityType.WITHER_SKELETON, context, (witherSkeleton) -> {
                        witherSkeleton.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                    });
                }
            }
        }

    }
}