package net.colorixer.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class WorldCreationNukeMixin extends Screen {

    @Shadow @Final net.minecraft.client.gui.screen.world.WorldCreator worldCreator;
    @Shadow @Final private TabManager tabManager;

    @Unique
    private static final Identifier BUTTON_DISABLED_TEXTURE = Identifier.ofVanilla("widget/button_disabled");

    protected WorldCreationNukeMixin(Text title) { super(title); }

    @Inject(method = "render", at = @At("TAIL"))
    private void ttll$handleDynamicButtons(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        WorldCreator.Mode currentMode = worldCreator.getGameMode();
        boolean isSurvival = currentMode == WorldCreator.Mode.SURVIVAL;
        boolean isHardcore = currentMode == WorldCreator.Mode.HARDCORE;
        boolean isCreative = currentMode == WorldCreator.Mode.CREATIVE;
        boolean isLockedMode = isSurvival || isHardcore;

        // --- HANDLER FOR MORE TAB (Game Rules) ---
        String moreTabTitle = Text.translatable("createWorld.tab.more.title").getString();
        if (tabManager.getCurrentTab() != null && tabManager.getCurrentTab().getTitle().getString().equals(moreTabTitle)) {
            if (isSurvival || isCreative) {
                int w = 310;
                int x = this.width / 2 - 155;
                int y = 100;

                context.drawGuiTexture(RenderLayer::getGuiTextured, BUTTON_DISABLED_TEXTURE, x, y, w, 20);
                Text grText = Text.translatable("createWorld.tab.gamerules.title").formatted(Formatting.GRAY);
                context.drawCenteredTextWithShadow(this.textRenderer, grText, x + (w / 2), y + 6, 0xFFFFFF);
            }
        }

        // --- HANDLER FOR GAME TAB (Difficulty, Cheats, Mode Labels) ---
        if (tabManager.getCurrentTab() != null && tabManager.getCurrentTab().getTitle().getString().equals(Text.translatable("createWorld.tab.game.title").getString())) {
            for (Element element : this.children()) {
                if (element instanceof CyclingButtonWidget<?> button) {
                    boolean isGameMode = button.getMessage().getString().contains(Text.translatable("selectWorld.gameMode").getString());
                    boolean isDifficulty = button.getValue() instanceof Difficulty;
                    boolean isCheats = button.getMessage().getString().contains(Text.translatable("selectWorld.allowCommands").getString());

                    if (isGameMode) {
                        if (isSurvival) button.setMessage(Text.translatable("selectWorld.gameMode").append(": ").append(Text.literal("Mediumcore").formatted(Formatting.YELLOW)));
                        if (isHardcore) button.setMessage(Text.translatable("selectWorld.gameMode").append(": ").append(Text.literal("Hardcore").formatted(Formatting.DARK_RED)));
                    }

                    if (isDifficulty || isCheats) {
                        if (isLockedMode) {
                            if (isDifficulty) worldCreator.setDifficulty(isHardcore ? Difficulty.HARD : Difficulty.NORMAL);
                            if (isCheats) worldCreator.setCheatsEnabled(false);

                            button.active = false;
                            button.visible = false;

                            String valKey = isDifficulty ? (isHardcore ? "options.difficulty.hard" : "options.difficulty.normal") : "options.off";
                            ttll$drawLockedColored(context, button.getX(), button.getY(),
                                    isDifficulty ? "options.difficulty" : "selectWorld.allowCommands",
                                    valKey, Formatting.GRAY);

                            // Tooltip Check
                            if (mouseX >= button.getX() && mouseX <= button.getX() + button.getWidth() &&
                                    mouseY >= button.getY() && mouseY <= button.getY() + button.getHeight()) {
                                String infoKey = isCheats ? "selectWorld.allowCommands.info" : (isHardcore ? "selectWorld.gameMode.hardcore.info" : "selectWorld.gameMode.survival.info");
                                context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines(Text.translatable(infoKey), 200), mouseX, mouseY);
                            }
                        } else {
                            button.active = true;
                            button.visible = true;
                        }
                    }
                }
            }
        }
    }

    @Unique
    private void ttll$drawLockedColored(DrawContext context, int x, int y, String key, String val, Formatting valColor) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, BUTTON_DISABLED_TEXTURE, x, y, 210, 20);
        Text text = Text.translatable(key).formatted(Formatting.GRAY)
                .append(": ")
                .append(Text.translatable(val).formatted(valColor));
        context.drawCenteredTextWithShadow(this.textRenderer, text, x + 105, y + 6, 0xFFFFFF);
    }
}