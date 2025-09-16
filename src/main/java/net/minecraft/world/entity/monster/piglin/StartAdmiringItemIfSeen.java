package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/piglin/StartAdmiringItemIfSeen.class */
public class StartAdmiringItemIfSeen<E extends Piglin> extends Behavior<E> {
    private final int admireDuration;

    public StartAdmiringItemIfSeen(int i) {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT, MemoryModuleType.ADMIRING_DISABLED, MemoryStatus.VALUE_ABSENT, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryStatus.VALUE_ABSENT));
        this.admireDuration = i;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.behavior.Behavior
    public boolean checkExtraStartConditions(ServerLevel serverLevel, E e) {
        return PiglinAi.isLovedItem(((ItemEntity) e.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get()).getItem().getItem());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.behavior.Behavior
    public void start(ServerLevel serverLevel, E e, long j) {
        e.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, this.admireDuration);
    }
}
