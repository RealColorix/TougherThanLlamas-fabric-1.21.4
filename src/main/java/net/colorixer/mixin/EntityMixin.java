package net.colorixer.mixin;

import net.colorixer.access.SpiderCobwebAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements SpiderCobwebAccessor {

    /* ---------------- INTERNAL STATE ---------------- */

    @Unique
    private boolean ttll$hasShotCobweb = false;

    /* ---------------- ACCESS ---------------- */

    @Override
    public boolean ttll$hasShotCobweb() {
        return this.ttll$hasShotCobweb;
    }

    @Override
    public void ttll$setHasShotCobweb(boolean value) {
        this.ttll$hasShotCobweb = value;
    }

    /* ---------------- SAVE ---------------- */

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void ttll$writeSpiderCobwebFlag(
            NbtCompound nbt,
            CallbackInfoReturnable<NbtCompound> cir
    ) {
        nbt.putBoolean("HasShotCobweb", this.ttll$hasShotCobweb);
    }

    /* ---------------- LOAD ---------------- */

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void ttll$readSpiderCobwebFlag(
            NbtCompound nbt,
            CallbackInfo ci
    ) {
        if (nbt.contains("HasShotCobweb")) {
            this.ttll$hasShotCobweb = nbt.getBoolean("HasShotCobweb");
        }
    }
}