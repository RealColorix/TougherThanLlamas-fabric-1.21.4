package net.colorixer.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.colorixer.util.IdentifierUtilForChopableClass;
import net.colorixer.util.RegistryUtilForChopableClass;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Chopable implements SimpleSynchronousResourceReloadListener {

    private static final Identifier RELOAD_LISTENER_ID = IdentifierUtilForChopableClass.createIdentifier("ttll", "chopable_reload");
    private static final Map<Block, BlockReplacementConditions> CHOPABLE_MAP = new HashMap<>();

    // Bundles the block change with the specific drop for a specific tool condition
    private record ReplacementResult(Block resultBlock, Item dropItem, boolean doLootTable) {}

    public static void initialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new Chopable());

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient && world instanceof ServerWorld serverWorld) {
                ItemStack toolStack = player.getMainHandStack();
                return tryReplaceBlockBeforeBreak(serverWorld, pos, state, toolStack, player, blockEntity);
            }
            return true;
        });
    }

    @Override
    public @NotNull Identifier getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Block, BlockReplacementConditions> newMap = new HashMap<>();

        try {
            Map<Identifier, Resource> resources = manager.findResources("chopable", id -> id.getPath().endsWith(".json"));

            for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                Identifier resourceId = entry.getKey();
                Resource resource = entry.getValue();

                try (var is = resource.getInputStream();
                     var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    if (!root.has("conditions")) continue;

                    String path = resourceId.getPath();
                    String fileName = path.substring(path.lastIndexOf('/') + 1);
                    String blockName = fileName.substring(0, fileName.length() - ".json".length());
                    Block block = RegistryUtilForChopableClass.getBlock(resourceId.getNamespace() + ":" + blockName);

                    if (block == null) continue;

                    BlockReplacementConditions replacementConditions = new BlockReplacementConditions();
                    JsonArray conditionsArray = root.getAsJsonArray("conditions");

                    for (JsonElement element : conditionsArray) {
                        JsonObject obj = element.getAsJsonObject();

                        // Parse local result logic
                        Block resBlock = RegistryUtilForChopableClass.getBlock(obj.get("result").getAsString());
                        boolean doLoot = !obj.has("doloottable") || obj.get("doloottable").getAsBoolean();
                        Item drop = obj.has("drop_item") ? Registries.ITEM.get(Identifier.of(obj.get("drop_item").getAsString())) : null;

                        ReplacementResult resultPackage = new ReplacementResult(resBlock, drop, doLoot);

                        if (obj.has("blockabovedisables")) {
                            replacementConditions.setBlockAboveDisables(obj.get("blockabovedisables").getAsBoolean());
                        }

                        if (obj.has("item")) {
                            String itemField = obj.get("item").getAsString();
                            if (itemField.startsWith("#")) {
                                Identifier tagId = Identifier.tryParse(itemField.substring(1));
                                replacementConditions.addTagReplacement(TagKey.of(RegistryKeys.ITEM, tagId), resultPackage);
                            } else {
                                replacementConditions.addSpecificItem(itemField, resultPackage);
                            }
                        } else {
                            replacementConditions.setDefaultResult(resultPackage);
                        }
                    }
                    newMap.put(block, replacementConditions);
                } catch (Exception e) {
                    System.err.println("Error reading chopable JSON " + resourceId + ": " + e);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading chopable resources: " + e);
        }

        CHOPABLE_MAP.clear();
        CHOPABLE_MAP.putAll(newMap);
    }

    private static boolean tryReplaceBlockBeforeBreak(ServerWorld world, BlockPos pos, BlockState oldState, ItemStack toolStack, PlayerEntity player, BlockEntity blockEntity) {
        BlockReplacementConditions conditions = CHOPABLE_MAP.get(oldState.getBlock());
        if (conditions == null) return true;
        if (conditions.isBlockAboveDisables() && !world.isAir(pos.up())) return true;

        // Determine which result package to use based on the tool
        ReplacementResult match = conditions.getSpecificItemMap().get(Registries.ITEM.getId(toolStack.getItem()).toString());
        if (match == null) {
            for (var tagEntry : conditions.getTagReplacements().entrySet()) {
                if (toolStack.isIn(tagEntry.getKey())) {
                    match = tagEntry.getValue();
                    break;
                }
            }
        }
        if (match == null) match = conditions.getDefaultResult();
        if (match == null) return true;

        // Damage tool
        if (!player.isCreative() && toolStack.isDamageable()) {
            toolStack.damage(1, player, EquipmentSlot.MAINHAND);
            if (toolStack.getDamage() >= toolStack.getMaxDamage()) {
                player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                world.playSound(null, pos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }

        // Logic for drops
        List<ItemStack> drops = new ArrayList<>();
        if (match.doLootTable()) {
            drops.addAll(Block.getDroppedStacks(oldState, world, pos, blockEntity, player, toolStack));
        } else if (match.dropItem() != null) {
            drops.add(new ItemStack(match.dropItem()));
        }

        // Physics spawning
        Vec3d blockCenter = Vec3d.ofCenter(pos);
        Vec3d direction = player.getEyePos().subtract(blockCenter).normalize();
        Vec3d spawnPos = blockCenter.add(direction.multiply(0.5));

        for (ItemStack stack : drops) {
            ItemEntity entity = new ItemEntity(world, spawnPos.x, spawnPos.y, spawnPos.z, stack);
            entity.setVelocity(direction.multiply(0.2).add(0, 0.1, 0));
            world.spawnEntity(entity);
        }

        // Replace block and transfer properties (like facing/axis)
        BlockState newState = match.resultBlock().getDefaultState();
        for (Property<?> property : oldState.getProperties()) {
            if (newState.contains(property)) {
                newState = transferProperty(oldState, newState, property);
            }
        }

        world.setBlockState(pos, newState);
        world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        world.playSound(null, pos, oldState.getSoundGroup().getHitSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);

        return false;
    }

    private static <T extends Comparable<T>> BlockState transferProperty(BlockState from, BlockState to, Property<T> property) {
        return to.with(property, from.get(property));
    }

    private static class BlockReplacementConditions {
        private final Map<String, ReplacementResult> specificItemMap = new HashMap<>();
        private final Map<TagKey<Item>, ReplacementResult> tagReplacements = new HashMap<>();
        private ReplacementResult defaultResult = null;
        private boolean blockAboveDisables = false;

        public void addSpecificItem(String id, ReplacementResult res) { specificItemMap.put(id, res); }
        public void addTagReplacement(TagKey<Item> tag, ReplacementResult res) { tagReplacements.put(tag, res); }
        public void setDefaultResult(ReplacementResult res) { this.defaultResult = res; }
        public void setBlockAboveDisables(boolean val) { this.blockAboveDisables = val; }

        public Map<String, ReplacementResult> getSpecificItemMap() { return specificItemMap; }
        public Map<TagKey<Item>, ReplacementResult> getTagReplacements() { return tagReplacements; }
        public ReplacementResult getDefaultResult() { return defaultResult; }
        public boolean isBlockAboveDisables() { return blockAboveDisables; }
    }
}