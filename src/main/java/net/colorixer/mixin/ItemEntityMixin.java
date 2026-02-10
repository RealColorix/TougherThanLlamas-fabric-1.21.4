package net.colorixer.mixin;

import net.colorixer.block.torch.BurningCrudeTorchItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void ttll$extinguishTorchInWater(CallbackInfo ci) {
        ItemEntity entity = (ItemEntity) (Object) this;
        World world = entity.getWorld();

        if (!world.isClient && entity.getStack().getItem() instanceof BurningCrudeTorchItem) {
            // Check if the item entity itself is in water
            if (entity.isSubmergedInWater() || entity.getFluidHeight(net.minecraft.registry.tag.FluidTags.WATER) > 0.1) {
                world.playSound(null, entity.getBlockPos(),
                        SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                entity.discard(); // Deletes the dropped item
            }
        }
    }
}