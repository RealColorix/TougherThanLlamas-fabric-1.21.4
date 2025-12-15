package net.colorixer.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Players start each life with:
 *   • modified fall behavior
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @ModifyReturnValue(
            method = "createPlayerAttributes",
            at = @At("RETURN")
    )
    private static DefaultAttributeContainer.Builder hardmod$defaults(
            DefaultAttributeContainer.Builder original) {

        return original
                .add(EntityAttributes.SAFE_FALL_DISTANCE, 3.0D)
                .add(EntityAttributes.FALL_DAMAGE_MULTIPLIER, 1.5D);
    }
}
