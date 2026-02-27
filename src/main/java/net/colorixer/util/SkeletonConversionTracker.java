package net.colorixer.util;

public interface SkeletonConversionTracker {
    void setConvertingToSkeleton(boolean converting);
    boolean isConvertingToSkeleton();

    // Add these so we don't have to reference the Mixin class directly
    int getArrowCount();
    void setArrowCount(int count);
}