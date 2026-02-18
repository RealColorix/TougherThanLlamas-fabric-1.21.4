package net.colorixer.mixin;

import net.colorixer.access.CreeperStateAccessor;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CreeperEntityRenderState.class)
public class CreeperStateMixin implements CreeperStateAccessor {
    private boolean sheared;
    private boolean fire;

    @Override public boolean ttll$isSheared() { return sheared; }
    @Override public void ttll$setSheared(boolean sheared) { this.sheared = sheared; }
    @Override public boolean ttll$isFire() { return fire; }
    @Override public void ttll$setFire(boolean fire) { this.fire = fire; }
}