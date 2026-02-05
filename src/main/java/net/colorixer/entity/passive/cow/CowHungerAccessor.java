package net.colorixer.entity.passive.cow;

public interface CowHungerAccessor {
    int ttll$getHunger();
    void ttll$setHunger(int value);
    int ttll$getEatAnimTicks();
    void ttll$setEatAnimTicks(int ticks);
    long ttll$getLastBirthTime();
    void ttll$setLastBirthTime(long time);

    // Keep these for data tracking
    long ttll$getLastMilkTime();
    void ttll$setLastMilkTime(long time);

    // NEW: Boolean flags to prevent math errors on spawn
    boolean ttll$isPanicking();           // Used by FleeAttackerGoal
    void ttll$setPanicking(boolean value);

    boolean ttll$isBlockScared();         // Used by FleeBlockBreakGoal
    void ttll$setBlockScared(boolean value);

    void ttll$triggerPanic();

    void ttll$setEnraged(boolean enraged);
}