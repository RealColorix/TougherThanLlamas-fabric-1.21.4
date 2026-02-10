package net.colorixer;

import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.colorixer.block.ModBlockEntities;
import net.colorixer.block.ModBlocks;
import net.colorixer.block.campfire.CampfireBlockEntityRenderer;
import net.colorixer.block.drying_rack.DryingRackBlockEntityRenderer;
import net.colorixer.block.furnace.FurnaceBlockEntityRenderer;
import net.colorixer.block.torch.BurningCrudeTorchItem;
import net.colorixer.component.ModDataComponentTypes;
import net.colorixer.entity.ModEntities;
import net.colorixer.entity.client.SimpleThrownItemRenderer;
import net.colorixer.entity.spiders.JungleSpiderEntity;
import net.colorixer.item.ModItems;
import net.colorixer.mixin.LivingEntityRendererInvoker;
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
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SpiderEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.GrassColors;
import org.jetbrains.annotations.NotNull;

public class TougherThanLlamasClient implements ClientModInitializer {

	private static KeyBinding achievementScreenKey;

	private static String currentHealthStatus = "";
	private static int healthColor = 0xFFFFFF; // Default White

	private static String currentHungerStatus = "";
	private static int hungerColor = 0xFFFFFF; // Default White

	@Override


	public void onInitializeClient() {




		EntityRendererRegistry.register(ModEntities.JUNGLE_SPIDER, (context) -> {
			var renderer = new MobEntityRenderer<JungleSpiderEntity, LivingEntityRenderState, SpiderEntityModel>(
					context,
					new SpiderEntityModel(context.getPart(EntityModelLayers.CAVE_SPIDER)),
					0.4f
			) {
				@Override
				public LivingEntityRenderState createRenderState() {
					return new LivingEntityRenderState();
				}

				@Override
				public Identifier getTexture(LivingEntityRenderState state) {
					return Identifier.of("ttll", "textures/entity/spider/jungle_spider.png");
				}
			};

			// Use the Invoker to bypass the 'protected' access
			// Cast to Object first, then to your Invoker
			((LivingEntityRendererInvoker) (Object) renderer).callAddFeature(
					new net.minecraft.client.render.entity.feature.SpiderEyesFeatureRenderer<>(renderer)
			);

			return renderer;
		});



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
		BlockEntityRendererFactories.register(ModBlockEntities.CAMPFIREBLOCKENTITY, CampfireBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(ModBlockEntities.DRYING_RACK_BLOCK_ENTITY, DryingRackBlockEntityRenderer::new);

		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.COBWEB_FUll, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CAMPFIRE, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GRASS_SLAB, RenderLayer.getCutoutMipped());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.FURNACE, RenderLayer.getCutout());

		HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.world == null || client.player.isCreative() || client.player.isSpectator()) {
				return;
			}

			// 1. Logic Update
			if (client.world.getTime() != 0) {
				float health = client.player.getHealth();
				int hunger = client.player.getHungerManager().getFoodLevel();

				// Health Tiers (kept your original logic)
				if (health <= 0f) { currentHealthStatus = "Dead"; healthColor = 0xAA0000; }
				else if (health < 2.01f) { currentHealthStatus = "Dying"; healthColor = 0xFF5555; }
				else if (health < 4.01f) { currentHealthStatus = "Crippled"; healthColor = 0xFFAA00; }
				else if (health < 8.01f) { currentHealthStatus = "Injured"; healthColor = 0xFFFF55; }
				else if (health < 12.01f) { currentHealthStatus = "Hurt"; healthColor = 0xFFFFFF; }
				else { currentHealthStatus = ""; }

				// Hunger Tiers (kept your original logic)
				if (hunger < 2.01f) { currentHungerStatus = "Starving"; hungerColor = 0xFF5555; }
				else if (hunger < 4.01f) { currentHungerStatus = "Emaciated"; hungerColor = 0xFFAA00; }
				else if (hunger < 8.01f) { currentHungerStatus = "Famished"; hungerColor = 0xFFFF55; }
				else if (hunger < 12.01f) { currentHungerStatus = "Hungry"; hungerColor = 0xFFFFFF; }
				else { currentHungerStatus = ""; }
			}

			// 2. Dynamic Rendering
			// 2. Dynamic Rendering
			int screenWidth = client.getWindow().getScaledWidth();
			int screenHeight = client.getWindow().getScaledHeight();
			int centerX = screenWidth / 2;

// --- STEP 1: Determine Health Status Position ---
			int healthStatusX = centerX - 91;
			boolean hasExtraRow = client.player.getArmor() > 0 || client.player.getAbsorptionAmount() > 0;
			int healthY = screenHeight - 49 - (hasExtraRow ? 10 : 0);

// Render Health Status if it exists
			if (!currentHealthStatus.isEmpty()) {
				drawContext.drawTextWithShadow(client.textRenderer, currentHealthStatus, healthStatusX, healthY, healthColor);
			}

// --- STEP 2: Render Gloom Status (Pushed by Health Status) ---
			if (net.colorixer.util.GloomHelper.gloomLevel > 0.05f) {
				String gloomText = "Gloom";
				int gloomColor = 0xFFFFFF;
				int ticks = net.colorixer.util.GloomHelper.gloomTicks;

				// Get current TPS to keep timing consistent
				float tps = client.world.getTickManager().getTickRate();
				int dreadThreshold = (int)(60 * tps);  // 1 minute
				int terrorThreshold = (int)(120 * tps); // 2 minutes
				int maxFadeThreshold = (int)(60 * tps); // 1 minute fade duration

				// Logic for Stage Names and Colors
				if (ticks <= dreadThreshold) {
					gloomText = "Gloom";
					gloomColor = 0xFFFFFF;
				} else if (ticks <= terrorThreshold) {
					gloomText = "Dread";
					gloomColor = 0xFFFF55;
				} else {
					gloomText = "Terror";
					float fadeProgress = Math.min(1.0f, (float)(ticks - terrorThreshold) / maxFadeThreshold);
					int r = (int) (255 - (85 * fadeProgress));
					int g = (int) (85 - (85 * fadeProgress));
					int b = (int) (85 - (85 * fadeProgress));
					gloomColor = (r << 16) | (g << 8) | b;
				}

				// --- CALCULATE DYNAMIC X OFFSET ---
				// Added +1 to fix the "too far left" alignment issue
				int gloomX = healthStatusX + 1;

				if (!currentHealthStatus.isEmpty()) {
					String separator = " & ";
					int healthWidth = client.textRenderer.getWidth(currentHealthStatus);

					// Draw separator exactly after the health status width
					drawContext.drawTextWithShadow(client.textRenderer, separator, healthStatusX + healthWidth, healthY, 0xAAAAAA);

					// Shift the gloom text to start after the health status AND the separator
					gloomX = healthStatusX + healthWidth + client.textRenderer.getWidth(separator);
				}

				// Finally, draw the Gloom text
				drawContext.drawTextWithShadow(client.textRenderer, gloomText, gloomX, healthY, gloomColor);
			}

// --- STEP 3: Render Hunger Status (Stays alone on the right) ---
			if (!currentHungerStatus.isEmpty()) {
				int textWidth = client.textRenderer.getWidth(currentHungerStatus);
				int x = (centerX + 91) - textWidth;
				boolean hasRightExtraRow = client.player.getAir() < client.player.getMaxAir() || client.player.getVehicle() != null;
				int y = screenHeight - 49 - (hasRightExtraRow ? 10 : 0);
				drawContext.drawTextWithShadow(client.textRenderer, currentHungerStatus, x, y, hungerColor);
			}
		});
	}
}
