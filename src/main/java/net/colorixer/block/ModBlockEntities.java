package net.colorixer.block;

import net.colorixer.block.brick_block.WetBrickBlockEntity;
import net.colorixer.block.drying_rack.DryingRackBlockEntity;
import net.colorixer.block.furnace.FurnaceBlockEntity;
import net.colorixer.util.IdentifierUtil;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {

    public static BlockEntityType<FurnaceBlockEntity> FURNACEBLOCKENTITY;
    public static BlockEntityType<WetBrickBlockEntity> WET_BRICK_BLOCK_ENTITY;
    public static BlockEntityType<DryingRackBlockEntity> DRYING_RACK_BLOCK_ENTITY;


    public static void register() {


        FURNACEBLOCKENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                IdentifierUtil.createIdentifier("ttll", "furnace"),
                FabricBlockEntityTypeBuilder.create(FurnaceBlockEntity::new, ModBlocks.FURNACE).build()
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
