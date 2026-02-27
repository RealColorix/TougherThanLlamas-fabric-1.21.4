package net.colorixer.entity.client;

import net.colorixer.entity.projectile.BoneProjectile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.math.RotationAxis;

public class BoneProjectileRenderer extends EntityRenderer<BoneProjectile, BoneProjectileRenderer.BoneProjectileRenderState> {

    private static final ItemStack BONE_STACK = new ItemStack(Items.BONE);

    public BoneProjectileRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public BoneProjectileRenderState createRenderState() {
        return new BoneProjectileRenderState();
    }

    @Override
    public void updateRenderState(BoneProjectile entity, BoneProjectileRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.age = entity.age + tickDelta;
        state.id = entity.getId();
    }

    @Override
    public void render(BoneProjectileRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // Spin logic
        float rotation = state.age * 20.0F;
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
        matrices.translate(0, -0.125, 0);

        // FIX: Get the ItemRenderer directly from the MinecraftClient
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        itemRenderer.renderItem(
                BONE_STACK,
                ModelTransformationMode.GROUND,
                light,
                OverlayTexture.DEFAULT_UV,
                matrices,
                vertexConsumers,
                null,
                state.id
        );

        matrices.pop();
        super.render(state, matrices, vertexConsumers, light);
    }

    public static class BoneProjectileRenderState extends EntityRenderState {
        public float age;
        public int id;
    }
}