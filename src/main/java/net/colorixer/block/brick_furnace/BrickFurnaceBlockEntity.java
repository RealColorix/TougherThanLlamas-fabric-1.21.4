package net.colorixer.block.brick_furnace;

import net.colorixer.block.ModBlockEntities;
import net.colorixer.block.ModBlocks;
import net.colorixer.item.ModItems;
import net.minecraft.block.Block;
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
import net.minecraft.recipe.Ingredient;
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
    public static final int MAX_BURN_TIME = 16200;  // Set to 16200
    public static final int FUEL_VALUE = 1800;        // Set to 1800
    private int burnTimeRemaining = 0;
    public int recipeCookTime = 0;

    private static final Item[] VALID_FUELS = {Items.COAL, Items.CHARCOAL};

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(5, ItemStack.EMPTY);
    private final DefaultedList<ItemStack> fuelItems = DefaultedList.ofSize(9, ItemStack.EMPTY);

    // Tracking cooking progress for each ingredient slot; slot 0 is reserved for the cast.
    private final int[] cookingProgress = new int[5];

    public BrickFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRICK_FURNACE_BLOCK_ENTITY, pos, state);
    }

    public boolean addFuel(ItemStack stack, PlayerEntity player) {
        if (!isValidFuel(stack.getItem())) return false;
        if (burnTimeRemaining > MAX_BURN_TIME - FUEL_VALUE) return false;
        if (!player.isCreative()) {
            stack.decrement(1);
        }
        burnTimeRemaining += FUEL_VALUE; // Add the new fuel value
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
        // Save individual cooking progress values for slots 1 to 4.
        for (int i = 1; i < cookingProgress.length; i++) {
            nbt.putInt("CookingProgress_" + i, cookingProgress[i]);
        }
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
        for (int i = 1; i < cookingProgress.length; i++) {
            nbt.putInt("CookingProgress_" + i, cookingProgress[i]);
        }
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
        for (int i = 1; i < cookingProgress.length; i++) {
            cookingProgress[i] = nbt.getInt("CookingProgress_" + i);
        }
        if (world != null) {
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, 3);
        }
    }

    // --- Hardcoded Recipe (existing) ---
    public static class HardcodedRecipe {
        public final Item cast;
        public final Item ingredient;
        public final int ingredientCount;
        public final Item result;
        public final int cookTime;
        public final int experience;

        public HardcodedRecipe(Item cast, Item ingredient, int ingredientCount, Item result, int cookTime, int experience) {
            this.cast = cast;
            this.ingredient = ingredient;
            this.ingredientCount = ingredientCount;
            this.result = result;
            this.cookTime = cookTime;
            this.experience = experience;
        }

        public boolean matches(DefaultedList<ItemStack> inventory) {
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


    public static boolean isValidCast(Item item) {
        for (HardcodedRecipe recipe : HARDCODED_RECIPES) {
            if (recipe.cast == item) return true;
        }
        for (CookingRecipe recipe : COOKING_RECIPES) {
            if (recipe.requiredCast == item) return true;
        }
        return false;
    }

    public static boolean isValidIngredient(ItemStack ingredient, ItemStack cast) {
        // Ensure the cast matches the recipe
        for (HardcodedRecipe recipe : HARDCODED_RECIPES) {
            if (recipe.cast == cast.getItem() && ingredient.getItem() == recipe.ingredient) return true;
        }
        for (CookingRecipe recipe : COOKING_RECIPES) {
            if (recipe.requiredCast == cast.getItem() && recipe.ingredient.test(ingredient)) return true;
        }
        return false;
    }



    public static final List<HardcodedRecipe> HARDCODED_RECIPES = new ArrayList<>();

    static {
        // MOLDS
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.FLAT_MOLD,
                null,
                0,
                ModItems.FLAT_CAST,
                3000,
                0
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.INGOT_MOLD,
                null,
                0,
                ModItems.INGOT_CAST,
                3000,
                0
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.NUGGET_MOLD,
                null,
                0,
                ModItems.NUGGET_CAST,
                3000,
                0
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.SWORD_MOLD,
                null,
                0,
                ModItems.SWORD_CAST,
                3000,
                0
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.PICKAXE_MOLD,
                null,
                0,
                ModItems.PICKAXE_CAST,
                3000,
                0
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.AXE_MOLD,
                null,
                0,
                ModItems.AXE_CAST,
                3000,
                0
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.SHOVEL_MOLD,
                null,
                0,
                ModItems.SHOVEL_CAST,
                3000,
                0
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.HOE_MOLD,
                null,
                0,
                ModItems.HOE_CAST,
                3000,
                0
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.CHISEL_MOLD,
                null,
                0,
                ModItems.CHISEL_CAST,
                3000,
                0
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.BUCKET_MOLD,
                null,
                0,
                ModItems.BUCKET_CAST,
                3000,
                0
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.PLATE_MOLD,
                null,
                0,
                ModItems.PLATE_CAST,
                3000,
                0
        ));
        // IRON recipes
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.NUGGET_CAST,
                ModItems.IRON_DUST,
                4,
                ModItems.IRON_NUGGET_CAST,
                3500,
                2
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.NUGGET_CAST,
                Items.RAW_IRON,
                2,
                ModItems.IRON_NUGGET_CAST,
                3500,
                2
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.INGOT_CAST,
                Items.IRON_NUGGET,
                4,
                ModItems.IRON_INGOT_CAST,
                10700,
                7
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.PLATE_CAST,
                Items.IRON_INGOT,
                2,
                ModItems.IRON_PLATE_CAST,
                8300,
                7
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.SWORD_CAST,
                Items.IRON_INGOT,
                2,
                ModItems.IRON_SWORD_CAST,
                7000,
                9
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.PICKAXE_CAST,
                Items.IRON_INGOT,
                3,
                ModItems.IRON_PICKAXE_CAST,
                8900,
                12
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.AXE_CAST,
                Items.IRON_INGOT,
                2,
                ModItems.IRON_AXE_CAST,
                7000,
                10
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.SHOVEL_CAST,
                Items.IRON_INGOT,
                1,
                ModItems.IRON_SHOVEL_CAST,
                5000,
                7
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.SHOVEL_CAST,
                Items.IRON_NUGGET,
                4,
                ModItems.IRON_SHOVEL_CAST,
                5000,
                7
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.HOE_CAST,
                Items.IRON_INGOT,
                1,
                ModItems.IRON_HOE_CAST,
                5000,
                6
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.HOE_CAST,
                Items.IRON_NUGGET,
                4,
                ModItems.IRON_HOE_CAST,
                5000,
                6
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.CHISEL_CAST,
                Items.IRON_INGOT,
                1,
                ModItems.IRON_CHISEL_CAST,
                5000,
                5
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.CHISEL_CAST,
                Items.IRON_NUGGET,
                4,
                ModItems.IRON_CHISEL_CAST,
                5000,
                5
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.BUCKET_CAST,
                Items.IRON_INGOT,
                1,
                ModItems.IRON_BUCKET_CAST,
                4000,
                4
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.BUCKET_CAST,
                Items.IRON_NUGGET,
                4,
                ModItems.IRON_BUCKET_CAST,
                4000,
                4
        ));
        // GOLD recipes
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.NUGGET_CAST,
                ModItems.GOLD_DUST,
                4,
                ModItems.GOLD_NUGGET_CAST,
                3000,
                4
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.NUGGET_CAST,
                Items.RAW_GOLD,
                2,
                ModItems.GOLD_NUGGET_CAST,
                3200,
                4
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.INGOT_CAST,
                Items.GOLD_NUGGET,
                4,
                ModItems.GOLD_INGOT_CAST,
                7100,
                10
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.SWORD_CAST,
                Items.GOLD_INGOT,
                2,
                ModItems.GOLDEN_SWORD_CAST,
                5500,
                9
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.PICKAXE_CAST,
                Items.GOLD_INGOT,
                3,
                ModItems.GOLDEN_PICKAXE_CAST,
                7500,
                12
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.AXE_CAST,
                Items.GOLD_INGOT,
                2,
                ModItems.GOLDEN_AXE_CAST,
                5500,
                10
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.SHOVEL_CAST,
                Items.GOLD_INGOT,
                1,
                ModItems.GOLDEN_SHOVEL_CAST,
                3500,
                7
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.SHOVEL_CAST,
                Items.GOLD_NUGGET,
                4,
                ModItems.GOLDEN_SHOVEL_CAST,
                3500,
                7
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.HOE_CAST,
                Items.GOLD_INGOT,
                1,
                ModItems.GOLDEN_HOE_CAST,
                3500,
                6
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.HOE_CAST,
                Items.GOLD_NUGGET,
                4,
                ModItems.GOLDEN_HOE_CAST,
                3500,
                6
        ));
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.PLATE_CAST,
                Items.GOLD_INGOT,
                2,
                ModItems.GOLD_PLATE_CAST,
                6400,
                8
        ));
        // COPPER recipe
        HARDCODED_RECIPES.add(new HardcodedRecipe(
                ModItems.PLATE_CAST,
                Items.COPPER_INGOT,
                2,
                ModItems.COPPER_PLATE_CAST,
                8400,
                4
        ));
    }

    // --- New Cooking Recipe (per-slot) with cast requirement ---
    public static class CookingRecipe {
        public final Item requiredCast;
        public final Ingredient ingredient;  // Changed from Item to Ingredient
        public final ItemStack result;       // Changed from Item to ItemStack
        public final int cookTime;
        public final int experience;

        public CookingRecipe(Item requiredCast, Item ingredient, Item result, int cookTime, int experience) {
            this(requiredCast, Ingredient.ofItems(ingredient), new ItemStack(result), cookTime, experience);
        }

        public CookingRecipe(Item requiredCast, Block ingredient, Item result, int cookTime, int experience) {
            this(requiredCast, Ingredient.ofItems(ingredient.asItem()), new ItemStack(result), cookTime, experience);
        }

        public CookingRecipe(Item requiredCast, Block ingredient, Block result, int cookTime, int experience) {
            this(requiredCast, Ingredient.ofItems(ingredient.asItem()), new ItemStack(result.asItem()), cookTime, experience);
        }

        private CookingRecipe(Item requiredCast, Ingredient ingredient, ItemStack result, int cookTime, int experience) {
            this.requiredCast = requiredCast;
            this.ingredient = ingredient;
            this.result = result;
            this.cookTime = cookTime;
            this.experience = experience;
        }

        public boolean matches(ItemStack castStack, ItemStack ingredientStack) {
            return !castStack.isEmpty() && castStack.getItem() == requiredCast &&
                    ingredient.test(ingredientStack);
        }
    }




    public static final List<CookingRecipe> COOKING_RECIPES = new ArrayList<>();

    static {

        COOKING_RECIPES.add(new CookingRecipe(ModItems.FLAT_CAST, ModItems.BIRCH_FIREWOOD, Items.CHARCOAL, 1500, 0));
        COOKING_RECIPES.add(new CookingRecipe(ModItems.FLAT_CAST, ModItems.OAK_FIREWOOD, Items.CHARCOAL, 1500, 0));
        COOKING_RECIPES.add(new CookingRecipe(ModItems.FLAT_CAST, ModItems.SPRUCE_FIREWOOD, Items.CHARCOAL, 1500, 0));
        COOKING_RECIPES.add(new CookingRecipe(ModItems.FLAT_CAST, ModItems.JUNGLE_FIREWOOD, Items.CHARCOAL, 1500, 0));
        COOKING_RECIPES.add(new CookingRecipe(ModItems.FLAT_CAST, Items.BEEF, Items.COOKED_BEEF, 1500, 1));
        COOKING_RECIPES.add(new CookingRecipe(ModItems.FLAT_CAST, Items.PORKCHOP, Items.COOKED_PORKCHOP, 1500, 1));
        COOKING_RECIPES.add(new CookingRecipe(ModItems.FLAT_CAST, Items.SALMON, Items.COOKED_SALMON, 1500, 1));
        COOKING_RECIPES.add(new CookingRecipe(ModItems.FLAT_CAST, Items.COD, Items.COOKED_COD, 1500, 1));
        COOKING_RECIPES.add(new CookingRecipe(ModItems.FLAT_CAST, Items.RABBIT, Items.COOKED_RABBIT, 1500, 1));
        COOKING_RECIPES.add(new CookingRecipe(ModItems.FLAT_CAST, Items.CHICKEN, Items.COOKED_CHICKEN, 1500, 1));
        COOKING_RECIPES.add(new CookingRecipe(ModItems.FLAT_CAST, Items.MUTTON, Items.COOKED_MUTTON, 1500, 1));
        COOKING_RECIPES.add(new CookingRecipe(ModItems.FLAT_CAST, ModBlocks.WET_BRICK, ModBlocks.DRIED_BRICK, 4500, 0));

    }

    public static void tick(World world, BlockPos pos, BlockState state, BrickFurnaceBlockEntity entity) {
        if (world.isClient) {
            if (entity.isBurning() && world.random.nextInt(40) == 0) {
                world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 4.0f, 1.0f, true);
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

        boolean foundMatch = false;

        // Process hardcoded recipes first
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

                    if (recipe.experience > 0) {
                        ExperienceOrbEntity xpOrb = new ExperienceOrbEntity(world,
                                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, recipe.experience);
                        world.spawnEntity(xpOrb);
                    }

                    entity.recipeCookTime = 0;
                    entity.markDirty();
                    world.updateListeners(pos, state, state, 3);
                    break;
                }
            }
        }

        // If no hardcoded recipe matched, decrease the global timer.
        if (!foundMatch) {
            entity.recipeCookTime = Math.max(0, entity.recipeCookTime - 5);
        }

        // Process fuel inventory (unchanged)
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
        if (fuelChanged) {
            entity.markDirtyAndUpdate();
        }
        if (stateChanged) {
            entity.markDirtyAndUpdate();
        }

        // Process cooking recipes on each ingredient slot (slots 1 to 4)
        // Now the recipe will only apply if the cast in slot 0 matches the recipe requirement.
        // Process cooking recipes on each ingredient slot (slots 1 to 4)
        ItemStack castStack = entity.getCastItem();
        for (int slot = 1; slot < entity.items.size(); slot++) {
            ItemStack stack = entity.items.get(slot);
            if (!stack.isEmpty()) {
                for (CookingRecipe cRecipe : COOKING_RECIPES) {
                    if (cRecipe.matches(castStack, stack)) {
                        if (entity.isBurning()) {
                            entity.cookingProgress[slot]++;
                        } else {
                            entity.cookingProgress[slot] = Math.max(0, entity.cookingProgress[slot] - 1);
                        }
                        if (entity.cookingProgress[slot] >= cRecipe.cookTime) {
                            // Create new stack preserving count and NBT
                            ItemStack resultStack = cRecipe.result.copy();
                            resultStack.setCount(stack.getCount());
                            entity.items.set(slot, resultStack);

                            if (cRecipe.experience > 0) {
                                ExperienceOrbEntity xpOrb = new ExperienceOrbEntity(world,
                                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, cRecipe.experience);
                                world.spawnEntity(xpOrb);
                            }

                            entity.cookingProgress[slot] = 0;
                            entity.markDirty();
                            world.updateListeners(pos, state, state, 3);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void dropAllContents() {
        if (world != null && !world.isClient) {
            // Drop items.java from main inventory (cast + ingredients)
            for (ItemStack stack : new ArrayList<>(items)) {
                if (!stack.isEmpty()) {
                    world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack.copy()));
                }
            }
            // Drop items.java from fuel inventory
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
