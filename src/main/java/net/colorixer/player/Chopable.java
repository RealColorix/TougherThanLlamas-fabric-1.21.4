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
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
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
import net.minecraft.item.ItemStack;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;

import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * Handles block replacement logic based on specific items, tags, or default conditions.
 */
public class Chopable implements SimpleSynchronousResourceReloadListener {

    /**
     * Identifier for the resource reload listener.
     */
    private static final Identifier RELOAD_LISTENER_ID = IdentifierUtilForChopableClass.createIdentifier("ttll", "chopable_reload");

    /**
     * Mapping from blocks to their replacement conditions.
     */
    private static final Map<Block, BlockReplacementConditions> CHOPABLE_MAP = new HashMap<>();

    /**
     * Initializes the Chopable class by registering the reload listener and block break event.
     * This method should be called during your mod's initialization phase.
     */
    public static void initialize() {
        // Register this class as a resource reload listener to load JSON configurations.
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new Chopable());

        // Register the BEFORE break event to handle block replacement before the block breaks.
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient && world instanceof ServerWorld serverWorld) {
                ItemStack toolStack = player.getMainHandStack();
                // Attempt to replace the block before it breaks.
                return tryReplaceBlockBeforeBreak(serverWorld, pos, state, toolStack, player, blockEntity);
            }
            return true; // Allow the block to break normally if no replacement is needed.
        });
    }

    /* -------------------------------------------------------------------------------------------- */
    /*  Implementation of SimpleSynchronousResourceReloadListener to load "chopable/*.json"       */
    /* -------------------------------------------------------------------------------------------- */

    @Override
    public @NotNull Identifier getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        // Temporary map to store new chopable data.
        Map<Block, BlockReplacementConditions> newMap = new HashMap<>();

        try {
            // Locate all JSON files under the "chopable" directory.
            Map<Identifier, Resource> resources = manager.findResources(
                    "chopable",
                    id -> id.getPath().endsWith(".json")
            );

            for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                Identifier resourceId = entry.getKey();
                Resource resource = entry.getValue();
                if (resource == null) continue;

                try (var is = resource.getInputStream();
                     var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                    // Parse the JSON content.
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    if (!root.has("conditions") || !root.get("conditions").isJsonArray()) {
                        // Skip files without a "conditions" array.
                        continue;
                    }
                    JsonArray conditionsArray = root.getAsJsonArray("conditions");

                    // Extract block information from the file path.
                    String fullPath = resourceId.getPath(); // e.g., "chopable/oak_log.json"
                    if (!fullPath.startsWith("chopable/")) {
                        System.err.println("Skipping " + resourceId + " - Invalid path: " + fullPath);
                        continue;
                    }
                    String fileName = fullPath.substring("chopable/".length()); // "oak_log.json"
                    if (!fileName.endsWith(".json")) {
                        System.err.println("Skipping " + resourceId + " - File does not end with .json: " + fileName);
                        continue;
                    }
                    String blockName = fileName.substring(0, fileName.length() - ".json".length()); // "oak_log"
                    String fullBlockId = resourceId.getNamespace() + ":" + blockName; // "ttll:oak_log"

                    // Retrieve the block from the registry.
                    Block block = RegistryUtilForChopableClass.getBlock(fullBlockId);
                    if (block == null) {
                        System.err.println("Skipping " + resourceId + " - Block not found: " + fullBlockId);
                        continue;
                    }

                    // Initialize replacement conditions for this block.
                    BlockReplacementConditions replacementConditions = new BlockReplacementConditions();

                    for (JsonElement element : conditionsArray) {
                        if (!element.isJsonObject()) {
                            System.err.println("Skipping invalid condition in " + resourceId + " - not a JSON object");
                            continue;
                        }
                        JsonObject obj = element.getAsJsonObject();
                        if (!obj.has("result")) {
                            System.err.println("Skipping condition in " + resourceId + " - missing 'result' field");
                            continue;
                        }

                        String resultId = obj.get("result").getAsString();
                        Block resultBlock = RegistryUtilForChopableClass.getBlock(resultId);
                        if (resultBlock == null) {
                            System.err.println("Invalid 'result' block: " + resultId + " in " + resourceId);
                            continue;
                        }

                        if (obj.has("item")) {
                            String itemField = obj.get("item").getAsString();
                            if (IdentifierUtilForChopableClass.isTag(itemField)) {
                                // Handle tag-based replacement.
                                TagReplacement tagReplacement = parseTagReplacement(itemField, resultBlock, resourceId);
                                if (tagReplacement != null) {
                                    replacementConditions.addTagReplacement(tagReplacement);
                                    System.out.println("Added tag-based replacement: " + itemField + " -> " + resultId + " for block " + fullBlockId);
                                }
                            } else {
                                // Handle specific item replacement.
                                replacementConditions.addSpecificItem(itemField, resultBlock);
                                System.out.println("Added specific item replacement: " + itemField + " -> " + resultId + " for block " + fullBlockId);
                            }
                        } else {
                            // Handle default replacement.
                            replacementConditions.setDefaultReplacement(resultBlock);
                            System.out.println("Added default replacement: " + resultId + " for block " + fullBlockId);
                        }

                        // Handle blockabovedisables condition.
                        if (obj.has("blockabovedisables")) {
                            boolean blockAboveDisables = obj.get("blockabovedisables").getAsBoolean();
                            replacementConditions.setBlockAboveDisables(blockAboveDisables);
                            System.out.println("Added blockabovedisables: " + blockAboveDisables + " for block " + fullBlockId);
                        }

                        // Handle doloottable condition.
                        if (obj.has("doloottable")) {
                            boolean doLootTable = obj.get("doloottable").getAsBoolean();
                            replacementConditions.setDoLootTable(doLootTable);
                            System.out.println("Added doloottable: " + doLootTable + " for block " + fullBlockId);
                        }

                    }

                    // Add to the new map if any conditions are set.
                    if (replacementConditions.hasConditions()) {
                        newMap.put(block, replacementConditions);
                    }

                } catch (Exception e) {
                    System.err.println("Error reading chopable JSON " + resourceId + ": " + e);
                }
            }
        } catch (Exception e) {
            System.err.println("Error while loading chopable resources: " + e);
        }

        // Replace old data with the newly loaded data.
        CHOPABLE_MAP.clear();
        CHOPABLE_MAP.putAll(newMap);

        System.out.println("[Chopable] Reloaded chopable data for " + CHOPABLE_MAP.size() + " blocks.");
    }

    /**
     * Parses a tag-based replacement condition from the JSON.
     *
     * @param itemField  The item field from the JSON (e.g., "#minecraft:axes").
     * @param resultBlock The block to replace with.
     * @param resourceId The resource identifier (for error logging).
     * @return A TagReplacement object or null if parsing fails.
     */
    private static TagReplacement parseTagReplacement(String itemField, Block resultBlock, Identifier resourceId) {
        // Since tags are formatted as "#minecraft:axes", directly parse the tag identifier.
        Identifier tagId = IdentifierUtilForChopableClass.isTag(itemField) ?
                Identifier.tryParse(itemField.substring(1)) : null;

        if (tagId == null) {
            System.err.println("Invalid tag format: " + itemField + " in " + resourceId);
            return null;
        }

        TagKey<Item> tagKey = TagKey.of(RegistryKeys.ITEM, tagId);
        return new TagReplacement(tagKey, resultBlock);
    }

    /* -------------------------------------------------------------------------------------------- */
    /*  Block Replacement Logic Before Break                                                      */
    /* -------------------------------------------------------------------------------------------- */

    /**
     * Attempts to replace the block before it breaks based on the tool used.
     *
     * @param world       The server world.
     * @param pos         The position of the block.
     * @param oldState    The state of the block before it was broken.
     * @param toolStack   The tool used to break the block.
     * @param player      The player who broke the block.
     * @param blockEntity The block entity, if any.
     * @return True if the block should break normally, false if it should be canceled.
     */
    private static boolean tryReplaceBlockBeforeBreak(
            ServerWorld world,
            BlockPos pos,
            BlockState oldState,
            ItemStack toolStack,
            PlayerEntity player,
            BlockEntity blockEntity
    ) {
        Block block = oldState.getBlock();
        BlockReplacementConditions conditions = CHOPABLE_MAP.get(block);

        if (conditions == null) {
            return true; // No replacement conditions set for this block; allow normal break.
        }

        // Check if there's a block above and if blockabovedisables is true.
        if (conditions.isBlockAboveDisables() && !world.isAir(pos.up())) {
            return true; // Allow normal break.
        }

        // Get the tool's identifier.
        String toolId = Registries.ITEM.getId(toolStack.getItem()).toString();

        // 1. Check for specific item replacement.
        Block resultBlock = conditions.getSpecificItemMap().get(toolId);

        // 2. If no specific item match, check for tag-based replacements.
        if (resultBlock == null) {
            for (TagReplacement tagReplacement : conditions.getTagReplacements()) {
                TagKey<Item> tag = tagReplacement.getTag();
                if (toolStack.isIn(tag)) {
                    resultBlock = tagReplacement.getReplacementBlock();
                    break; // Use the first matching tag replacement.
                }
            }
        }

        // 3. If still no replacement, check for default replacement.
        if (resultBlock == null) {
            resultBlock = conditions.getDefaultReplacement();
        }



        if (resultBlock != null) {
            // Drop the block's loot table if doLootTable is true.


            if (!toolStack.isEmpty() && toolStack.isDamageable() && !player.isCreative()) {
                // Damage the tool by 1 point; pass the EquipmentSlot (MAINHAND)
                toolStack.damage(1, player, EquipmentSlot.MAINHAND);

                // Check if the tool's damage is equal to or exceeds its maximum,
                // indicating that it should break.
                if (toolStack.getDamage() >= toolStack.getMaxDamage()) {
                    // Remove the tool from the player's main hand.
                    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                    // Play the break sound.
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            }

            if (conditions.shouldDoLootTable()) {
                // Get positions
                Vec3d blockCenter = Vec3d.ofCenter(pos);
                Vec3d playerEyes = player.getEyePos();
                boolean playerBelow = playerEyes.y < blockCenter.y;

                // Calculate spawn position offset (0.75 blocks toward player)
                Vec3d direction = playerEyes.subtract(blockCenter).normalize();
                Vec3d spawnPos = blockCenter.add(direction.multiply(0.75));

                // Calculate velocity - DIFFERENT BEHAVIOR BASED ON POSITION
                Vec3d velocity;
                if (playerBelow) {
                    // Player is below - items should fall down toward player
                    velocity = new Vec3d(
                            (world.random.nextDouble() - 0.5) * 0.1,  // Small random horizontal
                            -0.2,                                    // Force downward
                            (world.random.nextDouble() - 0.5) * 0.1  // Small random horizontal
                    );
                } else {
                    // Player is above - items should arc up toward player
                    velocity = direction.multiply(0.25).add(0, 0.15, 0); // Toward player + slight upward
                }

                // Drop items
                for (ItemStack stack : Block.getDroppedStacks(oldState, world, pos, blockEntity, player, toolStack)) {
                    ItemEntity itemEntity = new ItemEntity(
                            world,
                            spawnPos.x, spawnPos.y, spawnPos.z,
                            stack,
                            velocity.x, velocity.y, velocity.z
                    );

                    world.spawnEntity(itemEntity);
                }
            }




            BlockState newState = resultBlock.getDefaultState();

            // Transfer relevant properties from the old block to the new block.
            for (Property<?> property : oldState.getProperties()) {
                if (newState.contains(property)) {
                    newState = transferProperty(oldState, newState, property);
                }
            }

            // Replace the block in place.
            world.setBlockState(pos, newState);

            // Emit a game event for the block change.
            world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);


            return false; // Cancel the block break event.
        }

        return true; // Allow normal break if no replacement is needed.
    }

    /**
     * Transfers a property from one block state to another.
     *
     * @param from     The original block state.
     * @param to       The new block state.
     * @param property The property to transfer.
     * @param <T>      The type of the property.
     * @return The new block state with the property transferred.
     */
    private static <T extends Comparable<T>> BlockState transferProperty(BlockState from, BlockState to, Property<T> property) {
        return to.with(property, from.get(property));
    }

    /* -------------------------------------------------------------------------------------------- */
    /*  Inner Classes to Represent Replacement Conditions and Tag Replacements                     */
    /* -------------------------------------------------------------------------------------------- */

    /**
     * Holds replacement conditions for a specific block.
     */
    private static class BlockReplacementConditions {
        // Mapping from specific item IDs to replacement blocks.
        private final Map<String, Block> specificItemMap = new HashMap<>();

        // List of tag-based replacements.
        private final List<TagReplacement> tagReplacements = new ArrayList<>();

        // Default replacement block (if any).
        private Block defaultReplacement = null;

        // Whether a block above disables replacement.
        private boolean blockAboveDisables = false;

        // Whether the block should drop its loot table.
        private boolean doLootTable = true; // Default to true for backward compatibility

        /**
         * Adds a specific item and its corresponding replacement block.
         *
         * @param itemId      The ID of the item.
         * @param replacement The block to replace with.
         */
        public void addSpecificItem(String itemId, Block replacement) {
            specificItemMap.put(itemId, replacement);
        }

        /**
         * Adds a tag-based replacement.
         *
         * @param tagReplacement The tag-based replacement to add.
         */
        public void addTagReplacement(TagReplacement tagReplacement) {
            tagReplacements.add(tagReplacement);
        }

        /**
         * Sets the default replacement block.
         *
         * @param defaultReplacement The default block to replace with.
         */
        public void setDefaultReplacement(Block defaultReplacement) {
            this.defaultReplacement = defaultReplacement;
        }

        /**
         * Sets whether a block above disables replacement.
         *
         * @param blockAboveDisables Whether a block above disables replacement.
         */
        public void setBlockAboveDisables(boolean blockAboveDisables) {
            this.blockAboveDisables = blockAboveDisables;
        }

        /**
         * Sets whether the block should drop its loot table.
         *
         * @param doLootTable Whether the block should drop its loot table.
         */
        public void setDoLootTable(boolean doLootTable) {
            this.doLootTable = doLootTable;
        }

        public Map<String, Block> getSpecificItemMap() {
            return specificItemMap;
        }

        public List<TagReplacement> getTagReplacements() {
            return tagReplacements;
        }

        public Block getDefaultReplacement() {
            return defaultReplacement;
        }

        public boolean isBlockAboveDisables() {
            return blockAboveDisables;
        }

        public boolean shouldDoLootTable() {
            return doLootTable;
        }

        /**
         * Checks if any replacement conditions are set.
         *
         * @return True if any conditions are present, false otherwise.
         */
        public boolean hasConditions() {
            return !specificItemMap.isEmpty() || !tagReplacements.isEmpty() || defaultReplacement != null;
        }
    }

    /**
     * Represents a tag-based replacement condition.
     */
    private static class TagReplacement {
        private final TagKey<Item> tag;
        private final Block replacementBlock;

        public TagReplacement(TagKey<Item> tag, Block replacementBlock) {
            this.tag = tag;
            this.replacementBlock = replacementBlock;
        }

        public TagKey<Item> getTag() {
            return tag;
        }

        public Block getReplacementBlock() {
            return replacementBlock;
        }
    }
}