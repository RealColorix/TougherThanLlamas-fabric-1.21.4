package net.colorixer.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

public class InVicinityCriterion extends AbstractCriterion<InVicinityCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, BlockPos pos) {
        ServerWorld world = player.getServerWorld();
        // Check both advancement requirements and block/tag matches
        this.trigger(player, conditions -> conditions.matches(player, world, pos));
    }

    public record Conditions(
            Optional<LootContextPredicate> player,
            Optional<BlockPredicate> block,
            List<Identifier> requiredAdvancements
    ) implements AbstractCriterion.Conditions {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                        BlockPredicate.CODEC.optionalFieldOf("block").forGetter(Conditions::block),
                        Identifier.CODEC.listOf().optionalFieldOf("required_advancements", List.of()).forGetter(Conditions::requiredAdvancements)
                ).apply(instance, Conditions::new)
        );

        public boolean matches(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
            // 1. Check if at least one required advancement is done (if the list isn't empty)
            if (!requiredAdvancements.isEmpty()) {
                boolean hasRequired = false;
                for (Identifier id : requiredAdvancements) {
                    AdvancementEntry entry = player.getServer().getAdvancementLoader().get(id);
                    if (entry != null && player.getAdvancementTracker().getProgress(entry).isDone()) {
                        hasRequired = true;
                        break;
                    }
                }
                if (!hasRequired) return false;
            }

            // 2. Check the block. block.get().test(world, pos) handles tags (#) automatically!
            return block.isEmpty() || block.get().test(world, pos);
        }
    }
}