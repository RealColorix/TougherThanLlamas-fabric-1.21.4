package net.colorixer.block.drying_rack;

import net.colorixer.block.ModBlockEntities;
import net.colorixer.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class DryingRackBlockEntity extends BlockEntity {

    /** two slots – 0 ⇢ north‑side, 1 ⇢ south‑side */
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private final int[] dryingTime = new int[]{0, 0};
    public static final int MAX_DRYING_TIME = 24_000;

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK_BLOCK_ENTITY, pos, state);
    }

    /* -------------------------------------------------------------- tick */
    public void tick() {
        if (world == null || world.isClient) return;

        for (int slot = 0; slot < 2; slot++) {
            ItemStack stack = inventory.get(slot);

            if (!stack.isEmpty()) {
                if (stack.isOf(ModItems.RAW_LEATHER)) {
                    dryingTime[slot]++;
                    if (dryingTime[slot] >= MAX_DRYING_TIME) {
                        inventory.set(slot, new ItemStack(Items.LEATHER));
                        dryingTime[slot] = 0;
                        markDirtyAndUpdate();
                    }
                } else if (stack.isOf(ModItems.LAYERED_BAGASSE)) {
                    dryingTime[slot]++;
                    if (dryingTime[slot] >= MAX_DRYING_TIME) {
                        ItemStack parchment = new ItemStack(ModItems.PARCHMENT);
                        // Apply custom texture for Parchment while it stays on the rack
                        parchment.set(
                                DataComponentTypes.CUSTOM_MODEL_DATA,
                                new CustomModelDataComponent(List.of(101f), List.of(), List.of(), List.of())
                        );
                        inventory.set(slot, parchment);
                        dryingTime[slot] = 0;
                        markDirtyAndUpdate();
                    }
                } else {
                    dryingTime[slot] = 0;
                }
            } else {
                dryingTime[slot] = 0;
            }
        }
    }

    public void dropContents() {
        if (world == null || world.isClient) return;

        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                ItemStack drop = stack.copy();
                // Strip the custom texture data when it falls on the ground
                drop.remove(DataComponentTypes.CUSTOM_MODEL_DATA);

                world.spawnEntity(
                        new ItemEntity(
                                world,
                                pos.getX() + 0.5,
                                pos.getY() + 0.5,
                                pos.getZ() + 0.5,
                                drop
                        )
                );
            }
        }
        inventory.clear();
        markDirty();                    // keep NBT in sync
    }

    /* ---------------------------------------------------------- helpers */
    public ItemStack getStack(int slot) { return inventory.get(slot); }

    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        dryingTime[slot] = 0;
        markDirtyAndUpdate();
    }

    public ItemStack removeStack(int slot) {
        ItemStack out = inventory.get(slot).copy();
        // Strip the custom texture data when the player takes it
        out.remove(DataComponentTypes.CUSTOM_MODEL_DATA);

        inventory.set(slot, ItemStack.EMPTY);
        dryingTime[slot] = 0;
        markDirtyAndUpdate();
        return out;
    }

    private void markDirtyAndUpdate() {
        markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    /* ------------------------------------------------- NBT + networking */
    @Override public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup r) {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt, r);
        return nbt;
    }

    @Override protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup r) {
        super.writeNbt(nbt, r);
        Inventories.writeNbt(nbt, inventory, true, r);
        nbt.putIntArray("DryTimes", dryingTime);
    }

    @Override public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup r) {
        super.readNbt(nbt, r);
        inventory.clear();
        Inventories.readNbt(nbt, inventory, r);
        int[] arr = nbt.getIntArray("DryTimes");
        if (arr.length == 2) { dryingTime[0] = arr[0]; dryingTime[1] = arr[1]; }
    }

    /* ------------------------------------------------------- drop loot */
    public void dropAll() {
        if (world != null && !world.isClient) {
            for (ItemStack st : inventory) {
                if (!st.isEmpty()) {
                    ItemStack drop = st.copy();
                    // Strip the custom texture data when it falls on the ground
                    drop.remove(DataComponentTypes.CUSTOM_MODEL_DATA);

                    world.spawnEntity(new ItemEntity(world,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            drop));
                }
            }
            inventory.clear();
            markDirty();
        }
    }
}