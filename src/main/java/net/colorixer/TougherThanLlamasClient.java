package net.colorixer;

import net.colorixer.block.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;

public class TougherThanLlamasClient implements ClientModInitializer {

	private static KeyBinding achievementScreenKey;

	@Override
	public void onInitializeClient() {


		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.WICKER,

				RenderLayer.getCutout()
		);

	}
}
