package net.colorixer.block.campfire;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class CampfireBlockEntityRenderer implements BlockEntityRenderer<CampfireBlockEntity> {
    public CampfireBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(CampfireBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stack = entity.getInventory();
        if (stack.isEmpty()) return;

        Direction facing = entity.getCachedState().get(CampfireBlock.FACING);

        matrices.push();

        matrices.translate(0.5, 0.75, 0.5);

        // Rotate to match the block direction
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.getPositiveHorizontalDegrees()-90));


        // Scale down so it fits on the logs
        matrices.scale(0.5f, 0.5f, 0.75f);

        MinecraftClient.getInstance().getItemRenderer().renderItem(
                stack,
                ModelTransformationMode.FIXED,
                light,
                overlay,
                matrices,
                vertexConsumers,
                entity.getWorld(),
                0
        );

        matrices.pop();
    }
    /** FÖR MODEL FILERNA
    ,
    {
        "name": "north_south_fire",
            "from": [8, 0, 0],
        "to": [8, 16, 16],
        "rotation": {"angle": 0, "axis": "y", "origin": [8, 8, 8]},
        "faces": {
        "east": {"uv": [0, 0, 16, 16], "texture": "#2"},
        "west": {"uv": [0, 0, 16, 16], "texture": "#2"}
    }
    },
    {
        "name": "east_west_fire",
            "from": [0, 0, 8],
        "to": [16, 16, 8],
        "rotation": {"angle": 0, "axis": "y", "origin": [8, 8, 8]},
        "faces": {
        "north": {"uv": [0, 0, 16, 16], "texture": "#2"},
        "south": {"uv": [0, 0, 16, 16], "texture": "#2"}
    }
    }
     **/
}


