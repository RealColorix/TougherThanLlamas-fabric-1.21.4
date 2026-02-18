package net.colorixer.mixin;

import net.colorixer.access.CreeperStateAccessor;
import net.colorixer.entity.creeper.firecreeper.FireCreeperEntity;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreeperEntityRenderer.class)
public class CreeperRendererMixin {

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void syncShearedState(CreeperEntity creeper,
                                  CreeperEntityRenderState state,
                                  float f,
                                  CallbackInfo ci) {

        CreeperStateAccessor accessor = (CreeperStateAccessor) state;

        boolean sheared =
                ((CreeperStateAccessor) creeper).ttll$isSheared();

        accessor.ttll$setSheared(sheared);
        accessor.ttll$setFire(creeper instanceof FireCreeperEntity);
    }

    @Inject(
            method = "getTexture(Lnet/minecraft/client/render/entity/state/CreeperEntityRenderState;)Lnet/minecraft/util/Identifier;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void getShearedTexture(CreeperEntityRenderState state,
                                   CallbackInfoReturnable<Identifier> cir) {

        CreeperStateAccessor accessor = (CreeperStateAccessor) state;

        if (accessor.ttll$isSheared()) {
            String path = accessor.ttll$isFire()
                    ? "fire_creeper_sheared.png"
                    : "creeper_sheared.png";

            cir.setReturnValue(
                    Identifier.of("ttll", "textures/entity/creeper/" + path)
            );
        }
    }
}
