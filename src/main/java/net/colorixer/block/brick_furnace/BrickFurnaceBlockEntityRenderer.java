package net.colorixer.block.brick_furnace;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class BrickFurnaceBlockEntityRenderer implements BlockEntityRenderer<BrickFurnaceBlockEntity> {

    private final ItemRenderer itemRenderer;

    public BrickFurnaceBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    private static final Vec3d CAST_POSITION = new Vec3d(8f / 16f, 8.5f / 16f, 8f / 16f);

    private static final Vec3d[] INGREDIENT_POSITIONS = new Vec3d[] {
            new Vec3d(4.5f/16f, 9.2f/16f, 4.5f/16f),
            new Vec3d(11.5f/16f, 9.2f/16f, 4.5f/16f),
            new Vec3d(11.5f/16f, 9.2f/16f, 11.5f/16f),
            new Vec3d(4.5f/16f, 9.2f/16f, 11.5f/16f)
    };

    private static final Vec3d FUEL_BASE = new Vec3d(0.5f, 0, 0.5f);
    private static final Vec3d[] FUEL_POSITIONS = new Vec3d[] {
            new Vec3d(-3/16f, 9/64f, -3/16f),
            new Vec3d(3/16f, 9/64f, -3/16f),
            new Vec3d(3/16f, 9/64f, 3/16f),
            new Vec3d(-3/16f, 9/64f, 3/16f),
            new Vec3d(0, 11/64f, -3/16f),
            new Vec3d(3/16f, 11/64f+0.001f, 0),
            new Vec3d(0, 11/64f, 3/16f),
            new Vec3d(-3/16f, 11/64f+0.001f, 0),
            new Vec3d(0.0, 13/64f, 0.0)
    };

    @Override
    public void render(BrickFurnaceBlockEntity entity,
                       float tickDelta,
                       MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers,
                       int light,
                       int overlay) {
        ItemStack cast = entity.getCastItem();
        if (!cast.isEmpty()) {
            matrices.push();
            matrices.translate(CAST_POSITION.x, CAST_POSITION.y, CAST_POSITION.z);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            matrices.scale(1f, 1f, 1f);
            itemRenderer.renderItem(cast, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, null, 0);
            matrices.pop();
        }
        Direction facing = entity.getCachedState().get(BrickFurnaceBlock.FACING);
        float extraRotation = 0f;
        switch (facing) {
            case EAST:
                extraRotation = 270f;
                break;
            case SOUTH:
                extraRotation = 180f;
                break;
            case WEST:
                extraRotation = 90f;
                break;
            default:
                extraRotation = 0f;
                break;
        }
        for (int i = 1; i < entity.getInventory().size(); i++) {
            ItemStack ingredient = entity.getInventory().get(i);
            if (!ingredient.isEmpty()) {
                matrices.push();
                Vec3d pos = INGREDIENT_POSITIONS[i - 1];
                matrices.translate(pos.x, pos.y, pos.z);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(extraRotation));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
                matrices.scale(0.4f, 0.4f, 0.8f);
                itemRenderer.renderItem(ingredient, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, null, 0);
                matrices.pop();
            }
        }
        for (int i = 0; i < entity.getFuelInventory().size(); i++) {
            ItemStack fuel = entity.getFuelInventory().get(i);
            if (!fuel.isEmpty()) {
                matrices.push();
                Vec3d pos = FUEL_BASE.add(FUEL_POSITIONS[i]);
                matrices.translate(pos.x, pos.y, pos.z);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
                matrices.scale(0.5f, 0.5f, 0.5f);
                itemRenderer.renderItem(fuel, ModelTransformationMode.FIXED, light, overlay, matrices, vertexConsumers, null, 0);
                matrices.pop();
            }
        }
    }
}
