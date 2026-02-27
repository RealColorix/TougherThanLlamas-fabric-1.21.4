package net.colorixer.mixin;

import net.colorixer.effect.ModEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    // Target "renderVignetteOverlay" to match your base code Line 193
    // Descriptor must match: (Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/Entity;)V
    @Inject(
            method = "renderVignetteOverlay",
            at = @At("HEAD"),
            cancellable = true
    )
    private void removeVignette(DrawContext context, Entity entity, CallbackInfo ci) {
        // Cancel the method immediately
        ci.cancel();
    }

    @Shadow
    @Final
    private MinecraftClient client;

    @Redirect(
            method = "renderCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"
            )
    )
    private Object hideSwordSpecifically(SimpleOption<AttackIndicator> instance) {
        // First, check if this call is actually for the Attack Indicator
        // (Since SimpleOption.getValue() is used for many things, we check the client options)
        if (instance == this.client.options.getAttackIndicator()) {

            if (this.client.player != null) {
                HitResult hit = this.client.crosshairTarget;

                if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
                    Entity target = ((EntityHitResult) hit).getEntity();

                    if (target instanceof ShulkerEntity) {
                        ItemStack handStack = this.client.player.getMainHandStack();

                        // If holding a Hoe, return 'OFF' to skip sword rendering
                        if (handStack.getItem() instanceof HoeItem) {
                            return AttackIndicator.OFF;
                        }
                    }
                }
            }
        }

        // Otherwise, return the actual setting
        return instance.getValue();
    }

    @Unique private static final Identifier BLEEDING_FULL = Identifier.of("ttll", "hud/heart/bleeding_full");
    @Unique private static final Identifier BLEEDING_HALF = Identifier.of("ttll", "hud/heart/bleeding_half");
    @Unique private static final Identifier BLEEDING_FULL_BLINKING = Identifier.of("ttll", "hud/heart/bleeding_full_blinking");
    @Unique private static final Identifier BLEEDING_HALF_BLINKING = Identifier.of("ttll", "hud/heart/bleeding_half_blinking");

    @Unique private static final Identifier BLEEDING_HARDCORE_FULL = Identifier.of("ttll", "hud/heart/bleeding_hardcore_full");
    @Unique private static final Identifier BLEEDING_HARDCORE_HALF = Identifier.of("ttll", "hud/heart/bleeding_hardcore_half");
    @Unique private static final Identifier BLEEDING_HARDCORE_FULL_BLINKING = Identifier.of("ttll", "hud/heart/bleeding_hardcore_full_blinking");
    @Unique private static final Identifier BLEEDING_HARDCORE_HALF_BLINKING = Identifier.of("ttll", "hud/heart/bleeding_hardcore_half_blinking");

    @Inject(method = "drawHeart", at = @At("HEAD"), cancellable = true)
    private void ttll$drawBleedingHearts(
            DrawContext context,
            @Coerce Object type,
            int x, int y, boolean hardcore, boolean blinking, boolean half,
            CallbackInfo ci
    ) {
        // Because "type" is now an Object, we check if it's the "NORMAL" heart by its string name
        if (type.toString().equals("NORMAL")) {
            PlayerEntity player = net.minecraft.client.MinecraftClient.getInstance().player;

            if (player != null && player.hasStatusEffect(ModEffects.BLEEDING)) {
                Identifier texture;

                if (hardcore) {
                    if (half) {
                        texture = blinking ? BLEEDING_HARDCORE_HALF_BLINKING : BLEEDING_HARDCORE_HALF;
                    } else {
                        texture = blinking ? BLEEDING_HARDCORE_FULL_BLINKING : BLEEDING_HARDCORE_FULL;
                    }
                } else {
                    if (half) {
                        texture = blinking ? BLEEDING_HALF_BLINKING : BLEEDING_HALF;
                    } else {
                        texture = blinking ? BLEEDING_FULL_BLINKING : BLEEDING_FULL;
                    }
                }

                // FIX 2: Pass 'RenderLayer::getGuiTextured' so the DrawContext knows how to draw it
                context.drawGuiTexture(RenderLayer::getGuiTextured, texture, x, y, 9, 9);
                ci.cancel();
            }
        }
    }
}