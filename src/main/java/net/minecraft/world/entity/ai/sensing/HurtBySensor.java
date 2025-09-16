package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/HurtBySensor.class */
public class HurtBySensor extends Sensor<LivingEntity> {
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY);
    }

//    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
//    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
//        Brain<?> brain = livingEntity.getBrain();
//        DamageSource lastDamageSource = livingEntity.getLastDamageSource();
//        if (lastDamageSource != null) {
//            brain.setMemory(MemoryModuleType.HURT_BY,  livingEntity.getLastDamageSource());
//            Entity entity = lastDamageSource.getEntity();
//            if (entity instanceof LivingEntity) {
//                brain.setMemory(MemoryModuleType.HURT_BY_ENTITY,  entity);
//            }
//        } else {
//            brain.eraseMemory(MemoryModuleType.HURT_BY);
//        }
//        brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent(livingEntity2 -> {
//            if (!livingEntity2.isAlive() || livingEntity2.level != serverLevel) {
//                brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
//            }
//        });
//    }
@Override
protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
    Brain<?> brain = livingEntity.getBrain();
    DamageSource lastDamageSource = livingEntity.getLastDamageSource();

    if (lastDamageSource != null) {
        brain.setMemory(MemoryModuleType.HURT_BY, lastDamageSource);
        Entity entity = lastDamageSource.getEntity();

        // Старый стиль Java 8
        if (entity instanceof LivingEntity) {
            LivingEntity livingAttacker = (LivingEntity) entity;
            brain.setMemory(MemoryModuleType.HURT_BY_ENTITY, livingAttacker);
        }
    } else {
        brain.eraseMemory(MemoryModuleType.HURT_BY);
    }

    brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent(livingAttacker -> {
        if (!livingAttacker.isAlive() || livingAttacker.level != serverLevel) {
            brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
        }
    });
}
}
