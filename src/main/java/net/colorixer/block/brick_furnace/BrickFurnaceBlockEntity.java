package net.colorixer.block.brick_furnace;

import net.colorixer.block.ModBlockEntities;
import net.colorixer.item.ModItems; // Ensure your mod items are imported correctly.
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.inventory.Inventories;
import java.util.ArrayList;
import java.util.List;

public class BrickFurnaceBlockEntity extends BlockEntity {
    public static final int MAX_BURN_TIME = 9000;
    public static final int FUEL_VALUE = 1000;
    private int burnTimeRemaining = 0;
    // This field tracks progress toward the current recipe.
    public int recipeCookTime = 0;

    private static final Item[] VALID_FUELS = {Items.COAL, Items.CHARCOAL};

    // Inventory: slot 0 = cast; slots 1–4 = ingredients.
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(5, ItemStack.EMPTY);
    // Fuel inventory remains the same.
    private final DefaultedList<ItemStack> fuelItems = DefaultedList.ofSize(9, ItemStack.EMPTY);

    public BrickFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRICK_FURNACE, pos, state);
    }



    public boolean addFuel(ItemStack stack, PlayerEntity player) {
        if (!isValidFuel(stack.getItem())) return false;
        if (burnTimeRemaining > MAX_BURN_TIME - FUEL_VALUE) return false;
        if (!player.isCreative()) {
            stack.decrement(1);
        }
        burnTimeRemaining += FUEL_VALUE;
        for (int i = 0; i < fuelItems.size(); i++) {
            if (fuelItems.get(i).isEmpty()) {
                fuelItems.set(i, new ItemStack(stack.getItem()));
                break;
            }
        }
        markDirtyAndUpdate();
        return true;
    }

    private boolean isValidFuel(Item item) {
        return item == Items.COAL || item == Items.CHARCOAL;
    }

    public boolean isBurning() {
        return burnTimeRemaining > 0 && getCachedState().get(BrickFurnaceBlock.LIT);
    }

    public int getBurnTime() {
        return burnTimeRemaining;
    }

    public void setBurnTime(int burnTime) {
        this.burnTimeRemaining = burnTime;
        markDirtyAndUpdate();
    }


    private void markDirtyAndUpdate() {
        this.markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public DefaultedList<ItemStack> getInventory() {
        return items;
    }

    public DefaultedList<ItemStack> getFuelInventory() {
        return fuelItems;
    }

    public ItemStack getCastItem() {
        return items.get(0);
    }

    public void setCastItem(ItemStack stack) {
        items.set(0, stack);
        markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        Inventories.writeNbt(nbt, items, true, registries);
        nbt.putInt("BurnTime", burnTimeRemaining);
        NbtCompound fuelNbt = new NbtCompound();
        Inventories.writeNbt(fuelNbt, fuelItems, true, registries);
        nbt.put("FuelItems", fuelNbt);
        nbt.putInt("RecipeCookTime", recipeCookTime);
        return nbt;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("BurnTime", burnTimeRemaining);
        Inventories.writeNbt(nbt, items, true, registries);
        NbtCompound fuelNbt = new NbtCompound();
        Inventories.writeNbt(fuelNbt, fuelItems, true, registries);
        nbt.put("FuelItems", fuelNbt);
        nbt.putInt("RecipeCookTime", recipeCookTime);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        burnTimeRemaining = nbt.getInt("BurnTime");
        items.clear();
        Inventories.readNbt(nbt, items, registries);
        fuelItems.clear();
        if (nbt.contains("FuelItems", 10)) {
            NbtCompound fuelNbt = nbt.getCompound("FuelItems");
            Inventories.readNbt(fuelNbt, fuelItems, registries);
        }
        recipeCookTime = nbt.getInt("RecipeCookTime");
        if (world != null) {
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, 3);
        }
    }

    // --- Hardcoded Recipe System ---
    // We define a static inner class to hold recipe data.
    public static class HardcodedRecipe {
        public final Item cast;
        public final Item ingredient;
        public final int ingredientCount;
        public final Item result;
        public final int cookTime;
        public final int experience; // XP rewarded

        public HardcodedRecipe(Item cast, Item ingredient, int ingredientCount, Item result, int cookTime, int experience) {
            this.cast = cast;
            this.ingredient = ingredient;
            this.ingredientCount = ingredientCount;
            this.result = result;
            this.cookTime = cookTime;
            this.experience = experience;
        }

        public boolean matches(DefaultedList<ItemStack> inventory) {
            // Assumes slot 0 holds the cast and slots 1–4 contain ingredients.
            if (inventory.get(0).isEmpty() || inventory.get(0).getItem() != cast)
                return false;
            int total = 0;
            for (int i = 1; i < inventory.size(); i++) {
                ItemStack stack = inventory.get(i);
                if (!stack.isEmpty()) {
                    if (stack.getItem() != ingredient) {
                        return false;
                    }
                    total += stack.getCount();
                }
            }
            return total >= ingredientCount;
        }

        public void consumeIngredients(DefaultedList<ItemStack> inventory) {
            int remaining = ingredientCount;
            for (int i = inventory.size() - 1; i >= 1 && remaining > 0; i--) {
                ItemStack stack = inventory.get(i);
                if (!stack.isEmpty() && stack.getItem() == ingredient) {
                    int count = stack.getCount();
                    if (count <= remaining) {
                        remaining -= count;
                        inventory.set(i, ItemStack.EMPTY);
                    } else {
                        stack.decrement(remaining);
                        remaining = 0;
                    }
                }
            }
        }
    }

    public static final List<HardcodedRecipe> HARDCODED_RECIPES = new ArrayList<>();

    static {

        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.NUGGET_CAST,
                ModItems.IRON_DUST,
                4,
                ModItems.IRON_NUGGET_CAST,
                150,
                3
        ));

        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.PICKAXE_CAST,
                Items.IRON_INGOT,
                3,
                ModItems.IRON_PICKAXE_CAST,
                200,
                5
        ));



        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.NUGGET_CAST,
                ModItems.GOLDEN_DUST,
                4,
                ModItems.GOLDEN_NUGGET_CAST,
                150,
                3
        ));
    }

    public static void tick(World world, BlockPos pos, BlockState state, BrickFurnaceBlockEntity entity) {
        if (world.isClient) {
            if (entity.isBurning() && world.random.nextInt(40) == 0) {
                world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 2.0f, 1.0f, true);
            }
            return;
        }

        boolean stateChanged = false;
        if (state.get(BrickFurnaceBlock.LIT)) {
            if (entity.burnTimeRemaining > 0) {
                entity.burnTimeRemaining--;
                if (entity.burnTimeRemaining <= 0) {
                    BlockState newState = state.with(BrickFurnaceBlock.LIT, false)
                            .with(BrickFurnaceBlock.USED, true);
                    world.setBlockState(pos, newState);
                    world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
                            SoundCategory.BLOCKS, 1.0f, 1.0f);
                    stateChanged = true;
                }
            } else {
                BlockState newState = state.with(BrickFurnaceBlock.LIT, false)
                        .with(BrickFurnaceBlock.USED, true);
                world.setBlockState(pos, newState);
                stateChanged = true;
            }
        }

        int activeSlots = 0;
        if (entity.burnTimeRemaining > 0) {
            activeSlots = Math.min((entity.burnTimeRemaining / FUEL_VALUE) + 1, entity.fuelItems.size());
        }
        boolean fuelChanged = false;
        for (int i = activeSlots; i < entity.fuelItems.size(); i++) {
            if (!entity.fuelItems.get(i).isEmpty()) {
                entity.fuelItems.set(i, ItemStack.EMPTY);
                fuelChanged = true;
            }
        }
        if (stateChanged || fuelChanged) {
            entity.markDirty();
            world.updateListeners(pos, state, state, 3);
        }

        boolean foundMatch = false;

        for (HardcodedRecipe recipe : HARDCODED_RECIPES) {
            if (recipe.matches(entity.getInventory())) {
                foundMatch = true;

                if (entity.isBurning()) {
                    entity.recipeCookTime++;
                } else {
                    entity.recipeCookTime = Math.max(0, entity.recipeCookTime - 5);
                }

                if (entity.recipeCookTime >= recipe.cookTime) {
                    recipe.consumeIngredients(entity.getInventory());
                    entity.setCastItem(new ItemStack(recipe.result));

                    ExperienceOrbEntity xpOrb = new ExperienceOrbEntity(world,
                            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, recipe.experience);
                    world.spawnEntity(xpOrb);

                    entity.recipeCookTime = 0;
                    entity.markDirty();
                    world.updateListeners(pos, state, state, 3);
                }

                // Stop after first matched recipe is processed
                break;
            }
        }

// If no recipe matched, slowly reduce the timer
        if (!foundMatch) {
            entity.recipeCookTime = Math.max(0, entity.recipeCookTime - 5);
        }

        // --- End Hardcoded Recipe Processing ---
    }

    public void dropAllContents() {
        if (world != null && !world.isClient) {
            // Drop items from main inventory (cast + ingredients)
            for (ItemStack stack : new ArrayList<>(items)) {
                if (!stack.isEmpty()) {
                    world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy()));
                }
            }
            // Drop items from fuel inventory
            for (ItemStack stack : new ArrayList<>(fuelItems)) {
                if (!stack.isEmpty()) {
                    world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy()));
                }
            }
            // Clear inventories
            items.clear();
            fuelItems.clear();
            markDirty();
        }
    }
}
