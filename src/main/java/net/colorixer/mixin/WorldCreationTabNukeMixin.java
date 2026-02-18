package net.colorixer.mixin;

import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.gui.screen.world.CreateWorldScreen$GameTab")
public class WorldCreationTabNukeMixin {

    @Unique
    private static final String ALLOW_COMMANDS = "selectWorld.allowCommands";

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/CyclingButtonWidget$Builder;build(IIIILnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/CyclingButtonWidget$UpdateCallback;)Lnet/minecraft/client/gui/widget/CyclingButtonWidget;"
            )
    )
    private CyclingButtonWidget ttll$nukeButKeepLayout(CyclingButtonWidget.Builder builder, int x, int y, int w, int h, Text text, CyclingButtonWidget.UpdateCallback callback, CreateWorldScreen screen) {
        var creator = screen.getWorldCreator();
        boolean isSurvival = creator.getGameMode() == net.minecraft.client.gui.screen.world.WorldCreator.Mode.SURVIVAL;

        // Create the button normally first to check what it is
        CyclingButtonWidget button = builder.build(x, y, w, h, text, callback);

        boolean isDifficulty = button.getValue() instanceof Difficulty;
        boolean isCheats = button.getMessage().getString().contains(Text.translatable(ALLOW_COMMANDS).getString());

        if (isSurvival && (isDifficulty || isCheats)) {
            // Lock logic
            if (isDifficulty) creator.setDifficulty(Difficulty.NORMAL);
            if (isCheats) creator.setCheatsEnabled(false);

            // NUKE: Make the real button active=false and alpha=0
            // This keeps the grid slot occupied but makes the button "ghosted"
            button.active = false;
            button.visible = false; // We will draw the fake one exactly over this spot
        }

        return button;
    }
}