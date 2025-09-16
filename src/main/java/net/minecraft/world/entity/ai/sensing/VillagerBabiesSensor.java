package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/VillagerBabiesSensor.class */
public class VillagerBabiesSensor extends Sensor<LivingEntity> {
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
    }

    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    protected void doTick(ServerLevel serverLevel, LivingEntity livingEntity) {
        livingEntity.getBrain().setMemory( MemoryModuleType.VISIBLE_VILLAGER_BABIES,  getNearestVillagerBabies(livingEntity));
    }

    private List<LivingEntity> getNearestVillagerBabies(LivingEntity livingEntity) {
        return (List) getVisibleEntities(livingEntity).stream().filter(this::isVillagerBaby).collect(Collectors.toList());
    }

    private boolean isVillagerBaby(LivingEntity livingEntity) {
        return livingEntity.getType() == EntityType.VILLAGER && livingEntity.isBaby();
    }

    private List<LivingEntity> getVisibleEntities(LivingEntity livingEntity) {
        return (List) livingEntity.getBrain().getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES).orElse(Lists.newArrayList());
    }
}
