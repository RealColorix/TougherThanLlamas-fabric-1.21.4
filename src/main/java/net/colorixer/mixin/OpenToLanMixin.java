package net.colorixer.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(OpenToLanScreen.class)
public abstract class OpenToLanMixin extends Screen {

    @Shadow @Final private static Text ALLOW_COMMANDS_TEXT;
    @Shadow @Final private static Text GAME_MODE_TEXT;

    @Unique
    private static final Identifier BUTTON_DISABLED_TEXTURE = Identifier.of("minecraft", "widget/button_disabled");

    protected OpenToLanMixin(Text title) {
        super(title);
    }

    /**
     * 1. THE LOGIC: If cheats are OFF in world properties, we nuke the interactive buttons.
     */
    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/OpenToLanScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"
            )
    )
    private <T extends Element & Drawable & Selectable> T ttll$conditionalNuke(OpenToLanScreen instance, T element) {
        MinecraftClient client = MinecraftClient.getInstance();
        IntegratedServer server = client.getServer();

        // Check if world was created with cheats DISABLED
        if (server != null && !server.getSaveProperties().areCommandsAllowed() && element instanceof CyclingButtonWidget<?> button) {
            String msg = button.getMessage().getString();

            // Nuke Allow Commands AND Game Mode buttons
            if (msg.contains(ALLOW_COMMANDS_TEXT.getString()) || msg.contains(GAME_MODE_TEXT.getString())) {
                return null;
            }
        }
        return super.addDrawableChild(element);
    }

    /**
     * 2. THE VISUALS: Draw the dead buttons only if cheats were disabled at world creation.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void ttll$drawLockedLanButtons(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        IntegratedServer server = client.getServer();

        // Only render fake buttons if the world blocks cheats
        if (server == null || server.getSaveProperties().areCommandsAllowed()) return;

        boolean isHardcore = server.getSaveProperties().isHardcore();

        // FAKE GAME MODE (Left Side)
        // Shows Hardcore if it's a hardcore world, otherwise Mediumcore
        String modeLabel = isHardcore ? "Hardcore" : "Mediumcore";
        ttll$drawFakeButton(context, this.width / 2 - 155, 100, 150,
                GAME_MODE_TEXT.getString() + ": " + modeLabel);

        // FAKE ALLOW COMMANDS (Right Side)
        ttll$drawFakeButton(context, this.width / 2 + 5, 100, 150,
                ALLOW_COMMANDS_TEXT.getString() + ": OFF");
    }

    @Unique
    private void ttll$drawFakeButton(DrawContext context, int x, int y, int w, String label) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, BUTTON_DISABLED_TEXTURE, x, y, w, 20);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(label).formatted(Formatting.GRAY),
                x + w / 2,
                y + 6,
                0xFFA0A0A0
        );
    }
}