package net.colorixer.block.drying_rack;

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

public class DryingRackBlockEntityRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {

    private final ItemRenderer itemRenderer;
    public DryingRackBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemRenderer = ctx.getItemRenderer();
    }

    /* slot‑centre positions when the block itself is facing NORTH */
    private static final Vec3d NORTH_POS = new Vec3d( 8 / 16f, 7.5f / 16f,  4.5f / 16f);   // slot 0
    private static final Vec3d SOUTH_POS = new Vec3d( 8 / 16f, 7.5f / 16f, 11.5f / 16f);   // slot 1
    private static final float SLANT      = 67.5f;                                    // inward leg angle

    @Override
    public void render(DryingRackBlockEntity be, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vcp, int light, int overlay) {

        /* yaw from block‑facing */
        Direction facing = be.getCachedState().get(DryingRackBlock.FACING);
        float rotY = switch (facing) {
            case EAST  -> 270f;
            case SOUTH -> 180f;
            case WEST  ->  90f;
            default    ->   0f;          // NORTH
        };

        for (int slot = 0; slot < 2; slot++) {
            ItemStack stack = be.getStack(slot);
            if (stack.isEmpty()) continue;

            matrices.push();

            /* translate slot centre to world space … */
            Vec3d base = slot == 0 ? NORTH_POS : SOUTH_POS;
            double offX = base.x - 0.5, offZ = base.z - 0.5;
            double rad  = Math.toRadians(rotY);
            double rx   =  offX * Math.cos(rad) - offZ * Math.sin(rad);
            double rz   =  offX * Math.sin(rad) + offZ * Math.cos(rad);
            matrices.translate(0.5 + rx, base.y, 0.5 + rz);

            /* yaw to block facing */
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotY));

            /* pitch → lay flat + inward slant */
            float slant = (slot == 0 ?  SLANT : -SLANT);
            if (rotY % 180 == 0) slant = -slant;          // mirror for N / S
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90f + slant));

            /* NEW: flip the texture if required */
            boolean flip = (slot == 1) ^ (rotY % 180 != 0);
            if (flip) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f));
            }

            /* render */
            itemRenderer.renderItem(stack, ModelTransformationMode.FIXED,
                    light, overlay, matrices, vcp, null, 0);
            matrices.pop();
        }

    }
}
