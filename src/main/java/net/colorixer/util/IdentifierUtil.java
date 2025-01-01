package net.colorixer.util;

import net.minecraft.util.Identifier;

public class IdentifierUtil {

    public static Identifier createIdentifier(String namespace, String path) {
        return Identifier.of(namespace, path);
    }

}
