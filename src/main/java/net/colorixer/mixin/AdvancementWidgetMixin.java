package net.colorixer.mixin;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Mixin(AdvancementWidget.class)
public abstract class AdvancementWidgetMixin {

    @Shadow @Final private PlacedAdvancement advancement;
    @Shadow @Nullable private AdvancementProgress progress;
    @Shadow @Final private List<OrderedText> title;
    @Shadow @Final private MinecraftClient client;
    @Shadow @Nullable private AdvancementWidget parent;

    @Unique
    private boolean isHardGated() {
        try {
            // Get the ID of the current advancement being rendered
            String currentId = this.advancement.getAdvancementEntry().id().toString();

            // HARD-CODED CHECK: If this is Leather Armor, check Kill a Monster
            if (currentId.equals("ttll:story/leather_armor")) {
                var handler = this.client.getNetworkHandler();
                if (handler != null) {
                    var manager = handler.getAdvancementHandler();
                    var progressMap = ((ClientAdvancementManagerAccessor) manager).getProgressMap();

                    for (var entry : progressMap.entrySet()) {
                        if (entry.getKey().id().toString().equals("ttll:story/kill_a_monster")) {
                            // Returns TRUE (Red) if kill_a_monster is NOT done
                            return !entry.getValue().isDone();
                        }
                    }
                    // If the requirement isn't in the map at all, it's not done -> Red
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Unique
    private int getObfuscationTier() {
        if (this.progress != null && this.progress.isDone()) return 0;

        // 1. PATH DISCOVERY (White)
        if (isAnyDescendantStarted(this.advancement)) return 0;

        // 2. HARD GATE (Red)
        if (isHardGated()) return 4;

        int dist = getDynamicDistance();

        // 3. MYSTERY (Locked/Dark)
        if (dist >= 4) return 2;

        // 4. PROGRESS (Green)
        if (this.progress != null && this.progress.isAnyObtained()) return 3;

        // 5. NEARBY (Gray)
        if (dist == 3) return 1;

        // 6. AVAILABLE (White)
        return 0;
    }

    @Redirect(
            method = {"renderWidgets", "drawTooltip"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementObtainedStatus;getFrameTexture(Lnet/minecraft/advancement/AdvancementFrame;)Lnet/minecraft/util/Identifier;")
    )
    private Identifier getCustomFrameSprite(AdvancementObtainedStatus status, AdvancementFrame frame) {
        int tier = getObfuscationTier();
        if (status == AdvancementObtainedStatus.OBTAINED) return status.getFrameTexture(frame);

        String type = frame.asString();
        String suffix = switch (tier) {
            case 1 -> "_gray";
            case 2 -> "_locked";
            case 3 -> "_green";
            case 4 -> "_red";
            default -> "";
        };

        if (suffix.isEmpty()) return status.getFrameTexture(frame);
        return Identifier.of("ttll", "advancements/" + type + "_frame" + suffix);
    }

    @Inject(method = "drawText", at = @At("HEAD"), cancellable = true)
    private void swapTextToQuestionMarks(DrawContext context, List<OrderedText> text, int x, int y, int color, CallbackInfo ci) {
        int tier = getObfuscationTier();
        if (text != null && text.equals(this.title)) {
            if (tier == 4) {
                renderCustomText(context, "Locked", x, y, color, ci);
            } else if (tier == 2) {
                renderCustomText(context, "???", x, y, color, ci);
            }
        }
    }

    @Unique
    private void renderCustomText(DrawContext context, String literal, int x, int y, int color, CallbackInfo ci) {
        List<OrderedText> wrapped = this.client.textRenderer.wrapLines(Text.literal(literal), 163);
        for (int i = 0; i < wrapped.size(); i++) {
            context.drawTextWithShadow(this.client.textRenderer, wrapped.get(i), x, y + i * 9, color);
        }
        ci.cancel();
    }

    @Unique
    private int getDynamicDistance() {
        if (this.progress != null && this.progress.isDone()) return 0;
        int distance = 0;
        AdvancementWidgetMixin current = (AdvancementWidgetMixin) (Object) this;
        while (current != null) {
            if (current.progress == null || !current.progress.isDone()) distance++;
            else break;
            current = (AdvancementWidgetMixin) (Object) current.parent;
        }
        return distance;
    }

    @Unique
    private boolean isAnyDescendantStarted(PlacedAdvancement root) {
        var handler = this.client.getNetworkHandler();
        if (handler == null) return false;
        var progressMap = ((ClientAdvancementManagerAccessor) handler.getAdvancementHandler()).getProgressMap();
        for (PlacedAdvancement child : root.getChildren()) {
            AdvancementProgress childProgress = progressMap.get(child.getAdvancementEntry());
            if (childProgress != null && childProgress.isAnyObtained()) return true;
            if (isAnyDescendantStarted(child)) return true;
        }
        return false;
    }

    @Redirect(
            method = "drawTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementObtainedStatus;getBoxTexture()Lnet/minecraft/util/Identifier;")
    )
    private Identifier getDynamicBoxTexture(AdvancementObtainedStatus status) {
        int tier = getObfuscationTier();

        // 1. Progressing (Green Fill / Blue Background)
        if (tier == 3) {
            if (status == AdvancementObtainedStatus.OBTAINED) {
                return Identifier.of("ttll", "advancements/box_obtaining");
            }
            return status.getBoxTexture(); // Standard Blue
        }

        // 2. Map Tiers to specific boxes
        return switch (tier) {
            case 4 -> Identifier.of("ttll", "advancements/box_red");
            case 2 -> Identifier.of("ttll", "advancements/box_locked");
            case 1 -> Identifier.of("ttll", "advancements/box_dark_blue");
            // Case 0 (White/Available) is now back to Standard Blue
            default -> status.getBoxTexture();
        };
    }
}