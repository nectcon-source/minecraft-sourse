package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/NearestLivingEntitySensor.class */
public class NearestLivingEntitySensor extends Sensor<LivingEntity> {
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
        AABB var3 = livingEntity.getBoundingBox().inflate((double)16.0F, (double)16.0F, (double)16.0F);
        List<LivingEntity> var4 = serverLevel.getEntitiesOfClass(LivingEntity.class, var3, (var1x) -> var1x != livingEntity && var1x.isAlive());
        livingEntity.getClass();
        var4.sort(Comparator.comparingDouble(livingEntity::distanceToSqr));
        Brain<?> var5 = livingEntity.getBrain();
        var5.setMemory(MemoryModuleType.LIVING_ENTITIES, var4);
        var5.setMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES, var4.stream().filter((var1x) -> isEntityTargetable(livingEntity, var1x)).collect(Collectors.toList()));
    }

    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.LIVING_ENTITIES, MemoryModuleType.VISIBLE_LIVING_ENTITIES);
    }
}
