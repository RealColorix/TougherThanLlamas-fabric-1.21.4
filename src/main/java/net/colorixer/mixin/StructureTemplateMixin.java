package net.colorixer.mixin;

import net.colorixer.util.StructureTemplateUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {

    @Inject(method = "process", at = @At("RETURN"), cancellable = true)
    private static void onProcess(ServerWorldAccess world, BlockPos pos, BlockPos pivot, StructurePlacementData placementData, List<StructureTemplate.StructureBlockInfo> infos, CallbackInfoReturnable<List<StructureTemplate.StructureBlockInfo>> cir) {
        List<StructureTemplate.StructureBlockInfo> originalList = cir.getReturnValue();
        List<StructureTemplate.StructureBlockInfo> modifiedList = new ArrayList<>();

        for (StructureTemplate.StructureBlockInfo info : originalList) {
            // Casting to the Accessor to get the private fields
            StructureBlockInfoAccessor accessor = (StructureBlockInfoAccessor) (Object) info;

            BlockPos blockPos = accessor.getPos();
            BlockState originalState = accessor.getState();
            NbtCompound nbt = accessor.getNbt();

            // FIX: Passing all 4 required arguments now
            // 1. originalState
            // 2. blockPos
            // 3. originalList (for wall finding)
            // 4. nbt (for LootTable checking)
            BlockState newState = StructureTemplateUtil.getReplacementState(originalState, blockPos, originalList, nbt);

            modifiedList.add(new StructureTemplate.StructureBlockInfo(blockPos, newState, nbt));
        }

        cir.setReturnValue(modifiedList);
    }

    @Redirect(
            method = "spawnEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/structure/StructureTemplate;getEntity(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/nbt/NbtCompound;)Ljava/util/Optional;")
    )
    private Optional<?> filterVillagerSpawns(ServerWorldAccess world, NbtCompound nbt) {
        String id = nbt.getString("id");
        if (nbt.contains("Pos", 9)) {
            double x = nbt.getList("Pos", 6).getDouble(0);
            double z = nbt.getList("Pos", 6).getDouble(2);
            if (!StructureTemplateUtil.shouldShowEntity(id, new BlockPos((int)x, 64, (int)z))) {
                return Optional.empty();
            }
        }

        try {
            return EntityType.getEntityFromNbt(nbt, world.toServerWorld(), net.minecraft.entity.SpawnReason.STRUCTURE);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}