package net.colorixer.mixin;

import net.colorixer.access.LeavesFallAccess;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class LeavesEntityFallState implements LeavesFallAccess {

    @Unique
    private float ttll$storedFallDistance = 0.0F;

    @Unique
    private boolean ttll$processedLeaves = false;

    @Override
    public float ttll$getStoredFallDistance() {
        return this.ttll$storedFallDistance;
    }

    @Override
    public void ttll$setStoredFallDistance(float value) {
        this.ttll$storedFallDistance = value;
    }

    @Override
    public boolean ttll$hasProcessedLeaves() {
        return this.ttll$processedLeaves;
    }

    @Override
    public void ttll$setProcessedLeaves(boolean value) {
        this.ttll$processedLeaves = value;
    }
}
