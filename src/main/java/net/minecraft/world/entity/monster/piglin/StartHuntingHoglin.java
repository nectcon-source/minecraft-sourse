package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/piglin/StartHuntingHoglin.class */
public class StartHuntingHoglin<E extends Piglin> extends Behavior<E> {
    public StartHuntingHoglin() {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryStatus.VALUE_PRESENT, MemoryModuleType.ANGRY_AT, MemoryStatus.VALUE_ABSENT, MemoryModuleType.HUNTED_RECENTLY, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryStatus.REGISTERED));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.behavior.Behavior
    public boolean checkExtraStartConditions(ServerLevel serverLevel, Piglin piglin) {
        return (piglin.isBaby() || PiglinAi.hasAnyoneNearbyHuntedRecently(piglin)) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.behavior.Behavior
    public void start(ServerLevel serverLevel, E e, long j) {
        Hoglin hoglin = (Hoglin) e.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN).get();
        PiglinAi.setAngerTarget(e, hoglin);
        PiglinAi.dontKillAnyMoreHoglinsForAWhile(e);
        PiglinAi.broadcastAngerTarget(e, hoglin);
        PiglinAi.broadcastDontKillAnyMoreHoglinsForAWhile(e);
    }
}
