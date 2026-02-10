package net.colorixer.block.campfire;

import net.colorixer.block.ModBlockEntities;
import net.colorixer.item.ModItems;
import net.colorixer.util.ExhaustionHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class CampfireBlockEntity extends BlockEntity {
    private ItemStack inventory = ItemStack.EMPTY;
    private float fuel = 4000.0f;
    private float pendingFuel = 0.0f;
    private int cookTime = 0;
    private int burnTimeCounter = 0;
    private int litTimer = 0;
    private float rainPenalty = 0.0f;

    public static final int MAX_FUEL = 8000;
    private static final int GRACE_PERIOD_TICKS = 200;

    public CampfireBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CAMPFIREBLOCKENTITY, pos, state);
    }

    public void tick() {
        if (this.world == null || this.world.isClient) return;

        BlockState state = this.getCachedState();
        boolean isLit = state.get(CampfireBlock.LIT);
        int stage = state.get(CampfireBlock.STAGE);

        // 1. WATERLOGGED CHECK
        if (state.get(CampfireBlock.WATERLOGGED) && isLit) {
            extinguish(true);
            return;
        }

        // 2. FUEL PROCESSING
        if (pendingFuel > 0) {
            float transferAmount = 10.0f;
            if (pendingFuel < transferAmount) transferAmount = pendingFuel;
            if (this.fuel < MAX_FUEL) {
                this.fuel = Math.min(this.fuel + transferAmount, (float)MAX_FUEL);
            }
            this.pendingFuel -= transferAmount;
            this.markDirty();
        }

        // 3. BURNING & STAGE LOGIC
        // 3. BURNING & STAGE LOGIC
        if (isLit) {
            if (this.fuel > 0) {
                // Determine burn rate
                float burnRate = 1.0f;

                // RAIN LOGIC: If raining and exposed to sky
                if (world.isRaining() && world.isSkyVisible(pos.up())) {
                    this.rainPenalty += 0.02f; // How fast the penalty grows (tweak this!)
                    burnRate += this.rainPenalty;
                } else {
                    this.rainPenalty = 0.0f; // Reset penalty when rain stops or covered
                }

                this.fuel -= burnRate;

                int currentStage = state.get(CampfireBlock.STAGE);
                int nextStage = currentStage;

                if (litTimer < GRACE_PERIOD_TICKS) {
                    litTimer++;
                    nextStage = 1;
                } else {
                    if (fuel > 4000) nextStage = 3;
                    else if (fuel >= 2000) nextStage = 2;
                    else nextStage = 1;
                }

                if (nextStage != currentStage) {
                    world.setBlockState(pos, state.with(CampfireBlock.STAGE, nextStage), 3);
                }
            } else {
                this.rainPenalty = 0.0f;
                extinguish(false); // Natural burn out
            }
        }

        // 4. COOKING LOGIC
        if (!this.inventory.isEmpty()) {
            CampfireRecipe recipe = CampfireRecipes.get(this.inventory);
            if (recipe != null) {
                boolean finished = false;

                // If lit and in stage 2 or 3, cook normally
                if (isLit && (stage == 2 || stage == 3)) {
                    this.cookTime++;
                    if (this.cookTime >= recipe.cookTime()) {
                        this.inventory = recipe.output().copy();
                        finished = true;
                    }

                    if (!finished && stage == 3) {
                        this.burnTimeCounter++;
                        if (this.burnTimeCounter >= recipe.burnTime()) {
                            this.inventory = recipe.burnOutput().copy();
                            finished = true;
                        }
                    }
                }
                // SHOVEL LOGIC: If unlit and in Stage 4, decrease cook time
                else if (!isLit && stage == 4) {
                    if (this.cookTime > 0) {
                        this.cookTime--; // Cooling down
                    }
                }

                if (finished) {
                    resetTimers();
                    syncToClient();
                }
            }
        } else if (this.inventory.isEmpty()) {
            resetTimers();
        }
        this.markDirty();
    }

    public void extinguish(boolean wasWater) {
        if (this.world == null) return;
        // Waterlogged or burnout goes to Stage 5 (Fuel 0)
        this.world.setBlockState(pos, getCachedState().with(CampfireBlock.LIT, false).with(CampfireBlock.STAGE, 5), 3);
        this.world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5f, 1.0f);
        this.litTimer = 0;
        this.fuel = 0;
        resetTimers();
        syncToClient();
    }

    public void ignite() {
        if (this.world == null || getCachedState().get(CampfireBlock.WATERLOGGED)) return;
        this.litTimer = 0;
        world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5f, 1.0f);
        world.setBlockState(pos, getCachedState().with(CampfireBlock.LIT, true).with(CampfireBlock.STAGE, 1), 3);
        syncToClient();
    }

    private void resetTimers() {
        this.cookTime = 0;
        this.burnTimeCounter = 0;
    }

    private static final Map<Item, Float> FUEL_VALUES = Util.make(new HashMap<>(), map -> {
        map.put(Items.STICK, 1000.0f);
        map.put(ModItems.BRANCH, 2000.0f);
    });

    public ActionResult onRightClick(PlayerEntity player, Hand hand) {
        if (world.isClient) return ActionResult.SUCCESS;

        ItemStack stack = player.getStackInHand(hand);
        BlockState state = getCachedState();
        Item item = stack.getItem();



        // 1. SHOVEL / HOE EXTINGUISH (only if Lit)
        if (state.get(CampfireBlock.LIT) &&
                (item instanceof ShovelItem || item instanceof HoeItem || item instanceof net.colorixer.item.items.HoeItem)) {
            world.setBlockState(pos, state.with(CampfireBlock.LIT, false).with(CampfireBlock.STAGE, 4), 3);
            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5f, 1.0f);
            if (!player.getAbilities().creativeMode) stack.damage(1, player, EquipmentSlot.MAINHAND);
            syncToClient();
            return ActionResult.SUCCESS;
        }

        // 2. WATER BUCKET EXTINGUISH + WATERLOGGED
        if (stack.isOf(Items.WATER_BUCKET)) {
            if (state.get(CampfireBlock.LIT) && !state.get(CampfireBlock.WATERLOGGED)) {
                world.setBlockState(pos, state.with(CampfireBlock.LIT, false)
                        .with(CampfireBlock.STAGE, 5)
                        .with(CampfireBlock.WATERLOGGED, true), 3);
                extinguish(true); // resets fuel and timers
                if (!player.getAbilities().creativeMode) {
                    player.setStackInHand(hand, new ItemStack(Items.BUCKET));
                }
                syncToClient();
                return ActionResult.SUCCESS;
            } else if (!state.get(CampfireBlock.LIT) && !state.get(CampfireBlock.WATERLOGGED)) {
                world.setBlockState(pos, state.with(CampfireBlock.WATERLOGGED, true), 3);

                if (!player.getAbilities().creativeMode) {
                    player.setStackInHand(hand, new ItemStack(Items.BUCKET));
                }
                syncToClient();
                return ActionResult.SUCCESS;
            }
        }

        // PICK UP WATER WITH EMPTY BUCKET
        if (stack.isOf(Items.BUCKET) && state.get(CampfireBlock.WATERLOGGED)) {
            world.setBlockState(pos, state.with(CampfireBlock.WATERLOGGED, false), 3);
            if (!player.getAbilities().creativeMode) {
                player.setStackInHand(hand, new ItemStack(Items.WATER_BUCKET));
            }
            world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
            syncToClient();
            return ActionResult.SUCCESS;
        }


        // 3. STICK LOGIC
        if (!state.get(CampfireBlock.STICK)) {
            // Add the pointy stick
            if (item == ModItems.POINTY_STICK) {
                world.setBlockState(pos, state.with(CampfireBlock.STICK, true), 3);
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0f, 1.2f);
                syncToClient();
                return ActionResult.SUCCESS;
            }
        }
        if (state.get(CampfireBlock.STICK)) {
            if (stack.isEmpty() && inventory.isEmpty()) {
                world.setBlockState(pos, state.with(CampfireBlock.STICK, false), 3);
                player.getInventory().offerOrDrop(new ItemStack(ModItems.POINTY_STICK));
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0f, 1.0f);
                syncToClient();
                return ActionResult.SUCCESS;
            }
        }

        // 4. PLACE FOOD (only if stick = true)
        if (state.get(CampfireBlock.STICK) && !stack.isEmpty()) {
            CampfireRecipe recipe = CampfireRecipes.get(stack);
            if (recipe != null && inventory.isEmpty()) {
                inventory = stack.split(1);
                resetTimers();
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 0.8f, 1.2f);
                syncToClient();
                return ActionResult.SUCCESS;
            }
        }

        // 5. LIGHTING (Stage 0 or 4, fuel > 0)
        if (!state.get(CampfireBlock.LIT) && (state.get(CampfireBlock.STAGE) == 0 || state.get(CampfireBlock.STAGE) == 4) && fuel > 0) {
            // Firestarter
            if (item instanceof net.colorixer.item.items.FireStarterItem fireStarter) {
                if (!world.isClient) {
                    player.getHungerManager().addExhaustion(0.1F);
                    ExhaustionHelper.triggerJitter(5);
                }
                if (world.random.nextDouble() < fireStarter.chance) ignite();
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 0.5f, 1.0f);
                if (!player.getAbilities().creativeMode) stack.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                syncToClient();
                return ActionResult.SUCCESS;
            }

            // Flint & steel
            if (stack.isOf(Items.FLINT_AND_STEEL)) {
                player.getItemCooldownManager().set(stack, 10);
                if (world.random.nextInt(5) == 0) ignite();
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 0.5f, 1.0f);
                if (!player.getAbilities().creativeMode) stack.damage(1, player, EquipmentSlot.MAINHAND);
                syncToClient();
                return ActionResult.SUCCESS;
            }

            // Fire charge / torch
            if (stack.isOf(Items.FIRE_CHARGE) || stack.isOf(net.colorixer.block.ModBlocks.BURNING_CRUDE_TORCH.asItem())) {
                ignite();
                world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                if (stack.isOf(Items.FIRE_CHARGE) && !player.getAbilities().creativeMode) stack.decrement(1);
                syncToClient();
                return ActionResult.SUCCESS;
            }
        }

        // 6. ADD FUEL (only if lit)
        if (FUEL_VALUES.containsKey(item) && state.get(CampfireBlock.LIT)) {
            if (fuel + pendingFuel < MAX_FUEL) {
                pendingFuel += FUEL_VALUES.get(item);
                if (!player.getAbilities().creativeMode) stack.decrement(1);
                if (state.get(CampfireBlock.STAGE) >= 4) world.setBlockState(pos, state.with(CampfireBlock.STAGE, 0), 3);
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0f, 1.5f);
                syncToClient();
                return ActionResult.SUCCESS;
            }
        }

        if (!inventory.isEmpty()) {
            // Give the item to the player
            if (!player.getInventory().insertStack(inventory.copy())) {
                player.dropItem(inventory.copy(), false); // drop on ground if inventory full
            }
            inventory = ItemStack.EMPTY;
            resetTimers();
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0f, 1.0f);
            syncToClient();
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }



    private void syncToClient() {
        this.markDirty();
        if (this.world != null) {
            // The '3' flag is a combination of NOTIFY_NEIGHBORS (1) and NOTIFY_LISTENERS (2)
            // NOTIFY_LISTENERS is what triggers the renderer/Chunk rebuild
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    public float getFuel() { return this.fuel; }
    public ItemStack getInventory() { return this.inventory; }

    @Override public Packet<ClientPlayPacketListener> toUpdatePacket() { return BlockEntityUpdateS2CPacket.create(this); }
    @Override public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) { return createNbt(registryLookup); }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (!inventory.isEmpty()) nbt.put("Inventory", inventory.toNbt(registryLookup));
        nbt.putFloat("Fuel", fuel);
        nbt.putFloat("PendingFuel", pendingFuel);
        nbt.putInt("LitTimer", litTimer);
        nbt.putInt("CookTime", cookTime);
        nbt.putInt("BurnTimeCounter", burnTimeCounter);
        nbt.putFloat("RainPenalty", rainPenalty);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("Inventory", 10)) {
            this.inventory = ItemStack.fromNbt(registryLookup, nbt.getCompound("Inventory")).orElse(ItemStack.EMPTY);
        } else {
            // MANDATORY: Clear the local client item so the renderer stops drawing it
            this.inventory = ItemStack.EMPTY;
        }
        this.fuel = nbt.getFloat("Fuel");
        this.pendingFuel = nbt.getFloat("PendingFuel");
        this.litTimer = nbt.getInt("LitTimer");
        this.cookTime = nbt.getInt("CookTime");
        this.burnTimeCounter = nbt.getInt("BurnTimeCounter");
        this.rainPenalty = nbt.getFloat("RainPenalty");
    }
}