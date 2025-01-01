package net.colorixer.block;

import net.colorixer.util.IdentifierUtil;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class BlockTags {

    public static final TagKey<Block> DRAX_MINEABLE = of("mineable/drax");

    private BlockTags() {}

    private static TagKey<Block> of(String path) {

        Identifier id = IdentifierUtil.createIdentifier("ttll", path);
        return TagKey.of(RegistryKeys.BLOCK, id);
    }
}
