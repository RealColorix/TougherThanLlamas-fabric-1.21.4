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

    private static final float killAnAnimalYChange = 3;
    private static final float acquireFlintYChange = -1.5f;
    private static final float obtainATwineYChange = -0.5f;
    private static final float mineIronOreYChange = 1f;

    private static final Map<String, float[]> POSITIONS = Map.ofEntries(


            Map.entry("ttll:tougherthanllamas/root", new float[]{0f, 0f}),
                Map.entry("ttll:tougherthanllamas/guide", new float[]{1.0f, 0.0f}),
                Map.entry("ttll:tougherthanllamas/acquire_wooden_club", new float[]{1.0f, 1.5f}),
                    Map.entry("ttll:tougherthanllamas/kill_an_animal", new float[]{2.0f, 0f + killAnAnimalYChange}),
                        Map.entry("ttll:tougherthanllamas/eat_food", new float[]{3.0f, 1f + killAnAnimalYChange}),
                            Map.entry("ttll:tougherthanllamas/balanced_diet", new float[]{4.0f, 1 + killAnAnimalYChange}),
                        Map.entry("ttll:tougherthanllamas/acquire_raw_leather", new float[]{3.0f, 0 + killAnAnimalYChange}),
                            Map.entry("ttll:tougherthanllamas/acquire_leather", new float[]{4.0f, 0 + killAnAnimalYChange}),
                                Map.entry("ttll:tougherthanllamas/leather_armor", new float[]{5.0f, 0 + killAnAnimalYChange}),
                         Map.entry("ttll:tougherthanllamas/knitt_wool", new float[]{3.0f, -1f + killAnAnimalYChange}),
                    Map.entry("ttll:tougherthanllamas/kill_a_monster", new float[]{2.0f, 1f}),
                        Map.entry("ttll:tougherthanllamas/kill_all_monsters", new float[]{3.0f, 1}),
                Map.entry("ttll:tougherthanllamas/acquire_flint", new float[]{1f, 0 + acquireFlintYChange}),
                    Map.entry("ttll:tougherthanllamas/sharp_rock", new float[]{2f, 0 + acquireFlintYChange}),
                        Map.entry("ttll:tougherthanllamas/obtain_a_twine", new float[]{3f, 0 + acquireFlintYChange + obtainATwineYChange}),
                            Map.entry("ttll:tougherthanllamas/craft_flint_axe", new float[]{4f, 0.5f + acquireFlintYChange + obtainATwineYChange}),
                            Map.entry("ttll:tougherthanllamas/craft_stone_shovel", new float[]{4f, -0.5f + acquireFlintYChange + obtainATwineYChange}),
                                Map.entry("ttll:tougherthanllamas/furnace", new float[]{5f, -0.5f + acquireFlintYChange + obtainATwineYChange}),
                        Map.entry("ttll:tougherthanllamas/mine_iron_ore", new float[]{3f, 0 + acquireFlintYChange + mineIronOreYChange}),
                            Map.entry("ttll:tougherthanllamas/iron_chisel", new float[]{4f, 0 + acquireFlintYChange + mineIronOreYChange}),
                                Map.entry("ttll:tougherthanllamas/vicinity_crafting_table", new float[]{5f, 0 + acquireFlintYChange + mineIronOreYChange}),
                                    Map.entry("ttll:tougherthanllamas/smelt_iron", new float[]{6f, 0 + acquireFlintYChange + mineIronOreYChange})
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

            float scale = 1.25f;
            float jiggleX = 0;// ((random.nextFloat() * 0.2f) - 0.1f) * scale;
            float jiggleY = 0;//((random.nextFloat() * 0.2f) - 0.1f) * scale;

            float X_Centering =   0f;// 10.0f;
            float Y_Centering =  - 8f;// 8.0f;

            if (POSITIONS.containsKey(id)) {
                float[] basePos = POSITIONS.get(id);
                display.setPos((basePos[0] *scale) + jiggleX + X_Centering, ((basePos[1]*scale) + jiggleY + Y_Centering) *-1 );
            }
        }
    }
}