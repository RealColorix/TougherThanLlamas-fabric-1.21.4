package net.colorixer.block.furnace;

import net.colorixer.block.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class FurnaceBlockEntity extends BlockEntity {

    // Single inventory slot for "in-place" transformation
    private ItemStack inventory = ItemStack.EMPTY;

    private int fuel = 0;
    private int cookTime = 0;
    private int cookTimeTotal = 0;
    private boolean isLit = false;
    public static final int MAX_FUEL = 14000;

    public FurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FURNACEBLOCKENTITY, pos, state);
    }

    public void tick() {
        if (this.world == null || this.world.isClient) return;

        BlockState state = this.getCachedState();
        boolean isLit = state.get(FurnaceBlock.LIT);
        boolean isLow = state.get(FurnaceBlock.LOW_FUEL);

        // 1. Handle Fuel Consumption
        if (isLit) {
            if (this.fuel > 0) {
                this.fuel--;
                boolean currentlyLow = this.fuel <= 3000;
                if (currentlyLow != isLow) {
                    world.setBlockState(pos, state.with(FurnaceBlock.LOW_FUEL, currentlyLow), 3);
                }
            } else {
                this.fuel = 0;
                world.setBlockState(pos, state.with(FurnaceBlock.LIT, false).with(FurnaceBlock.LOW_FUEL, false), 3);
                this.cookTime = 0;
                this.markDirty();
                return;
            }
        }

        // 2. Handle Cooking Logic
        if (isLit && !this.inventory.isEmpty()) {
            var recipe = net.colorixer.block.furnace.FurnaceRecipes.get(this.inventory);

            if (recipe != null) {
                // UPDATE: Set the total time from your FurnaceRecipe class
                this.cookTimeTotal = recipe.cookTime(); // This uses the 12000, 1800, etc.

                this.cookTime++;

                // Use the dynamic cookTimeTotal instead of a hardcoded 200
                if (this.cookTime >= this.cookTimeTotal) {
                    this.inventory = recipe.output().copy();
                    this.cookTime = 0;
                    syncToClient();
                }
            } else {
                this.cookTime = 0;
                this.cookTimeTotal = 0;
            }
        } else {
            this.cookTime = 0;
            this.cookTimeTotal = 0;
        }

        this.markDirty();
    }
    public boolean onRightClick(PlayerEntity player, Hand hand) {
        ItemStack stackInHand = player.getStackInHand(hand);

        // 1. Picking up the item
        if (!inventory.isEmpty()) {
            player.getInventory().offerOrDrop(inventory);
            inventory = ItemStack.EMPTY;
            this.cookTime = 0; // RESET timer immediately when picked up
            syncToClient();
            return true;
        }

        // 2. Putting an item in
        if (!stackInHand.isEmpty()) {
            if (FurnaceRecipes.get(stackInHand) != null) {
                inventory = stackInHand.split(1);
                this.cookTime = 0; // Start new item at zero
                syncToClient();
                return true;
            }
        }
        return false;
    }

    public int getFuel() {
        return this.fuel;
    }

    public void ignite() {
        if (this.fuel > 0 && this.world != null) {
            // This is what actually makes the tick() start running the "isLit" code
            this.world.setBlockState(pos, this.getCachedState().with(FurnaceBlock.LIT, true), 3);
            syncToClient();
        }
    }


    // Update your addFuel method to be more precise
    public void addFuel(int amount) {
        this.fuel = Math.min(this.fuel + amount, MAX_FUEL);
        markDirty();
    }

    /* ---------- Sync & NBT ---------- */

    private void syncToClient() {
        markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (!inventory.isEmpty()) {
            nbt.put("Inventory", inventory.toNbt(registryLookup));
        }
        nbt.putInt("Fuel", fuel);
        nbt.putInt("CookTime", cookTime);
        nbt.putBoolean("IsLit", isLit);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.inventory = ItemStack.fromNbt(registryLookup, nbt.getCompound("Inventory")).orElse(ItemStack.EMPTY);
        this.fuel = nbt.getInt("Fuel");
        this.cookTime = nbt.getInt("CookTime");
        this.isLit = nbt.getBoolean("IsLit");
    }

    /* ---------- Getters ---------- */

    public ItemStack getInventory() {
        return inventory;
    }

    public float getCookProgress() {
        return cookTimeTotal == 0 ? 0 : (float) cookTime / cookTimeTotal;
    }
}