package net.colorixer;

import net.colorixer.block.ModBlocks;
import net.colorixer.item.ItemsThatCanHitAndBreak;
import net.colorixer.item.ModItems;
import net.colorixer.player.Chopable;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TougherThanLlamas implements ModInitializer {
	public static final String MOD_ID = "ttll";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ItemsThatCanHitAndBreak.register();
		Chopable.initialize();


	}
}