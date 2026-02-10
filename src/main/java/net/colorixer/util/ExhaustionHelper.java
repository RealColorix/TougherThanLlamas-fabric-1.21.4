package net.colorixer.util;

public class ExhaustionHelper {
    private static int jitterTimer = 0;

    public static void triggerJitter(int ticks) {
        jitterTimer = ticks;
    }

    public static void tick() {
        if (jitterTimer > 0) {
            jitterTimer--;
        }
    }

    public static int getJitterTimer() {
        return jitterTimer;
    }
}