package net.colorixer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void ttll$restrictedFlintAndSteel(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (player != null) {
            // FIX: Check if the block is flammable or if fire can exist on this side
            // We check if the block clicked is burnable.
            // Iron blocks etc. return 0/false here.
            boolean isBurnable = world.getBlockState(pos).isBurnable();

            if (!state.isBurnable() && !state.isOf(net.minecraft.block.Blocks.OBSIDIAN)) {
                cir.setReturnValue(ActionResult.PASS);
                return;
            }

            // Set the 10-tick cooldown
            player.getItemCooldownManager().set(stack, 10);

            // 1/5 Chance to succeed (0, 1, 2, 3, 4)
            if (world.random.nextInt(5) == 0) {
                // Return and let the original method place the fire
                return;
            } else {
                // FAIL: Spark sounds but no fire
                world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 0.5f, 1.5f);

                // Spawn some "fail" particles (smoke)
                if (world.isClient) {
                    world.addParticle(net.minecraft.particle.ParticleTypes.SMOKE,
                            pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 0.0, 0.1, 0.0);
                }

                if (!player.getAbilities().creativeMode) {
                    stack.damage(1, player, EquipmentSlot.MAINHAND);
                }
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }
}