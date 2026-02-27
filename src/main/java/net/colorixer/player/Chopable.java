package net.colorixer.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.colorixer.TougherThanLlamas;
import net.colorixer.block.BlockTags;
import net.colorixer.util.IdentifierUtilForChopableClass;
import net.colorixer.util.RegistryUtilForChopableClass;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.world.World;
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
    private static final Random RANDOM = new Random();

    private record Drops(float chance, List<Item> items) {}
    private record ReplacementResult(Block resultBlock, Block displayBlock, List<Drops> drops, boolean doLootTable, float[] miningSpeeds) {}
    public static BlockState getGhostResult(World world, BlockPos pos, ItemStack toolStack) {
        BlockState oldState = world.getBlockState(pos);
        BlockReplacementConditions conditions = CHOPABLE_MAP.get(oldState.getBlock());

        if (conditions == null) return null;

        // Find the matching tool result (logic remains same)
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

        // NEW LOGIC:
        if (match != null) {
            // If it's null (from "blockdisplay": false), don't render anything
            if (match.displayBlock() == null) return null;

            BlockState newState = match.displayBlock().getDefaultState();

            // Transfer properties...
            for (Property<?> property : oldState.getProperties()) {
                if (newState.contains(property)) {
                    newState = transferProperty(oldState, newState, property);
                }
            }
            return newState;
        }

        return null;
    }


    public static void initialize() {
        TougherThanLlamas.LOGGER.info("Registering Chopable for " + TougherThanLlamas.MOD_ID);
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new Chopable());
        // Visuals for Client
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new Chopable());

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient && world instanceof ServerWorld serverWorld) {
                ItemStack toolStack = player.getMainHandStack();
               return tryReplaceBlockBeforeBreak(serverWorld, pos, state, toolStack, player, blockEntity);
            }
            return true;
        });
    }

    public static float getChopableSpeedMultiplier(World world, BlockPos pos, ItemStack toolStack) {
        BlockState state = world.getBlockState(pos);
        BlockReplacementConditions conditions = CHOPABLE_MAP.get(state.getBlock());

        if (conditions == null) return 1.0f;

        // Find the match FIRST to see if we have custom tool speeds
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
        if (match == null) return 1.0f;

        // CHECK CONDITIONS (Block Above)
        boolean blocked = conditions.isBlockAboveDisables() && isObstructive(world, pos.up());
        // index 0 = success speed, index 1 = fail speed
        float successSpeed = match.miningSpeeds()[0];
        float failSpeed = match.miningSpeeds().length > 1 ? match.miningSpeeds()[1] : 1.0f;

        return blocked ? failSpeed : successSpeed;
    }

    @Override
    public @NotNull Identifier getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<Block, BlockReplacementConditions> newMap = new HashMap<>();

        // Use findResources to grab everything in the 'chopable' folder
        Map<Identifier, Resource> resources = manager.findResources("chopable", id -> id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Resource resource = entry.getValue();

            try (var is = resource.getInputStream();
                 var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (!root.has("conditions")) continue;

                // Extract block name from the filename
                String path = resourceId.getPath();
                String fileName = path.substring(path.lastIndexOf('/') + 1);
                String blockName = fileName.substring(0, fileName.length() - ".json".length());

                // Build the full ID (e.g., ttll:stone)
                Identifier blockId = Identifier.of(resourceId.getNamespace(), blockName);
                Block block = Registries.BLOCK.get(blockId);

                // Skip if the block doesn't exist (returns air by default)
                if (block == Registries.BLOCK.get(Registries.BLOCK.getDefaultId()) && !blockName.equals("air")) continue;

                BlockReplacementConditions replacementConditions = new BlockReplacementConditions();
                JsonArray conditionsArray = root.getAsJsonArray("conditions");

                for (JsonElement element : conditionsArray) {
                    JsonObject obj = element.getAsJsonObject();

                    // 1. Basic Block and Loot settings
                    Block resBlock = Registries.BLOCK.get(Identifier.of(obj.get("result").getAsString()));

                    Block displayBlock = resBlock;
                    if (obj.has("blockdisplay")) {
                        JsonElement displayElem = obj.get("blockdisplay");

                        if (displayElem.isJsonPrimitive() && displayElem.getAsJsonPrimitive().isBoolean()) {
                            if (!displayElem.getAsBoolean()) {
                                displayBlock = null; // "blockdisplay": false
                            }
                        } else if (displayElem.isJsonPrimitive() && displayElem.getAsJsonPrimitive().isString()) {
                            displayBlock = Registries.BLOCK.get(Identifier.of(displayElem.getAsString()));
                        }
                    }

                    // 2. NEW: Parse the drop groups
                    boolean doLoot = !obj.has("doloottable") || obj.get("doloottable").getAsBoolean();

                    List<Drops> groups = new ArrayList<>();
                    if (obj.has("drops")) {
                        JsonArray groupsArray = obj.getAsJsonArray("drops");
                        for (JsonElement gElem : groupsArray) {
                            JsonObject gObj = gElem.getAsJsonObject();

                            float groupChance = gObj.has("chance") ? gObj.get("chance").getAsFloat() : 1.0f;

                            List<Item> groupItems = new ArrayList<>();
                            JsonArray itemsArray = gObj.getAsJsonArray("items");
                            for (JsonElement iElem : itemsArray) {
                                groupItems.add(Registries.ITEM.get(Identifier.of(iElem.getAsString())));
                            }
                            groups.add(new Drops(groupChance, groupItems));
                        }
                    }

                    // 3. Parse Mining Speeds (with your array safety)
                    JsonElement speedElement = obj.get("miningspeed");
                    float[] speeds;
                    if (speedElement != null && speedElement.isJsonArray()) {
                        JsonArray arr = speedElement.getAsJsonArray();
                        // Check size to prevent IndexOutOfBounds if someone only puts [2]
                        float success = arr.get(0).getAsFloat();
                        float fail = (arr.size() > 1) ? arr.get(1).getAsFloat() : 1.0f;
                        speeds = new float[]{success, fail};
                    } else {
                        speeds = new float[]{speedElement != null ? speedElement.getAsFloat() : 1.0f, 1.0f};
                    }

                    // 4. Create the result package with the List of groups instead of a single item
                    ReplacementResult resultPackage = new ReplacementResult(resBlock, displayBlock, groups, doLoot, speeds);
                    // 5. Handle block disabling and registration
                    if (obj.has("blockabovedisables")) {
                        replacementConditions.setBlockAboveDisables(obj.get("blockabovedisables").getAsBoolean());
                    }

                    if (obj.has("item")) {
                        String itemField = obj.get("item").getAsString();
                        if (itemField.startsWith("#")) {
                            Identifier tagId = Identifier.of(itemField.substring(1));
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

        CHOPABLE_MAP.clear();
        CHOPABLE_MAP.putAll(newMap);
    }

    private static boolean isObstructive(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return false;
        if (state.isReplaceable()) return false;
        if (state.isIn(net.minecraft.registry.tag.BlockTags.FLOWERS)) return false;
        if (state.isOf(Blocks.SUGAR_CANE)) return false;
        return !state.isIn(BlockTags.FOLIAGESLOWERS);
    }

    private static boolean tryReplaceBlockBeforeBreak(ServerWorld world, BlockPos pos, BlockState oldState, ItemStack toolStack, PlayerEntity player, BlockEntity blockEntity) {
        if (player.isCreative()) {
            return true;
        }
        BlockReplacementConditions conditions = CHOPABLE_MAP.get(oldState.getBlock());
        if (conditions == null) return true;
        boolean blocked = conditions.isBlockAboveDisables() && isObstructive(world, pos.up());
        if (blocked) return true;
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

        // Logic for drops with Random Chance
        List<ItemStack> drops = new ArrayList<>();
        if (match.doLootTable()) {
            drops.addAll(Block.getDroppedStacks(oldState, world, pos, blockEntity, player, toolStack));
        }

        // Process weighted groups
        for (Drops group : match.drops()) {
            if (RANDOM.nextFloat() <= group.chance()) {
                // Pick ONE random item from this specific group
                Item chosen = group.items().get(RANDOM.nextInt(group.items().size()));
                drops.add(new ItemStack(chosen));
            }
        }


        //----------------------------------
        //------- ITEM DROPPING CODE -------
        //----------------------------------
        Vec3d blockCenter = Vec3d.ofCenter(pos);
        Vec3d playerEyePos = player.getEyePos();

        Map<net.minecraft.util.math.Direction, Vec3d> faceOffsets = Map.of(
                net.minecraft.util.math.Direction.UP,    new Vec3d(0, 0.65, 0),
                net.minecraft.util.math.Direction.DOWN,  new Vec3d(0, -0.65, 0),
                net.minecraft.util.math.Direction.NORTH, new Vec3d(0, 0, -0.65),
                net.minecraft.util.math.Direction.SOUTH, new Vec3d(0, 0, 0.65),
                net.minecraft.util.math.Direction.WEST,  new Vec3d(-0.65, 0, 0),
                net.minecraft.util.math.Direction.EAST,  new Vec3d(0.65, 0, 0)
        );

        Vec3d spawnPos = null;
        net.minecraft.util.math.Direction chosenSide = null;
        double minDistance = Double.MAX_VALUE;

// 1. Identify the closest valid face
        for (var entry : faceOffsets.entrySet()) {
            net.minecraft.util.math.Direction side = entry.getKey();
            BlockPos neighborPos = pos.offset(side);

            if (!world.getBlockState(neighborPos).isFullCube(world, neighborPos)) {
                Vec3d potentialPos = blockCenter.add(entry.getValue());
                double dist = potentialPos.squaredDistanceTo(playerEyePos);

                if (dist < minDistance) {
                    minDistance = dist;
                    spawnPos = potentialPos;
                    chosenSide = side;
                }
            }
        }

// 2. Refine spawnPos to the closest point on the face's ring (radius 0.65)
        if (chosenSide != null) {
            Vec3d relativePlayer = playerEyePos.subtract(blockCenter);
            double ringRadius = 0.65;

            // We project the player's relative position onto the 2D plane of the face
            switch (chosenSide.getAxis()) {
                case Y -> { // UP or DOWN: Ring is in XZ plane
                    Vec3d dir = new Vec3d(relativePlayer.x, 0, relativePlayer.z).normalize().multiply(ringRadius);
                    spawnPos = new Vec3d(blockCenter.x + dir.x, spawnPos.y, blockCenter.z + dir.z);
                }
                case X -> { // EAST or WEST: Ring is in YZ plane
                    Vec3d dir = new Vec3d(0, relativePlayer.y, relativePlayer.z).normalize().multiply(ringRadius);
                    spawnPos = new Vec3d(spawnPos.x, blockCenter.y + dir.y, blockCenter.z + dir.z);
                }
                case Z -> { // NORTH or SOUTH: Ring is in XY plane
                    Vec3d dir = new Vec3d(relativePlayer.x, relativePlayer.y, 0).normalize().multiply(ringRadius);
                    spawnPos = new Vec3d(blockCenter.x + dir.x, blockCenter.y + dir.y, spawnPos.z);
                }
            }
        } else {
            spawnPos = blockCenter;
        }



// 3. Velocity and Spawning
        Vec3d toPlayer = playerEyePos.subtract(spawnPos);
        Vec3d horizontalBase = new Vec3d(toPlayer.x, 0, toPlayer.z).normalize().multiply(0.2);

        for (ItemStack stack : drops) {
            // Note: Used your requested -0.1 Y-offset for the entity spawn
            ItemEntity entity = new ItemEntity(world, spawnPos.x, spawnPos.y - 0.1, spawnPos.z, stack);

            double jitterX = (RANDOM.nextDouble() - 0.5) * 0.1;
            double jitterZ = (RANDOM.nextDouble() - 0.5) * 0.1;
            double yVel = (chosenSide == net.minecraft.util.math.Direction.DOWN) ? 0.0 : 0.1 + (RANDOM.nextDouble() * 0.05);

            entity.setVelocity(horizontalBase.x + jitterX, yVel, horizontalBase.z + jitterZ);
            world.spawnEntity(entity);
        }
        //---------------------------------
        //--------- OTHER CODE ------------
        //---------------------------------

        // Replace block and transfer properties
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