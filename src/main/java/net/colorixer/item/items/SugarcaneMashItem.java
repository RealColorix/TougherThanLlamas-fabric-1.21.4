package net.colorixer.item.items;

import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Objects;

public class SugarcaneMashItem extends Item {

    private final Item dropItem;
    private final int dropQuantity;
    private final SoundEvent washSound;

    public SugarcaneMashItem(Settings settings, Item dropItem, int dropQuantity, SoundEvent washSound) {
        super(
                settings
                        .component(
                                DataComponentTypes.FOOD,
                                new FoodComponent(0, 0.0F, true)
                        )
                        .component(
                                DataComponentTypes.CONSUMABLE,
                                ConsumableComponent.builder()
                                        .consumeSeconds(10.0f) // Exactly 10 seconds
                                        .useAction(UseAction.EAT) // The exact same animation
                                        .sound(Registries.SOUND_EVENT.getEntry(washSound))
                                        .consumeParticles(false)
                                        .build()
                        )
        );
        this.dropItem = Objects.requireNonNull(dropItem);
        this.dropQuantity = dropQuantity;
        this.washSound = Objects.requireNonNull(washSound);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (isLookingAtWater(world, user)) {
            return net.minecraft.item.ItemUsage.consumeHeldItem(world, user, hand);
        }
        return ActionResult.PASS;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        if (!isLookingAtWater(world, player)) {
            player.stopUsingItem();
            return;
        }

        if (remainingUseTicks % 5 == 0) {
            world.playSound(
                    null,
                    player.getBlockPos(),
                    this.washSound,
                    SoundCategory.PLAYERS,
                    0.5F,
                    0.9F + (world.random.nextFloat() * 0.2F)
            );

            if (!world.isClient && world instanceof ServerWorld serverWorld) {
                Vec3d look = player.getRotationVec(1.0F);

                double forwardDistance = 0.6;
                double spawnX = player.getX() + (look.x * forwardDistance);
                double spawnY = player.getY() + 1.2 + (look.y * 0.5);
                double spawnZ = player.getZ() + (look.z * forwardDistance);

                serverWorld.spawnParticles(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.WATER.getDefaultState()),
                        spawnX,
                        spawnY,
                        spawnZ,
                        3,
                        0.15, 0.1, 0.15,
                        0.02
                );

                serverWorld.spawnParticles(
                        ParticleTypes.SPLASH,
                        spawnX, spawnY - 0.2, spawnZ,
                        2, 0.1, 0.1, 0.1, 0.05
                );
            }
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack remainingStack = super.finishUsing(stack, world, user);

        if (user instanceof PlayerEntity player) {
            world.playSound(
                    null,
                    player.getBlockPos(),
                    SoundEvents.ENTITY_ITEM_PICKUP,
                    SoundCategory.NEUTRAL,
                    2.0F,
                    0.5F
            );

            ItemStack result = new ItemStack(dropItem, dropQuantity);

            // 1. Check if the player already has a non-full stack of the resulting item
            int slotWithRoom = player.getInventory().getOccupiedSlotWithRoomForStack(result);

            if (slotWithRoom != -1) {
                // They have a stack! Let's force it to merge there first.
                player.getInventory().insertStack(result);

                // If the existing stack filled up but there is still some result left over...
                if (!result.isEmpty()) {
                    if (remainingStack.isEmpty()) {
                        return result; // Put leftover in the hand
                    } else if (!player.getInventory().insertStack(result)) {
                        player.dropItem(result, true); // Inventory full, drop it
                    }
                }
                return remainingStack;
            }

            // 2. If NO existing stack was found to merge with:
            if (remainingStack.isEmpty()) {
                return result; // Hand is empty, put it directly in the hand
            } else {
                if (!player.getInventory().insertStack(result)) {
                    player.dropItem(result, true);
                }
            }
        }

        return remainingStack;
    }

    private boolean isLookingAtWater(World world, PlayerEntity player) {
        BlockHitResult hitResult = raycast(world, player, RaycastContext.FluidHandling.WATER);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return world.getFluidState(hitResult.getBlockPos()).isOf(Fluids.WATER);
        }
        return false;
    }
}