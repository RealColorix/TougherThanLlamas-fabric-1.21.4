package net.colorixer.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class WaterFogMixin {

    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true)
    private static void ttll$makeWaterFogDarkInCaves(Camera camera, float tickDelta, ClientWorld world, int clampedViewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir) {
        if (camera.getSubmersionType() == CameraSubmersionType.WATER) {
            Vector4f originalColor = cir.getReturnValue();

            // 1. Get the lightmap coordinates for the camera's EXACT position
            // This method uses the internal lightmap which is already smoothed/interpolated
            Vec3d camPos = camera.getPos();

            int x0 = MathHelper.floor(camPos.x);
            int y0 = MathHelper.floor(camPos.y);
            int z0 = MathHelper.floor(camPos.z);

            int x1 = x0 + 1;
            int y1 = y0 + 1;
            int z1 = z0 + 1;

            float fx = (float)(camPos.x - x0);
            float fy = (float)(camPos.y - y0);
            float fz = (float)(camPos.z - z0);

            float l000 = getLight(world, x0, y0, z0, skyDarkness);
            float l100 = getLight(world, x1, y0, z0, skyDarkness);
            float l010 = getLight(world, x0, y1, z0, skyDarkness);
            float l110 = getLight(world, x1, y1, z0, skyDarkness);
            float l001 = getLight(world, x0, y0, z1, skyDarkness);
            float l101 = getLight(world, x1, y0, z1, skyDarkness);
            float l011 = getLight(world, x0, y1, z1, skyDarkness);
            float l111 = getLight(world, x1, y1, z1, skyDarkness);

            float l00 = MathHelper.lerp(fx, l000, l100);
            float l10 = MathHelper.lerp(fx, l010, l110);
            float l01 = MathHelper.lerp(fx, l001, l101);
            float l11 = MathHelper.lerp(fx, l011, l111);

            float l0 = MathHelper.lerp(fy, l00, l10);
            float l1 = MathHelper.lerp(fy, l01, l11);

            float smoothLight = MathHelper.lerp(fz, l0, l1);



            // Normalize light into 0–1 range
            float x = MathHelper.clamp((smoothLight - 0.05f) / 0.95f, 0.0f, 1.0f);

// Exponential curve
// Higher strength = more aggressive darkness curve
            float strength = 6.0f;
            float multiplier = 1.0f - (float)Math.exp(-x * strength);
            cir.setReturnValue(new Vector4f(
                    originalColor.x * multiplier,
                    originalColor.y * multiplier,
                    originalColor.z * multiplier,
                    originalColor.w
            ));
        }
    }
    @Inject(method = "applyFog", at = @At("RETURN"), cancellable = true)
    private static void ttll$makeWaterLayeredEffect(
            Camera camera,
            BackgroundRenderer.FogType fogType,
            Vector4f color, // This color variable now holds the Dynamic Black/Blue color
            float viewDistance,
            boolean thickenFog,
            float tickDelta,
            CallbackInfoReturnable<Fog> cir
    ) {
        if (camera.getSubmersionType() == CameraSubmersionType.WATER) {


            float start = 0.0f;
            float end = 16.0f;

            cir.setReturnValue(new Fog(
                    start,
                    end,
                    FogShape.SPHERE, // Sphere feels more immersive than Cylinder underwater
                    color.x(), // Use the Red from getFogColor (Dynamic!)
                    color.y(), // Use the Green from getFogColor (Dynamic!)
                    color.z(), // Use the Blue from getFogColor (Dynamic!)
                    color.w()  // Alpha
            ));
        }
    }
    @Unique
    private static float getLight(ClientWorld world, int x, int y, int z, float skyDarkness) {

        int minY = world.getDimension().minY();
        int maxY = minY + world.getDimension().height() - 1;

        y = MathHelper.clamp(y, minY, maxY);

        int coords = WorldRenderer.getLightmapCoordinates(world, new BlockPos(x, y, z));

        float block = (coords & 0xFFFF) / 240.0f;
        float sky = ((coords >> 16) & 0xFFFF) / 240.0f;

        sky *= (1.0f - skyDarkness);

        return Math.max(block, sky);
    }
}