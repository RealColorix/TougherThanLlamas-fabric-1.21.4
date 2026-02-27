package net.colorixer.entity.passive.cow;

import net.colorixer.entity.passive.goals.AnimalDataAccessor;

/**
 * Cow-specific data.
 * Note: Methods like isPanicking, isEnraged, and isBlockScared
 * are inherited automatically from AnimalDataAccessor.
 */
public interface CowHungerAccessor extends AnimalDataAccessor {
    // Hunger System
    int ttll$getHunger();
    void ttll$setHunger(int value);
    void ttll$setEatAnimTicks(int ticks);

    // Lifecycle/Cooldown Tracking
    long ttll$getLastBirthTime();
    void ttll$setLastBirthTime(long time);
    long ttll$getLastMilkTime();
    void ttll$setLastMilkTime(long time);

    // Specific Cow Trigger
    void ttll$triggerPanic();

}