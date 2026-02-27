package net.colorixer.block;

import net.colorixer.TougherThanLlamas;
import net.colorixer.block.brick_block.WetBrickBlockEntity;
import net.colorixer.block.campfire.CampfireBlockEntity;
import net.colorixer.block.drying_rack.DryingRackBlockEntity;
import net.colorixer.block.furnace.FurnaceBlockEntity;
import net.colorixer.block.torch.BurningCrudeTorchBlockEntity;
import net.colorixer.util.IdentifierUtil;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {

    public static BlockEntityType<CampfireBlockEntity> CAMPFIREBLOCKENTITY;
    public static BlockEntityType<FurnaceBlockEntity> FURNACEBLOCKENTITY;
    public static BlockEntityType<WetBrickBlockEntity> WET_BRICK_BLOCK_ENTITY;
    public static BlockEntityType<DryingRackBlockEntity> DRYING_RACK_BLOCK_ENTITY;
    public static BlockEntityType<BurningCrudeTorchBlockEntity> BURNING_CRUDE_TORCH;

    public static void register() {
        TougherThanLlamas.LOGGER.info("Registering Mod Block Entities for " + TougherThanLlamas.MOD_ID);

        BURNING_CRUDE_TORCH = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                // Changed "furnace" to "crude_torch" to avoid conflicts
                IdentifierUtil.createIdentifier("ttll", "burning_crude_torch"),
                FabricBlockEntityTypeBuilder.create(
                        BurningCrudeTorchBlockEntity::new, // This must be the BlockEntity constructor
                        ModBlocks.BURNING_CRUDE_TORCH // The block it attaches to
                ).build()
        );

        FURNACEBLOCKENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                IdentifierUtil.createIdentifier("ttll", "furnace"),
                FabricBlockEntityTypeBuilder.create(FurnaceBlockEntity::new, ModBlocks.FURNACE).build()
        );

        CAMPFIREBLOCKENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                IdentifierUtil.createIdentifier("ttll", "campfire"),
                FabricBlockEntityTypeBuilder.create(CampfireBlockEntity::new, ModBlocks.CAMPFIRE).build()
        );

        WET_BRICK_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                IdentifierUtil.createIdentifier("ttll", "wet_brick"),
                FabricBlockEntityTypeBuilder.create(WetBrickBlockEntity::new, ModBlocks.WET_BRICK).build()
        );

        DRYING_RACK_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                IdentifierUtil.createIdentifier("ttll", "drying_rack"),
                FabricBlockEntityTypeBuilder.create(DryingRackBlockEntity::new, ModBlocks.DRYING_RACK).build()
        );
    }
}
