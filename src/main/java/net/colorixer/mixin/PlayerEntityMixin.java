package net.colorixer.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Players start each life with:
 *   • 5 hearts   (max‑health 10.0)
 *   • 2‑block reach (instead of vanilla 4.5)
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @ModifyReturnValue(method = "createPlayerAttributes", at = @At("RETURN"))
    private static DefaultAttributeContainer.Builder hardmod$defaults(
            DefaultAttributeContainer.Builder original) {

        return original
                .add(EntityAttributes.MAX_HEALTH,10.0D);
    }
}
