#version 150

#define MINECRAFT_LIGHT_POWER   (0.6)
#define MINECRAFT_AMBIENT_LIGHT (0.4)

vec4 minecraft_mix_light(vec3 lightDir0, vec3 lightDir1, vec3 normal, vec4 color) {
    lightDir0 = normalize(lightDir0);
    lightDir1 = normalize(lightDir1);
    float light0 = max(0.0, dot(lightDir0, normal));
    float light1 = max(0.0, dot(lightDir1, normal));
    float lightAccum = min(1.0, (light0 + light1) * MINECRAFT_LIGHT_POWER + MINECRAFT_AMBIENT_LIGHT);
    return vec4(color.rgb * lightAccum, color.a);
}

vec4 minecraft_sample_lightmap(sampler2D lightMap, ivec2 uv) {
    float blockLight = uv.x / 16.0;
    float skyLight = uv.y / 16.0;

    // --- REWORKED DARKNESS MULTIPLIER ---
    // We use pow() to create a curved falloff.
    // This makes the transition to your "Pitch Black" logic feel like a natural shadow.
    float smoothFalloff = pow(blockLight / 7.0, 2.2);
    float darknessMultiplier = clamp(smoothFalloff, 0.0, 1.0);

    vec4 darkColor = vec4(darknessMultiplier, darknessMultiplier, darknessMultiplier, 1.0);

    vec4 defaultLightColor = texture(lightMap, clamp(uv / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
    vec4 skyLight15 = texture(lightMap, clamp(vec2(0.0, 240.0) / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
    vec4 light0 = texture(lightMap, clamp(vec2(0.0, 0.0) / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));

    float nightness = smoothstep(0.1, 0.25, skyLight15.r);
    float blockLightFlipped = 16.0 - blockLight;

    // Adjusted nightColor to blend more softly with the surrounding air
    vec4 nightColor = (vec4(0.15, 0.18, 0.25, 1.0) + vec4(nightness, nightness, nightness, 1.0)) * smoothstep(8.0, 16.0, blockLightFlipped);

    nightColor = nightColor.r + darkColor.r > 1.0 ? vec4(1.0, 1.0, 1.0, 1.0) - vec4(darkColor.rgb, 0.0) : nightColor;
    nightness = 1.0 - nightness;

    if (blockLight <= 7.0 && light0.r > 0.25) blockLight = 8.0;

    vec4 newLightColor = defaultLightColor;

    // --- YOUR PROTECTED LOGIC (DO NOT CHANGE) ---
    if (blockLight == 0.0 && skyLight == 0.0) newLightColor = vec4(0.0, 0.0, 0.0, 1.0);
    else if (blockLight == 0.0 && skyLight > 0.0 && nightness == 1.0) newLightColor = vec4(0.0, 0.0, 0.0, 1.0);
    else if (blockLight <= 7.0 && skyLight == 0.0) newLightColor *= darkColor;
    else if (blockLight <= 7.0 && skyLight > 0.0 && nightness > 0.0) newLightColor *= darkColor + nightColor;
    // --- END PROTECTED LOGIC ---

    newLightColor.a = 1.0;

    return newLightColor;
}
