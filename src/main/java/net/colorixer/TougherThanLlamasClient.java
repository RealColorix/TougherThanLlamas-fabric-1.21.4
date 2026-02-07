package net.colorixer;

import net.colorixer.block.ModBlockEntities;
import net.colorixer.block.ModBlocks;
import net.colorixer.block.drying_rack.DryingRackBlockEntityRenderer;
import net.colorixer.block.furnace.FurnaceBlockEntityRenderer;
import net.colorixer.entity.ModEntities;
import net.colorixer.entity.client.SimpleThrownItemRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.text.Text;
import net.minecraft.world.biome.GrassColors;

public class TougherThanLlamasClient implements ClientModInitializer {

	private static KeyBinding achievementScreenKey;

	private static String currentHealthStatus = "";
	private static int healthColor = 0xFFFFFF; // Default White

	private static String currentHungerStatus = "";
	private static int hungerColor = 0xFFFFFF; // Default White

	@Override


	public void onInitializeClient() {





// For the Block
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
			// This 'tintIndex' matches the 'tintindex' in your JSON file.
			if (tintIndex == 0) {
				if (world != null && pos != null) {
					return BiomeColors.getGrassColor(world, pos);
				}
				return GrassColors.getDefaultColor();
			}
			// Returning -1 means "Don't tint this texture, use the original file colors"
			return -1;
		}, ModBlocks.GRASS_SLAB);



		EntityRendererRegistry.register(ModEntities.COBWEB_PROJECTILE, SimpleThrownItemRenderer::new);

		BlockEntityRendererFactories.register(ModBlockEntities.FURNACEBLOCKENTITY, FurnaceBlockEntityRenderer::new);

		BlockEntityRendererRegistry.register(ModBlockEntities.DRYING_RACK_BLOCK_ENTITY, DryingRackBlockEntityRenderer::new);


		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GRASS_SLAB, RenderLayer.getCutoutMipped());
		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.FURNACE,
				RenderLayer.getCutout()
		);

		HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.world == null || client.player.isCreative() || client.player.isSpectator()) {
				return;
			}

			// 1. Logic Update (Every Second)
			if (client.world.getTime() != 0) {
				float health = client.player.getHealth();
				int hunger = client.player.getHungerManager().getFoodLevel();


				if (health <= 0f) {
					currentHealthStatus = "Dead";
					healthColor = 0xAA0000; // Dark Red
				} else if (health < 2.1f) {
					currentHealthStatus = "Dying";
					healthColor = 0xFF5555; // Red
				} else if (health < 4.1f) {
					currentHealthStatus = "Crippled";
					healthColor = 0xFFAA00; // Orange
				} else if (health < 8.1f) {
					currentHealthStatus = "Injured";
					healthColor = 0xFFFF55; // Yellow
				} else if (health < 12.1f) {
					currentHealthStatus = "Hurt";
					healthColor = 0xFFFFFF; // White
				} else {
					currentHealthStatus = "";
				}

				// Hunger Tiers & Colors
				if (hunger < 2.1f) {
					currentHungerStatus = "Starving";
					hungerColor = 0xFF5555; // Red
				} else if (hunger < 4.1f) {
					currentHungerStatus = "Emaciated";
					hungerColor = 0xFFAA00; // Orange
				} else if (hunger < 8.1f) {
					currentHungerStatus = "Famished";
					hungerColor = 0xFFFF55; // Yellow
				} else if (hunger < 12.1f) {
					currentHungerStatus = "Hungry";
					hungerColor = 0xFFFFFF; // White
				} else {
					currentHungerStatus = "";
				}
			}
// 2. Dynamic Rendering
			int screenWidth = client.getWindow().getScaledWidth();
			int screenHeight = client.getWindow().getScaledHeight();
			int centerX = screenWidth / 2;

			// Render Health Status (Above HP Bar - Left Aligned)
			if (!currentHealthStatus.isEmpty()) {
				int x = centerX - 91;
				// Offset upward if player has Armor OR Absorption hearts (which create a second row)
				boolean hasExtraRow = client.player.getArmor() > 0 || client.player.getAbsorptionAmount() > 0;
				int y = screenHeight - 49 - (hasExtraRow ? 10 : 0);

				drawContext.drawTextWithShadow(client.textRenderer, currentHealthStatus, x, y, healthColor);
			}

			// Render Hunger Status (Above Hunger Bar - Right Aligned)
			if (!currentHungerStatus.isEmpty()) {
				int textWidth = client.textRenderer.getWidth(currentHungerStatus);
				int x = (centerX + 91) - textWidth;
				// Offset upward if player is underwater (bubbles) or riding an entity (mount health)
				boolean hasRightExtraRow = client.player.getAir() < client.player.getMaxAir() || client.player.getVehicle() != null;
				int y = screenHeight - 49 - (hasRightExtraRow ? 10 : 0);

				drawContext.drawTextWithShadow(client.textRenderer, currentHungerStatus, x, y, hungerColor);
			}
		});
	}
}
