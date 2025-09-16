package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/HoglinSpecificSensor.class */
public class HoglinSpecificSensor extends Sensor<Hoglin> {
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, new MemoryModuleType[0]);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public void doTick(ServerLevel serverLevel, Hoglin hoglin) {
        Brain<?> brain = hoglin.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, (Optional) findNearestRepellent(serverLevel, hoglin));
        Optional<Piglin> empty = Optional.empty();
        int i = 0;
        ArrayList newArrayList = Lists.newArrayList();
        for (LivingEntity livingEntity :  brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(Lists.newArrayList())) {
            if ((livingEntity instanceof Piglin) && !livingEntity.isBaby()) {
                i++;
                if (!empty.isPresent()) {
                    empty = Optional.of((Piglin) livingEntity);
                }
            }
            if ((livingEntity instanceof Hoglin) && !livingEntity.isBaby()) {
                newArrayList.add((Hoglin) livingEntity);
            }
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLIN, (Optional) empty);
        brain.setMemory( MemoryModuleType.NEAREST_VISIBLE_ADULT_HOGLINS,  newArrayList);
        brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, Integer.valueOf(i));
        brain.setMemory( MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, Integer.valueOf(newArrayList.size()));
    }

    private Optional<BlockPos> findNearestRepellent(ServerLevel serverLevel, Hoglin hoglin) {
        return BlockPos.findClosestMatch(hoglin.blockPosition(), 8, 4, blockPos -> {
            return serverLevel.getBlockState(blockPos).is(BlockTags.HOGLIN_REPELLENTS);
        });
    }
}
