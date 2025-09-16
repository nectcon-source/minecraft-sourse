package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/AdultSensor.class */
public class AdultSensor extends Sensor<AgableMob> {
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.VISIBLE_LIVING_ENTITIES);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public void doTick(ServerLevel serverLevel, AgableMob agableMob) {
        agableMob.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).ifPresent(list -> {
            setNearestVisibleAdult(agableMob, list);
        });
    }

    private void setNearestVisibleAdult(AgableMob agableMob, List<LivingEntity> list) {
        agableMob.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT, (Optional) list.stream().filter(livingEntity -> {
            return livingEntity.getType() == agableMob.getType();
        }).map(livingEntity2 -> {
            return (AgableMob) livingEntity2;
        }).filter(agableMob2 -> {
            return !agableMob2.isBaby();
        }).findFirst());
    }
}
