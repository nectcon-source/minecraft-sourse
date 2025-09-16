package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/VillagerHostilesSensor.class */
public class VillagerHostilesSensor extends Sensor<LivingEntity> {
    private static final ImmutableMap<EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES = ImmutableMap.<EntityType<?>, Float>builder().put(EntityType.DROWNED, Float.valueOf(8.0f)).put(EntityType.EVOKER, Float.valueOf(12.0f)).put(EntityType.HUSK, Float.valueOf(8.0f)).put(EntityType.ILLUSIONER, Float.valueOf(12.0f)).put(EntityType.PILLAGER, Float.valueOf(15.0f)).put(EntityType.RAVAGER, Float.valueOf(12.0f)).put(EntityType.VEX, Float.valueOf(8.0f)).put(EntityType.VINDICATOR, Float.valueOf(10.0f)).put(EntityType.ZOGLIN, Float.valueOf(10.0f)).put(EntityType.ZOMBIE, Float.valueOf(8.0f)).put(EntityType.ZOMBIE_VILLAGER, Float.valueOf(8.0f)).build();

    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_HOSTILE);
    }

    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
        livingEntity.getBrain().setMemory(MemoryModuleType.NEAREST_HOSTILE, (Optional) getNearestHostile(livingEntity));
    }

    private Optional<LivingEntity> getNearestHostile(LivingEntity livingEntity) {
        return getVisibleEntities(livingEntity).flatMap(list -> {
            return list.stream().filter(this::isHostile).filter(livingEntity2 -> {
                return isClose(livingEntity, livingEntity2);
            }).min((livingEntity3, livingEntity4) -> {
                return compareMobDistance(livingEntity, livingEntity3, livingEntity4);
            });
        });
    }

    private Optional<List<LivingEntity>> getVisibleEntities(LivingEntity livingEntity) {
        return livingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
    }

    private int compareMobDistance(LivingEntity livingEntity, LivingEntity livingEntity2, LivingEntity livingEntity3) {
        return Mth.floor(livingEntity2.distanceToSqr(livingEntity) - livingEntity3.distanceToSqr(livingEntity));
    }

    private boolean isClose(LivingEntity livingEntity, LivingEntity livingEntity2) {
        float floatValue = ((Float) ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(livingEntity2.getType())).floatValue();
        return livingEntity2.distanceToSqr(livingEntity) <= ((double) (floatValue * floatValue));
    }

    private boolean isHostile(LivingEntity livingEntity) {
        return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(livingEntity.getType());
    }
}
