package net.colorixer.mixin;

import net.colorixer.util.DifficultyLockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsDifficultyNukeMixin extends Screen {
    protected OptionsDifficultyNukeMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void ttll$captureAndNuke(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        boolean isSurvival = client.interactionManager.getCurrentGameMode() == GameMode.SURVIVAL;
        boolean cheatsDisabled = !client.player.hasPermissionLevel(2);
        boolean isHardcore = client.world.getLevelProperties().isHardcore();

        DifficultyLockState.shouldDraw = isSurvival && cheatsDisabled && !isHardcore;

        if (DifficultyLockState.shouldDraw) {
            for (Object child : this.children()) {
                if (child instanceof CyclingButtonWidget<?> b && b.getValue() instanceof Difficulty) {
                    DifficultyLockState.x = b.getX();
                    DifficultyLockState.y = b.getY();
                    DifficultyLockState.w = 150; // Standard width to match FOV exactly
                    break;
                }
            }

            removeButtonIf(element ->
                    (element instanceof CyclingButtonWidget<?> b && b.getValue() instanceof Difficulty) ||
                            (element instanceof ClickableWidget w && w.getWidth() < 50)
            );
        }
    }

    @Unique
    private void removeButtonIf(java.util.function.Predicate<Object> predicate) {
        this.children().removeIf(predicate);
        ScreenAccessor acc = (ScreenAccessor) this;
        acc.getDrawables().removeIf(d -> d instanceof ClickableWidget && predicate.test(d));
        acc.getSelectables().removeIf(s -> s instanceof ClickableWidget && predicate.test(s));
    }
}