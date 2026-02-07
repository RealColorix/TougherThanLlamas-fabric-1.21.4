package net.colorixer.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.block.BlockState;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class DestroyBlockCriterion extends AbstractCriterion<DestroyBlockCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, BlockPos pos) {
        ServerWorld world = player.getServerWorld();
        BlockState state = world.getBlockState(pos);
        // Check if the block broken matches the predicate defined in the JSON
        this.trigger(player, conditions -> conditions.matches(world, pos));
    }

    public record Conditions(Optional<LootContextPredicate> player, Optional<BlockPredicate> block) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                        BlockPredicate.CODEC.optionalFieldOf("block").forGetter(Conditions::block)
                ).apply(instance, Conditions::new)
        );

        public boolean matches(ServerWorld world, BlockPos pos) {
            // If no block is specified in JSON, it matches everything.
            // Otherwise, it checks if the block/tag matches.
            return block.isEmpty() || block.get().test(world, pos);
        }
    }
}