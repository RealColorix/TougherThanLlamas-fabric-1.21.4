package net.colorixer.mixin;

import net.minecraft.block.AbstractBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractBlock.class)
public interface BlockAccessor {

    @Accessor("settings")
    void ttll$setSettings(AbstractBlock.Settings settings);
}
