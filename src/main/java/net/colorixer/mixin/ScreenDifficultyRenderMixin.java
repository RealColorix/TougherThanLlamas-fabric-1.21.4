package net.colorixer.mixin;

import net.colorixer.util.DifficultyLockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenDifficultyRenderMixin {
    @Unique private static final Identifier TEX = Identifier.ofVanilla("widget/button_disabled");

    @Inject(method = "render", at = @At("TAIL"))
    private void ttll$renderHeaderAndFakeButton(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Ensure we are only doing this for OptionsScreen
        if (!((Object)this instanceof OptionsScreen screen)) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // 1. Restore the "Options" Title - Moved from 15 to 11 (4px up)
        context.drawCenteredTextWithShadow(client.textRenderer, screen.getTitle(), screen.width / 2, 12, 0xFFFFFF);

        // 2. Draw the fake Difficulty button if Mediumcore is active
        if (DifficultyLockState.shouldDraw) {
            int x = DifficultyLockState.x;
            int y = DifficultyLockState.y; // Moved 4px up
            int w = DifficultyLockState.w;

            // Draw the texture
            context.drawGuiTexture(RenderLayer::getGuiTextured, TEX, x, y, w, 20);

            // Draw the "Difficulty: Normal" text in gray
            Text text = Text.translatable("options.difficulty").formatted(Formatting.GRAY)
                    .append(": ")
                    .append(Text.translatable("options.difficulty.normal").formatted(Formatting.GRAY));

            // Text y position adjusted relative to button y
            context.drawCenteredTextWithShadow(client.textRenderer, text, x + (w / 2), y + 6, 0xFFFFFF);
        }
    }
}