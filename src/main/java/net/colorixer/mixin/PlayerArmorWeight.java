package net.colorixer.mixin;

import net.colorixer.access.PlayerArmorWeightAccessor;
import net.colorixer.player.ArmorWeight;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerArmorWeight implements PlayerArmorWeightAccessor {

    @Unique
    private int ttll$armorWeight;

    @Inject(method = "tick", at = @At("HEAD"))
    private void ttll$updateArmorWeight(CallbackInfo ci) {
        int total = 0;
        for (ItemStack stack : ((PlayerEntity) (Object) this).getArmorItems()) {
            total += ArmorWeight.get(stack.getItem());
        }
        this.ttll$armorWeight = total;
    }

    @Override
    public int ttll$getArmorWeight() {
        return this.ttll$armorWeight;
    }
}
