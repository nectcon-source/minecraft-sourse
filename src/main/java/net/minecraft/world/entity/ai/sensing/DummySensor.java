package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/DummySensor.class */
public class DummySensor extends Sensor<LivingEntity> {
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
    }

    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of();
    }
}
