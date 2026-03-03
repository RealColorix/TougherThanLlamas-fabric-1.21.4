package net.colorixer.util;

public class TtllLightingState {
    // volatile ensures Sodium's worker threads always see the updated value instantly
    public static volatile float currentMoonMultiplier = 1.0f;
}