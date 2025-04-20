package net.colorixer;

import net.colorixer.block.ModBlockEntities;
import net.colorixer.block.ModBlocks;
import net.colorixer.block.brick_block.WetBrickBlockEntity;
import net.colorixer.block.brick_furnace.BrickFurnaceBlockEntityRenderer;
import net.colorixer.block.drying_rack.DryingRackBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.resource.ResourceType;

public class TougherThanLlamasClient implements ClientModInitializer {

	private static KeyBinding achievementScreenKey;

	@Override
	public void onInitializeClient() {


		BlockEntityRendererRegistry.register(ModBlockEntities.DRYING_RACK_BLOCK_ENTITY, DryingRackBlockEntityRenderer::new);
		BlockEntityRendererRegistry.register(ModBlockEntities.BRICK_FURNACE_BLOCK_ENTITY, BrickFurnaceBlockEntityRenderer::new);


		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.BRICK_FURNACE,
				RenderLayer.getCutout()
		);
	}
}
