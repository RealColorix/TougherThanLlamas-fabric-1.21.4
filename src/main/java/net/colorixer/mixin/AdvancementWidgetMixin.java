package net.colorixer.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(AdvancementWidget.class)
public abstract class AdvancementWidgetMixin {

    @Shadow @Final private PlacedAdvancement advancement;
    @Shadow @Nullable private AdvancementProgress progress;
    @Shadow @Final private List<OrderedText> title;
    @Shadow @Final private List<OrderedText> description;
    @Shadow @Final private MinecraftClient client;
    @Shadow @Nullable private AdvancementWidget parent;
    @Shadow @Final private List<AdvancementWidget> children;
    @Shadow @Final private int x;
    @Shadow @Final private int y;
    @Shadow @Final private AdvancementDisplay display;
    private static final Set<String> Z_SHAPE_IDS = Set.of(
            "ttll:story/kill_all_monsters",
            "ttll:story/knitt_wool",
            "ttll:story/eat_food",
            "ttll:story/drying_rack",
            "ttll:story/acquire_leather",
            "ttll:story:vicinity_gravel",
            "ttll:story:vicinity_stone",
            "ttll:story/leather_armor",
            "ttll:story/acquire_flint",
            "ttll:story/furnace",
            "ttll:story/campfire_cooking",
            "ttll:story/smelt_iron",
            "ttll:story/iron_nugget"
    );

    @Unique
    private static final Map<String, String> GATES = Map.of(
            "ttll:story/leather_armor", "ttll:story/vicinity_crafting_table",
            "ttll:story/knitt_wool", "ttll:story/acquire_pointy_stick",
            "ttll:story/acquire_sinew", "ttll:story/helper_kill_cow_or_horse",
            "ttll:story/iron_nugget", "ttll:story/furnace",
            "ttll:story/vicinity_crafting_table", "ttll:story/iron_chisel"
    );

    /**
     * @author YourName
     * @reason L-shaped and Z-shaped connectors with progression-based coloring
     */
    @Unique
    private boolean isPathToRootDone(AdvancementWidget widget) {
        if (widget == null) return true;
        AdvancementProgress p = ((AdvancementWidgetAccessor)widget).getProgress();
        if (p == null || !p.isDone()) return false;
        return isPathToRootDone(((AdvancementWidgetAccessor)widget).getParentWidget());
    }

    /**
     * @author YourName
     * @reason Multi-pass rendering for the entire tree to ensure Yellow stacks on top.
     */
    @Overwrite
    public void renderLines(DrawContext context, int x, int y, boolean border) {
        for (int priority = 1; priority <= 5; priority++) {
            renderFullTreeLayer(context, x, y, border, priority);
        }
    }

    @Unique
    private void renderFullTreeLayer(DrawContext context, int x, int y, boolean border, int currentPriority) {
        // 1. Render this specific widget's layer if it matches the current priority
        renderPriorityLayer(context, x, y, border, currentPriority);

        // 2. Recursively force all children to draw ONLY their version of this priority layer
        for (AdvancementWidget child : this.children) {
            ((AdvancementWidgetMixin)(Object)child).renderFullTreeLayer(context, x, y, border, currentPriority);
        }
    }

    @Unique
    private void renderPriorityLayer(DrawContext context, int x, int y, boolean border, int currentPriority) {
        if (this.parent != null) {
            int tier = this.getObfuscationTier();
            boolean childDone = this.progress != null && this.progress.isDone();
            boolean pathUntilParentDone = isPathToRootDone(this.parent);

            // Check if this specific node is a "Blue" challenge (started but not finished)
            boolean isBlueChallenge = !childDone &&
                    this.display.getFrame() == net.minecraft.advancement.AdvancementFrame.CHALLENGE &&
                    this.progress != null && this.progress.isAnyObtained();

            // LOOK AHEAD logic remains the same
            boolean leadsToVisibleNode = false;
            for (AdvancementWidget child : this.children) {
                AdvancementWidgetMixin childMixin = (AdvancementWidgetMixin)(Object)child;
                int childTier = childMixin.getObfuscationTier();
                if (childTier == 7 || childTier == 3 || childTier == 0) {
                    leadsToVisibleNode = true;
                    break;
                }
                if (isAnyDescendantStarted(childMixin.advancement)) {
                    leadsToVisibleNode = true;
                    break;
                }
            }

            int linePriority;
            int color;

            if (childDone && pathUntilParentDone) {
                linePriority = 5; // YELLOW
                color = 0xFFB98F2C;
            }
            // FIX: Only allow Green/Blue if the path is done AND the current node isn't a hard gate
            else if (pathUntilParentDone && tier != 5 && tier != 6) {
                if (isBlueChallenge) {
                    linePriority = 4; // BLUE
                    color = 0xFF036A96;
                } else {
                    linePriority = 4; // GREEN
                    color = 0xFF51A64F;
                }
            }
            // Red check now correctly captures Tiers 5 and 6
            else if (tier == 5 || tier == 6) {
                linePriority = 1; // RED
                color = 0xFF6B2828;
            }
            else if (isNearProgress() || leadsToVisibleNode) {
                linePriority = 3; // WHITE
                color = 0xFFFFFFFF;
            }
            else {
                linePriority = 2; // GRAY
                color = 0xFF8A8A8A;
            }

            if (linePriority == currentPriority) {
                context.getMatrices().push();
                context.getMatrices().translate(0.5f, 0.0f, 0.0f);

                int startX = x + ((AdvancementWidgetAccessor)this.parent).getX() + 14;
                int startY = y + ((AdvancementWidgetAccessor)this.parent).getY() + 13;
                int endX = x + this.x + 14;
                int endY = y + this.y + 13;

                int finalColor = border ? 0xFF000000 : color;

                String id = this.advancement.getAdvancementEntry().id().toString();
                if (Z_SHAPE_IDS.contains(id)) {
                    drawZPath(context, startX, startY, endX, endY, finalColor, border);
                } else {
                    int diffX = Math.abs(endX - startX);
                    int diffY = Math.abs(endY - startY);
                    if (diffX > diffY) {
                        drawLPath(context, startX, startY, endX, endY, true, finalColor, border);
                    } else {
                        drawLPath(context, startX, startY, endX, endY, false, finalColor, border);
                    }
                }
                context.getMatrices().pop();
            }
        }
    }

    @Unique
    private boolean isNearProgress() {
        AdvancementWidgetAccessor parentAcc = (AdvancementWidgetAccessor)this.parent;
        AdvancementWidget grandparent = parentAcc.getParentWidget();
        if (grandparent != null) {
            AdvancementProgress gpProg = ((AdvancementWidgetAccessor)grandparent).getProgress();
            return gpProg != null && gpProg.isDone();
        }
        return false;
    }

    private void drawLPath(DrawContext context, int x1, int y1, int x2, int y2, boolean horizontalFirst, int color, boolean border) {
        if (horizontalFirst) {
            drawRobustLine(context, x1, x2, y1, true, color, border);
            int vStart = Math.min(y1, y2) - 1;
            int vEnd = Math.max(y1, y2) + 1;
            drawRobustLine(context, vStart, vEnd, x2, false, color, border);
        } else {
            int vStart = Math.min(y1, y2) - 1;
            int vEnd = Math.max(y1, y2) + 1;
            drawRobustLine(context, vStart, vEnd, x1, false, color, border);
            drawRobustLine(context, x1, x2, y2, true, color, border);
        }
    }

    private void drawZPath(DrawContext context, int x1, int y1, int x2, int y2, int color, boolean border) {
        int midX = x1 + (x2 - x1) / 2;
        drawRobustLine(context, x1, midX, y1, true, color, border);
        int vStart = Math.min(y1, y2) - 1;
        int vEnd = Math.max(y1, y2) + 1;
        drawRobustLine(context, vStart, vEnd, midX, false, color, border);
        drawRobustLine(context, midX, x2, y2, true, color, border);
    }

    private void drawRobustLine(DrawContext context, int start, int end, int fixed, boolean horizontal, int color, boolean border) {
        int s = Math.min(start, end);
        int e = Math.max(start, end);

        if (horizontal) {
            context.drawHorizontalLine(s, e, fixed, color);
            if (border) {
                context.drawHorizontalLine(s, e, fixed - 1, 0xFF000000);
                context.drawHorizontalLine(s, e, fixed + 1, 0xFF000000);
            }
        } else {
            context.drawVerticalLine(fixed, s, e, color);
            if (border) {
                context.drawVerticalLine(fixed - 1, s, e, 0xFF000000);
                context.drawVerticalLine(fixed + 1, s, e, 0xFF000000);
            }
        }
    }






    @Unique
    private String getRequiredAdvancementName() {
        String currentId = this.advancement.getAdvancementEntry().id().toString();
        String requiredId = GATES.get(currentId);

        if (requiredId != null && this.client.getNetworkHandler() != null) {
            var handler = this.client.getNetworkHandler();
            var manager = handler.getAdvancementHandler();

            // Find the requirement advancement in the tree
            var advEntry = manager.getManager().get(Identifier.of(requiredId));

            if (advEntry != null) {
                // Check distance of the REQUIREMENT, not the current widget
                int reqDist = getStaticDistance(advEntry);

                // If distance is 3 or less (White, Red, or Dark Red zones), show name
                // If it's 4+ (Mystery zone), show ???
                if (reqDist <= 3 && advEntry.getAdvancement().display().isPresent()) {
                    return advEntry.getAdvancement().display().get().getTitle().getString();
                }
            }
        }
        return "???";
    }

    // New helper to get distance for any specific advancement entry
    @Unique
    private int getStaticDistance(PlacedAdvancement target) {
        var handler = this.client.getNetworkHandler();
        if (handler == null) return 99;
        var progressMap = ((ClientAdvancementManagerAccessor) handler.getAdvancementHandler()).getProgressMap();

        int distance = 0;
        PlacedAdvancement current = target;
        while (current != null) {
            AdvancementProgress p = progressMap.get(current.getAdvancementEntry());
            if (p == null || !p.isDone()) distance++;
            else break;
            current = current.getParent();
        }
        return distance;
    }

    @Unique
    private boolean isHardGated() {
        try {
            String currentId = this.advancement.getAdvancementEntry().id().toString();
            if (GATES.containsKey(currentId)) {
                String requiredId = GATES.get(currentId);
                var handler = this.client.getNetworkHandler();
                if (handler != null) {
                    var manager = handler.getAdvancementHandler();
                    var progressMap = ((ClientAdvancementManagerAccessor) manager).getProgressMap();
                    for (var entry : progressMap.entrySet()) {
                        if (entry.getKey().id().toString().equals(requiredId)) {
                            return !entry.getValue().isDone();
                        }
                    }
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
        // 1. If the advancement is actually 100% done, it's Tier 0 (White)
        if (this.progress != null && this.progress.isDone()) return 0;

        // 2. Get the raw tier (checks for Tier 7 green or Tier 3 started)
        int raw = getRawTier();

        // 3. PRIORITY FIX: If it's supposed to be Green (7) or In-Progress (3),
        // keep it that way even if a later advancement is unlocked.
        if (raw == 7 || raw == 3) return raw;

        // 4. Reveal "White" only if it's not Green/Started but a descendant is finished
        if (isAnyDescendantStarted(this.advancement)) return 0;

        // 5. Ancestry Obfuscation (The Stage 2 / ??? logic for gated paths)
        if (this.parent != null) {
            int parentTier = ((AdvancementWidgetMixin)(Object)this.parent).getRawTier();
            if (parentTier == 5 || parentTier == 6) return 2;

            AdvancementWidget grandParent = ((AdvancementWidgetMixin)(Object)this.parent).parent;
            if (grandParent != null) {
                int gpTier = ((AdvancementWidgetMixin)(Object)grandParent).getRawTier();
                if (gpTier == 5 || gpTier == 6) return 2;
            }
        }

        return raw;
    }

    // Helper to calculate the tier of a widget without recursive ancestry checks
    @Unique
    private int getRawTier() {
        // Stage 0: Completed
        if (this.progress != null && this.progress.isDone()) return 0;

        // Handle Gated logic (Red/Dark Red)
        if (isHardGated()) {
            int dist = getDynamicDistance();
            if (dist >= 4) return 2;
            if (dist == 3) return 5;
            return 6;
        }

        // TIER 3: The "In Progress" state (Started 1/37)
        if (this.progress != null && this.progress.isAnyObtained()) {
            return 3;
        }

        // TIER 7: The "Available" state (0/1 or 0/37 but path to root is solid)
        // We check if the parent exists AND if the path from parent to root is finished
        if (this.parent != null && isPathToRootDone(this.parent)) {
            return 7;
        }

        // Distant/Mystery/Gray tiers
        int dist = getDynamicDistance();
        if (dist >= 4) return 2;
        if (dist == 3) return 1;

        return 0;
    }

    @Redirect(
            method = "drawTooltip",
            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z")
    )
    private boolean hideDescriptionFromBox(List<?> list) {
        int tier = getObfuscationTier();
        if (list == this.description && tier == 2) {
            return true;
        }
        return list.isEmpty();
    }

    @Redirect(
            method = "drawTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I")
    )
    private int hideProgressCounter(DrawContext instance, net.minecraft.client.font.TextRenderer textRenderer, Text text, int x, int y, int color) {
        int tier = getObfuscationTier();

        // If the text looks like progress (e.g., contains "/") and we are in Stage 2, 5, or 6, don't draw it
        if ((tier == 2 || tier == 5 || tier == 6) && text.getString().contains("/")) {
            return 0; // Skip drawing
        }

        return instance.drawTextWithShadow(textRenderer, text, x, y, color);
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
            case 3-> "_blue";
            case 5 -> "_dark_red";
            case 6 -> "_red";
            case 7 -> "_green";
            default -> "";
        };

        if (suffix.isEmpty()) return status.getFrameTexture(frame);
        return Identifier.of("ttll", "advancements/" + type + "_frame" + suffix);
    }

    @Inject(
            method = "renderWidgets",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItemWithoutEntity(Lnet/minecraft/item/ItemStack;II)V")
    )
    private void applyTier2Darkness(DrawContext context, int x, int y, CallbackInfo ci) {
        if (getObfuscationTier() == 2) {
            RenderSystem.setShaderColor(0.7f, 0.7f, 0.7f, 1.0f);
        }
    }

    @Inject(
            method = "renderWidgets",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawItemWithoutEntity(Lnet/minecraft/item/ItemStack;II)V", shift = At.Shift.AFTER)
    )
    private void resetTier2Darkness(DrawContext context, int x, int y, CallbackInfo ci) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }


    @Inject(method = "drawText", at = @At("HEAD"), cancellable = true)
    private void swapTextToQuestionMarks(DrawContext context, List<OrderedText> text, int x, int y, int color, CallbackInfo ci) {
        if (this.advancement.getAdvancementEntry().id().toString().equals("ttll:story/knowledge_book")) return;

        int tier = getObfuscationTier();

        if (tier == 6 || tier == 5 || tier == 2) {
            // Handle Title
            if (text != null && text.equals(this.title)) {
                String label = (tier == 2) ? "???" : "Locked";
                renderCustomText(context, label, x, y, color, ci);
                return;
            }

            // Handle Description Routing
            if (text != null && text.equals(this.description)) {
                if (tier == 2) {
                    ci.cancel();
                    return;
                }

                this.advancement.getAdvancement().display().ifPresent(display -> {
                    Text originalDesc = display.getDescription();
                    if (originalDesc.getContent() instanceof net.minecraft.text.TranslatableTextContent translatable) {

                        String finalKey;

                        // TIER 6: Specific "You need [Item]" text
                        if (tier == 6) {
                            finalKey = translatable.getKey().replace(".description", ".need_description");
                        }
                        // TIER 5: Generic "You need ??? first" text
                        else {
                            finalKey = "advancements.story.unknown.need_description";
                        }

                        // Get the requirement name (this method already returns "???" if distance is high)
                        String reqName = getRequiredAdvancementName();

                        // Pull translation from lang file and render
                        String translatedString = Text.translatable(finalKey, reqName).getString();
                        renderCustomText(context, translatedString, x, y, color, ci);
                    }
                });
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
        if (tier == 3) {
            if (status == AdvancementObtainedStatus.OBTAINED) return Identifier.of("minecraft", "advancements/box_obtained");
            return status.getBoxTexture();
        }

        return switch (tier) {
            case 7 -> Identifier.of("minecraft", "advancements/box_unobtained");
            case 6 -> Identifier.of("ttll", "advancements/box_red");
            case 5 -> Identifier.of("ttll", "advancements/box_dark_red");
            case 2 -> Identifier.of("ttll", "advancements/box_locked");
            case 1 -> Identifier.of("ttll", "advancements/box_dark_blue");
            default -> status.getBoxTexture();
        };
    }
}