package net.colorixer.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.colorixer.util.RegistryUtilForChopableClass;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;

import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A single class that:
 *  1) Loads JSON from data/<namespace>/chopable/*.json
 *  2) Stores the "block → (tool → replacement)" map
 *  3) Listens for block-break events BEFORE they happen, and replaces blocks immediately (no air flicker).
 */
public class Chopable implements SimpleSynchronousResourceReloadListener {

    /**
     * The ID we give our resource reload listener.
     * Use Identifier.tryParse(...) to avoid private constructor issues.
     */
    private static final Identifier RELOAD_LISTENER_ID = Identifier.tryParse("ttll:chopable_reload");

    /**
     * Our final "Block -> ( ToolItemId -> ReplacementBlock )" map.
     */
    private static final Map<Block, Map<String, Block>> CHOPABLE_MAP = new HashMap<>();

    /**
     * Returns an unmodifiable view of our chopable map (if you need it elsewhere).
     */
    public static Map<Block, Map<String, Block>> getChopableMap() {
        return Collections.unmodifiableMap(CHOPABLE_MAP);
    }

    /**
     * Called once (in onInitialize) to set up our reload listener and
     * the block-break event (BEFORE).
     */
    public static void initialize() {
        // 1. Register this class as a resource reload listener (so it loads JSON from data packs).
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new Chopable());

        // 2. Register the BEFORE break event (not AFTER!), so we can do an instant replace
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient && world instanceof ServerWorld serverWorld) {
                ItemStack toolStack = player.getMainHandStack();
                // If we replaced the block ourselves, return false to CANCEL the normal break (no air flicker).
                boolean replaced = tryReplaceBlockBeforeBreak(serverWorld, pos, state, toolStack, player, blockEntity);
                if (replaced) {
                    return false; // we handled it
                }
            }
            // If we didn’t replace it, let normal break happen (return true).
            return true;
        });
    }

    /* -------------------------------------------------------------------------------------------- */
    /*  Implement SimpleSynchronousResourceReloadListener to read "chopable/*.json" from data packs  */
    /* -------------------------------------------------------------------------------------------- */

    @Override
    public @NotNull Identifier getFabricId() {
        return RELOAD_LISTENER_ID != null ? RELOAD_LISTENER_ID
                : Objects.requireNonNull(Identifier.tryParse("minecraft:missing"));
    }

    @Override
    public void reload(ResourceManager manager) {
        // We'll build a new map on each reload, then swap it in when finished.
        Map<Block, Map<String, Block>> newMap = new HashMap<>();

        try {
            // Finds all JSONs under "chopable" (e.g. data/minecraft/chopable/oak_log.json).
            Map<Identifier, Resource> resources = manager.findResources(
                    "chopable",
                    id -> id.getPath().endsWith(".json")
            );

            for (Identifier resourceId : resources.keySet()) {
                Resource resource = resources.get(resourceId);
                if (resource == null) continue;

                try (var is = resource.getInputStream();
                     var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                    // Parse the JSON
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    if (!root.has("conditions") || !root.get("conditions").isJsonArray()) {
                        // Skip if no conditions array
                        continue;
                    }
                    JsonArray conditionsArray = root.getAsJsonArray("conditions");

                    // Example resourceId: "minecraft:chopable/oak_log.json"
                    //   namespace: resourceId.getNamespace() = "minecraft"
                    //   path: resourceId.getPath() = "chopable/oak_log.json"
                    //   blockName = "oak_log" after removing "chopable/" prefix and ".json"
                    String fullPath = resourceId.getPath(); // e.g. chopable/oak_log.json
                    String fileName = fullPath.substring("chopable/".length()); // oak_log.json
                    String blockName = fileName.replace(".json", "");           // oak_log
                    String fullBlockId = resourceId.getNamespace() + ":" + blockName;
                    // -> "minecraft:oak_log"

                    Block block = RegistryUtilForChopableClass.getBlock(fullBlockId);
                    if (block == null) {
                        System.err.println("Skipping " + resourceId + " - Block not found: " + fullBlockId);
                        continue;
                    }

                    // Build a "tool -> replacement block" map
                    Map<String, Block> conditionsMap = new HashMap<>();
                    for (JsonElement element : conditionsArray) {
                        if (!element.isJsonObject()) {
                            continue;
                        }
                        JsonObject obj = element.getAsJsonObject();
                        if (!obj.has("item") || !obj.has("result")) {
                            continue;
                        }

                        String itemId = obj.get("item").getAsString();
                        String resultId = obj.get("result").getAsString();
                        Block resultBlock = RegistryUtilForChopableClass.getBlock(resultId);
                        if (resultBlock == null) {
                            System.err.println("Invalid 'result' block: " + resultId + " in " + resourceId);
                            continue;
                        }
                        conditionsMap.put(itemId, resultBlock);
                    }

                    if (!conditionsMap.isEmpty()) {
                        newMap.put(block, conditionsMap);
                    }

                } catch (Exception e) {
                    System.err.println("Error reading chopable JSON " + resourceId + ": " + e);
                }
            }
        } catch (Exception e) {
            System.err.println("Error while loading chopable resources: " + e);
        }

        // Replace old data with the newly loaded data
        CHOPABLE_MAP.clear();
        CHOPABLE_MAP.putAll(newMap);

        System.out.println("[Chopable] Reloaded chopable data for " + CHOPABLE_MAP.size() + " blocks.");
    }

    /* -------------------------------------------------------------------------------------------- */
    /*  The block-break logic that uses our map to replace blocks IMMEDIATELY (no air flicker).     */
    /* -------------------------------------------------------------------------------------------- */

    /**
     * Called from BEFORE event: if we do the replacement, return true so we can CANCEL the normal break.
     *
     * @param world       The server world
     * @param pos         The block position
     * @param oldState    The old block state
     * @param toolStack   The tool used to break
     * @param player      The player breaking the block
     * @param blockEntity The block entity, if any, for drops
     * @return true if we replaced the block ourselves (cancel normal break), or false if we did nothing
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

        // Check if the block is in our chopable map
        Map<String, Block> conditions = CHOPABLE_MAP.get(block);
        if (conditions == null) {
            return false; // Not a "chopable" block => let normal break happen
        }

        // Registry ID of the tool used (e.g. "minecraft:diamond_axe")
        String toolId = Registries.ITEM.getId(toolStack.getItem()).toString();
        // Is there a special replacement for that tool?
        Block resultBlock = conditions.get(toolId);
        if (resultBlock != null) {

            Block.dropStacks(oldState, world, pos, blockEntity, player, toolStack);

            BlockState newState = resultBlock.getDefaultState();

            // Transfer orientation/properties (e.g. axis, facing) to the new state
            for (Property<?> property : oldState.getProperties()) {
                if (newState.contains(property)) {
                    newState = transferProperty(oldState, newState, property);
                }
            }

            // If you WANT to drop items as if the original block was broken, do it here:
            // Block.dropStacks(oldState, world, pos, blockEntity, player, toolStack);

            // Replace the block in place (never becomes air!)
            world.setBlockState(pos, newState);

            // Emit a game event for block change
            world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, pos);

            // Return true => "We handled the break; cancel vanilla break => NO flicker"
            return true;
        }

        return false; // Not replaced => let normal break happen
    }

    /**
     * Copies a property (e.g. "axis", "facing", "waterlogged") from one state to another.
     */
    private static <T extends Comparable<T>> BlockState transferProperty(BlockState from, BlockState to, Property<T> property) {
        return to.with(property, from.get(property));
    }
}
