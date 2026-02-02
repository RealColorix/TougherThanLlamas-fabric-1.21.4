package net.colorixer.block.furnace;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class FurnaceBlockEntityRenderer implements BlockEntityRenderer<FurnaceBlockEntity> {
    public FurnaceBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(FurnaceBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemStack stack = entity.getInventory(); // Use the single inventory field
        if (stack.isEmpty()) return;

        Direction facing = entity.getCachedState().get(FurnaceBlock.FACING);

        matrices.push();
        matrices.translate(0.5, 0.515625, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.getPositiveHorizontalDegrees()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
        matrices.translate(0, -0.1, 0.0);
        matrices.scale(0.5f, 0.5f, 0.5f);

        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);
        matrices.pop();
    }
}