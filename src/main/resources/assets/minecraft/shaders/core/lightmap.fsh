#version 150

uniform float AmbientLightFactor;
uniform float SkyFactor;
uniform float BlockFactor;
uniform int UseBrightLightmap;
uniform vec3 SkyLightColor;
uniform float NightVisionFactor;
uniform float DarknessScale;
uniform float DarkenWorldFactor;
uniform float BrightnessFactor;
uniform float MoonMultiplier;

in vec2 texCoord;

out vec4 fragColor;

float get_brightness(float level) {
    float curved_level = level / (4.0 - 3.0 * level);
    return mix(curved_level, 1.0, AmbientLightFactor);
}

vec3 notGamma(vec3 x) {
    vec3 nx = 1.0 - x;
    return 1.0 - nx * nx * nx * nx;
}

void main() {
    float block_brightness = get_brightness(floor(texCoord.x * 16.0) / 15.0) * BlockFactor;
    float sky_brightness = get_brightness(floor(texCoord.y * 16.0) / 15.0) * SkyFactor;

    vec3 color = vec3(
    block_brightness,
    block_brightness * ((block_brightness * 0.6 + 0.4) * 0.6 + 0.4),
    block_brightness * (block_brightness * block_brightness * 0.6 + 0.4)
    );

    if (UseBrightLightmap != 0) {
        color = mix(color, vec3(0.99, 1.12, 1.0), 0.25);
        color = clamp(color, 0.0, 1.0);
    } else {
        color += SkyLightColor * sky_brightness;
        color = mix(color, vec3(0.75), 0.04);

        vec3 darkened_color = color * vec3(0.7, 0.6, 0.6);
        color = mix(color, darkened_color, DarkenWorldFactor);
    }

    if (NightVisionFactor > 0.0) {
        float max_component = max(color.r, max(color.g, color.b));
        if (max_component < 1.0) {
            vec3 bright_color = color / max_component;
            color = mix(color, bright_color, NightVisionFactor);
        }
    }

    if (UseBrightLightmap == 0) {
        color = clamp(color - vec3(DarknessScale), 0.0, 1.0);
    }

    vec3 nGamma = notGamma(color);
    color = mix(color, nGamma, BrightnessFactor);
    color = mix(color, vec3(0.75), 0.04);
    color = clamp(color, 0.0, 1.0);

    // ==========================================
    // TOUGHER THAN LLAMAS - SIMPLE DARKNESS
    // ==========================================
    if (NightVisionFactor <= 0.0 && UseBrightLightmap == 0) {
        float blockLight = floor(texCoord.x * 16.0);
        float skyLight = floor(texCoord.y * 16.0);

        // Simple night detection
        float nightness = 1.0 - smoothstep(0.05, 0.15, SkyFactor);

        // We use the MoonMultiplier directly without extra squaring.
        // This prevents entities from becoming "double-dark".
        float ambientVisibility = mix(1.0, MoonMultiplier, nightness);

        // Torch check: If there is block light, we don't want to crush it
        float torchEffect = clamp(blockLight / 15.0, 0.0, 1.0);

        // Final blend: Moon light affects sky-lit areas, torches protect their own area
        color *= max(torchEffect, ambientVisibility);

        // Hard-enforce cave darkness
        if (blockLight == 0.0 && skyLight == 0.0) {
            color = vec3(0.0);
        }
    }

    fragColor = vec4(color, 1.0);
}