package net.colorixer.mixin;

import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementPositioner;
import net.minecraft.advancement.PlacedAdvancement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

@Mixin(AdvancementPositioner.class)
public abstract class AdvancementPositionerMixin {

    @Shadow private float row;
    @Shadow private int depth;
    @Shadow @Final private PlacedAdvancement advancement;

    // --- 1. Spacing Logic ---
    @ModifyConstant(method = "calculateRecursively", constant = @Constant(floatValue = 1.0F))
    private float increaseSiblingSpacing(float original) {
        return 1.75f; // Sibling gap
    }

    @ModifyConstant(method = "onFinishCalculation", constant = @Constant(floatValue = 1.0F))
    private float increaseBranchSpacing(float original) {
        return 1.75f; // Branch gap
    }



    private static final Map<String, float[]> POSITIONS = Map.ofEntries(
            Map.entry("ttll:story/root", new float[]{0.0f, 0.0f}),
            Map.entry("ttll:story/guide", new float[]{-1.5f, 1.0f}),
                Map.entry("ttll:story/destroy_leaves", new float[]{-1.0f, -2.0f}),
                    Map.entry("ttll:story/acquire_branch", new float[]{-3.0f, -1.0f}),
                        Map.entry("ttll:story/acquire_pointy_stick", new float[]{-5.5f, -2.5f}),
                    Map.entry("ttll:story/acquire_stick", new float[]{-2.5f, -3.0f}),
                        Map.entry("ttll:story/acquire_wooden_club", new float[]{-1.0f, -4.0f}),
                            Map.entry("ttll:story/kill_an_animal", new float[]{-4.0f, -5.0f}),
                                Map.entry("ttll:story/eat_food", new float[]{-7.0f, -6.0f}),
                                    Map.entry("ttll:story/balanced_diet", new float[]{-9.0f, -7.0f}),
                                Map.entry("ttll:story/acquire_raw_leather", new float[]{-2.0f, -6.0f}),
                                    Map.entry("ttll:story/drying_rack", new float[]{-4.0f, -7.0f}),
                                        Map.entry("ttll:story/acquire_leather", new float[]{-6.0f, -8.0f}),
                                            Map.entry("ttll:story/leather_armor", new float[]{-8.0f, -8.0f}),
                                Map.entry("ttll:story/knitt_wool", new float[]{-6.0f, -4.0f}),
                            Map.entry("ttll:story/kill_a_monster", new float[]{2.0f, -3.0f}),
                                Map.entry("ttll:story/kill_all_monsters", new float[]{4.0f, -2.0f}),
                Map.entry("ttll:story/vicinity_gravel", new float[]{2.0f, 1.0f}),
                    Map.entry("ttll:story/acquire_crude_flint", new float[]{0.5f, 2.0f}),
                    Map.entry("ttll:story/vicinity_stone", new float[]{4.0f, 1.5f}),
                        Map.entry("ttll:story/acquire_flint", new float[]{5.0f, 2.0f})




    );


    @Redirect(
            method = "apply",
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V")
    )

    private void applyWithManualPositions(Optional<AdvancementDisplay> optional, Consumer<? super AdvancementDisplay> action) {
        if (optional.isPresent()) {
            AdvancementDisplay display = optional.get();
            String id = this.advancement.getAdvancementEntry().id().toString();

            // Seed with ID so it's the same every time for this specific widget
            Random random = new Random(id.hashCode());

            // Independent random values so it's not just a 45-degree diagonal shift

            float scale = 1f;
            float jiggleX = ((random.nextFloat() * 0.4f) - 0.2f) * scale;
            float jiggleY = ((random.nextFloat() * 0.4f) - 0.2f) * scale;

            float X_Centering = 10.0f;
            float Y_Centering = 8.0f;

            if (POSITIONS.containsKey(id)) {
                float[] basePos = POSITIONS.get(id);
                display.setPos((basePos[0] *scale) + jiggleX + X_Centering, (basePos[1]*scale) + jiggleY + Y_Centering);
            }
        }
    }
}