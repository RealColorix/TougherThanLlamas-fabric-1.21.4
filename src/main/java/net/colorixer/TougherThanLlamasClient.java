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
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.text.Text;

public class TougherThanLlamasClient implements ClientModInitializer {

	private static KeyBinding achievementScreenKey;

	private static String currentStatus = "";


	@Override


	public void onInitializeClient() {


		EntityRendererRegistry.register(ModEntities.COBWEB_PROJECTILE, SimpleThrownItemRenderer::new);

		BlockEntityRendererFactories.register(ModBlockEntities.FURNACEBLOCKENTITY, FurnaceBlockEntityRenderer::new);

		BlockEntityRendererRegistry.register(ModBlockEntities.DRYING_RACK_BLOCK_ENTITY, DryingRackBlockEntityRenderer::new);


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
			if (client.world.getTime() % 20 == 0) {
				float health = client.player.getHealth();
				int hunger = client.player.getHungerManager().getFoodLevel();

				String healthStatus = "";
				String hungerStatus = "";

				// Updated Health Tiers
				if (health <= 5.0f) { healthStatus = "Crippled"; }
				else if (health <= 10.0f) { healthStatus = "Hurt"; }
				else if (health <= 15.0f) { healthStatus = "Bruised"; }

				// Updated Hunger Tiers
				if (hunger <= 5) { hungerStatus = "Starving"; }
				else if (hunger <= 10) { hungerStatus = "Emaciated"; }
				else if (hunger <= 15) { hungerStatus = "Hungry"; }

				if (!healthStatus.isEmpty() && !hungerStatus.isEmpty()) {
					currentStatus = healthStatus + " & " + hungerStatus;
				} else if (!healthStatus.isEmpty()) {
					currentStatus = healthStatus;
				} else if (!hungerStatus.isEmpty()) {
					currentStatus = hungerStatus;
				} else {
					currentStatus = "";
				}
			}

			// 2. Dynamic Rendering (Every Frame)
			if (!currentStatus.isEmpty()) {
				int x = client.getWindow().getScaledWidth() / 2 + 10;

				// BASE POSITION: Above hunger bar
				int y = client.getWindow().getScaledHeight() - 49;

				// BUMP LOGIC:
				// If player is underwater (air < 300) OR riding a horse/vehicle
				boolean isUnderwater = client.player.getAir() < 300;
				boolean isRiding = client.player.getVehicle() != null;

				if (isUnderwater || isRiding) {
					y -= 10; // Shift up by 10 pixels to clear the air/mount bar
				}

				drawContext.drawTextWithShadow(
						client.textRenderer,
						Text.of(currentStatus),
						x, y,
						0xFFFFFF
				);
			}
		});
	}
}
