package net.colorixer.util;

import net.minecraft.util.Identifier;

public class IdentifierUtil {

    /**
     * Creates an Identifier using the built-in static method Identifier.of(...).
     * If your environment has 'private' for that constructor, this is a workaround.
     */
    public static Identifier createIdentifier(String namespace, String path) {
        return Identifier.of(namespace, path);
    }

    /**
     * Creates an Identifier from a string like "namespace:path".
     * If there's no ':' in the string, defaults to "minecraft:" as the namespace.
     */
    public static Identifier createIdentifierFromString(String fullId) {
        if (fullId.contains(":")) {
            String[] split = fullId.split(":", 2);
            return createIdentifier(split[0], split[1]);
        } else {
            // Default to "minecraft" if no explicit namespace was specified
            return createIdentifier("minecraft", fullId);
        }
    }
}
