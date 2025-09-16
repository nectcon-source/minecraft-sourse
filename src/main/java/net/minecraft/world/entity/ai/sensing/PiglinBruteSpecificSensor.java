package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/PiglinBruteSpecificSensor.class */
public class PiglinBruteSpecificSensor extends Sensor<LivingEntity> {
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEARBY_ADULT_PIGLINS);
    }

    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
        Brain<?> brain = livingEntity.getBrain();
        Optional<Mob> empty = Optional.empty();
        ArrayList newArrayList = Lists.newArrayList();
        for (LivingEntity livingEntity2 :  brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(ImmutableList.of())) {
            if ((livingEntity2 instanceof WitherSkeleton) || (livingEntity2 instanceof WitherBoss)) {
                empty = Optional.of((Mob) livingEntity2);
                break;
            }
        }
        for (LivingEntity livingEntity3 :  brain.getMemory(MemoryModuleType.LIVING_ENTITIES).orElse(ImmutableList.of())) {
            if ((livingEntity3 instanceof AbstractPiglin) && ((AbstractPiglin) livingEntity3).isAdult()) {
                newArrayList.add((AbstractPiglin) livingEntity3);
            }
        }
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, (Optional) empty);
        brain.setMemory( MemoryModuleType.NEARBY_ADULT_PIGLINS,  newArrayList);
    }
}
