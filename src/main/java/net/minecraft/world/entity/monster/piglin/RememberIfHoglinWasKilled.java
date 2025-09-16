package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/piglin/RememberIfHoglinWasKilled.class */
public class RememberIfHoglinWasKilled<E extends Piglin> extends Behavior<E> {
    public RememberIfHoglinWasKilled() {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.HUNTED_RECENTLY, MemoryStatus.REGISTERED));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.behavior.Behavior
    public void start(ServerLevel serverLevel, E e, long j) {
        if (isAttackTargetDeadHoglin(e)) {
            PiglinAi.dontKillAnyMoreHoglinsForAWhile(e);
        }
    }

    private boolean isAttackTargetDeadHoglin(E e) {
        LivingEntity livingEntity = (LivingEntity) e.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
        return livingEntity.getType() == EntityType.HOGLIN && livingEntity.isDeadOrDying();
    }
}
