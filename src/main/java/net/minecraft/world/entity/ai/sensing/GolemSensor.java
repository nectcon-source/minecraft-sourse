package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/GolemSensor.class */
public class GolemSensor extends Sensor<LivingEntity> {
    public GolemSensor() {
        this(200);
    }

    public GolemSensor(int i) {
        super(i);
    }

    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
        checkForNearbyGolem(livingEntity);
    }

    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.LIVING_ENTITIES);
    }

    public static void checkForNearbyGolem(LivingEntity livingEntity) {
        Optional<List<LivingEntity>> var1 = livingEntity.getBrain().getMemory(MemoryModuleType.LIVING_ENTITIES);
        if (var1.isPresent()) {
            boolean var2 = (var1.get()).stream().anyMatch((var0x) -> var0x.getType().equals(EntityType.IRON_GOLEM));
            if (var2) {
                golemDetected(livingEntity);
            }

        }
    }

    public static void golemDetected(LivingEntity livingEntity) {
        livingEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.GOLEM_DETECTED_RECENTLY, true, 600L);
    }
}
