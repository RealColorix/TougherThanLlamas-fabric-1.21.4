package net.colorixer.entity.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;

@Environment(EnvType.CLIENT)
public class SimpleThrownItemRenderer<T extends Entity>
        extends EntityRenderer<T, SimpleThrownItemRenderer.ThrownItemState> {

    private final ItemRenderer itemRenderer;

    public SimpleThrownItemRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
    }

    /* ---------------- STATE ---------------- */

    public static class ThrownItemState extends EntityRenderState {
        public ItemStack stack = ItemStack.EMPTY;
    }

    /* ---------------- STATE UPDATE ---------------- */

    @Override
    public void updateRenderState(T entity, ThrownItemState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);

        // ALLA SnowballEntity (och din CobwebProjectile) har getStack()
        if (entity instanceof net.minecraft.entity.projectile.thrown.ThrownItemEntity thrown) {
            state.stack = thrown.getStack();
        } else {
            state.stack = ItemStack.EMPTY;
        }
    }

    /* ---------------- RENDER ---------------- */

    @Override
    public void render(ThrownItemState state,
                       MatrixStack matrices,
                       VertexConsumerProvider consumers,
                       int light) {

        if (state.stack.isEmpty()) return;

        matrices.push();

        // Billboard mot kameran (vanilla-beteende)
        matrices.multiply(this.dispatcher.getRotation());

        matrices.scale(1F, 1F, 1F);

        this.itemRenderer.renderItem(
                state.stack,
                ModelTransformationMode.GROUND,
                light,
                OverlayTexture.DEFAULT_UV,
                matrices,
                consumers,
                null,
                0
        );

        matrices.pop();
    }

    @Override
    public ThrownItemState createRenderState() {
        return new ThrownItemState();
    }
}
