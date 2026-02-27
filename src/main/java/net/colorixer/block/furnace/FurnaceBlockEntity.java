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
import net.minecraft.util.math.Direction;

public class FurnaceBlockEntity extends BlockEntity {

    // Single inventory slot for "in-place" transformation
    private ItemStack inventory = ItemStack.EMPTY;

    private int fuel = 0;
    private int cookTime = 0;
    private int cookTimeTotal = 0;
    private boolean isLit = false;
    public static final int MAX_FUEL = 16000;

    public FurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FURNACEBLOCKENTITY, pos, state);
    }

    public void tick() {
        if (this.world == null || this.world.isClient) return;

        BlockState state = this.getCachedState();
        boolean isLit = state.get(FurnaceBlock.LIT);
        int currentFuelLevel = state.get(FurnaceBlock.FUEL_LEVEL);

        if (isLit) {
            if (this.fuel > 0) {
                this.fuel--;
                if (world.random.nextInt(100) == 0) {
                    this.ttll$tryFurnaceSpread();
                }
            } else {
                this.fuel = 0;
                // Reset to 0 when empty
                world.setBlockState(pos, state.with(FurnaceBlock.LIT, false).with(FurnaceBlock.FUEL_LEVEL, 0), 3);
                this.cookTime = 0;
                this.markDirty();
                return;
            }
        }

        // Updated math: division by 2000 capped at 8
        // 0: 0 | 1-2000: 1 | ... | 14001-16000: 8
        int calculatedLevel = (this.fuel == 0) ? 0 : Math.min(8, (this.fuel - 1) / 2000 + 1);

        if (calculatedLevel != currentFuelLevel) {
            world.setBlockState(pos, this.getCachedState().with(FurnaceBlock.FUEL_LEVEL, calculatedLevel), 3);
        }

        // 3. Handle Cooking Logic
        if (isLit && !this.inventory.isEmpty()) {
            var recipe = net.colorixer.block.furnace.FurnaceRecipes.get(this.inventory);
            if (recipe != null) {
                this.cookTimeTotal = recipe.cookTime();
                this.cookTime++;
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


    public void addFuel(int amount) {
        this.fuel = Math.min(this.fuel + amount, MAX_FUEL);
        // Updated math here as well for instant visual update
        int newLevel = (this.fuel == 0) ? 0 : Math.min(8, (this.fuel - 1) / 2000 + 1);

        if (this.world != null && !this.world.isClient) {
            BlockState state = this.getCachedState();
            if (state.get(FurnaceBlock.FUEL_LEVEL) != newLevel) {
                this.world.setBlockState(pos, state.with(FurnaceBlock.FUEL_LEVEL, newLevel), 3);
            }
        }
        markDirty();
    }

    /* ---------- Sync & NBT ---------- */

    private void syncToClient() {
        markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    private void ttll$tryFurnaceSpread() {
        if (this.world == null) return;

        Direction facing = this.getCachedState().get(FurnaceBlock.FACING);
        BlockPos frontPos = this.pos.offset(facing);
        Direction side = facing.rotateYClockwise();

        // Define the 6 specific danger points
        java.util.List<BlockPos> targets = new java.util.ArrayList<>();

        // 1-5: The Cross shape 1 block away
        targets.add(frontPos);              // Center
        targets.add(frontPos.up());         // Top
        targets.add(frontPos.down());       // Bottom
        targets.add(frontPos.offset(side)); // Right
        targets.add(frontPos.offset(side.getOpposite())); // Left

        // 6: The Reach block (2 blocks away, center only)
        targets.add(frontPos.offset(facing));

        // Pick one and try to ignite
        BlockPos target = targets.get(world.random.nextInt(targets.size()));

        if (world.getBlockState(target).isAir()) {
            if (this.ttll$isPosBurnable(target)) {
                world.setBlockState(target, net.minecraft.block.AbstractFireBlock.getState(world, target));
                world.playSound(null, target, net.minecraft.sound.SoundEvents.BLOCK_LAVA_EXTINGUISH,
                        net.minecraft.sound.SoundCategory.BLOCKS, 0.3f, 1.2f);
            }
        }
    }

    private boolean ttll$isPosBurnable(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (world.getBlockState(pos.offset(dir)).isBurnable()) {
                return true;
            }
        }
        return false;
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