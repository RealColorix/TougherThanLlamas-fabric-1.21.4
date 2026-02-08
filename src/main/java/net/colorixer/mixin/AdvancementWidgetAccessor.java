package net.colorixer.mixin;

import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(AdvancementWidget.class)
public interface AdvancementWidgetAccessor {
    @Accessor("parent")
    AdvancementWidget getParentWidget();
    @Accessor("children")
    List<AdvancementWidget> getChildren();

    @Accessor("x")
    int getX();

    @Accessor("y")
    int getY();

    // ADD THIS LINE BELOW
    @Accessor("progress")
    AdvancementProgress getProgress();
}