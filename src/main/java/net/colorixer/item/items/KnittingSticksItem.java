package net.colorixer.item.items;

import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Objects;

public class KnittingSticksItem extends Item {

    private final int maxProgress;
    private final Item dropItem;
    private final int dropQuantity;
    private final Item dropToolItem;
    private final SoundEvent knittingSound;
    private final Block particleBlock;

    // Main Constructor (Supports custom block particles)
    public KnittingSticksItem(Settings settings,
                              int maxProgress,
                              Item dropItem,
                              int dropQuantity,
                              Item dropToolItem,
                              SoundEvent knittingSound,
                              Block particleBlock) {

        super(
                settings
                        .maxDamage(maxProgress)
                        .component(
                                DataComponentTypes.FOOD,
                                new FoodComponent(0, 0.0F, true)
                        )
                        .component(
                                DataComponentTypes.CONSUMABLE,
                                ConsumableComponent.builder()
                                        .consumeSeconds(1000f)
                                        .useAction(UseAction.EAT)
                                        .sound(Registries.SOUND_EVENT.getEntry(knittingSound))
                                        .consumeParticles(false)
                                        .build()
                        )
        );
        this.maxProgress   = maxProgress;
        this.dropItem      = Objects.requireNonNull(dropItem);
        this.dropQuantity  = dropQuantity;
        this.dropToolItem  = Objects.requireNonNull(dropToolItem);
        this.knittingSound = Objects.requireNonNull(knittingSound);
        this.particleBlock = particleBlock;
    }

    // Overloaded Constructor (Defaults to null block)
    public KnittingSticksItem(Settings settings,
                              int maxProgress,
                              Item dropItem,
                              int dropQuantity,
                              Item dropToolItem,
                              SoundEvent knittingSound) {
        this(settings, maxProgress, dropItem, dropQuantity, dropToolItem, knittingSound, null);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        return net.minecraft.item.ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        if (remainingUseTicks % 5 == 0) {
            world.playSound(
                    null,
                    player.getBlockPos(),
                    this.knittingSound,
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

                // Particle logic: Use block particles if specified, otherwise use item particles
                if (this.particleBlock != null) {
                    serverWorld.spawnParticles(
                            new BlockStateParticleEffect(ParticleTypes.BLOCK, this.particleBlock.getDefaultState()),
                            spawnX, spawnY, spawnZ,
                            3, 0.15, 0.1, 0.15, 0.02
                    );
                } else {
                    serverWorld.spawnParticles(
                            new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(this.dropItem)),
                            spawnX, spawnY, spawnZ,
                            3, 0.15, 0.1, 0.15, 0.02
                    );
                }

                // Your original progress logic
                int progress = stack.getDamage() + 1;

                if (progress >= maxProgress) {
                    completeKnitting(player, stack);
                    player.stopUsingItem();
                } else {
                    stack.setDamage(progress);
                }
            }
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F * stack.getDamage() / maxProgress);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        float ratio = (float) stack.getDamage() / maxProgress;
        int red, green, blue = 0;
        if (ratio < 0.5F) {
            red   = 255;
            green = Math.round(510 * ratio);
        } else {
            red   = Math.round(255 * (1.0F - ratio) * 2);
            green = 255;
        }
        return (red << 16) | (green << 8) | blue;
    }

    private void completeKnitting(PlayerEntity player, ItemStack stack) {
        World world = player.getWorld();
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 2.0F, 0.5F);

        stack.decrement(1);

        ItemStack result = new ItemStack(dropItem, dropQuantity);
        if (!player.getInventory().insertStack(result)) {
            player.dropItem(result, true);
        }

        ItemStack toolBack = new ItemStack(dropToolItem, 1);
        if (!player.getInventory().insertStack(toolBack)) {
            player.dropItem(toolBack, true);
        }
    }

    @Override
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public boolean canBeNested() {
        return false;
    }
}