package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/piglin/StopAdmiringIfTiredOfTryingToReachItem.class */
public class StopAdmiringIfTiredOfTryingToReachItem<E extends Piglin> extends Behavior<E> {
    private final int maxTimeToReachItem;
    private final int disableTime;

    public StopAdmiringIfTiredOfTryingToReachItem(int i, int i2) {
        super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.VALUE_PRESENT, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, MemoryStatus.REGISTERED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryStatus.REGISTERED));
        this.maxTimeToReachItem = i;
        this.disableTime = i2;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.behavior.Behavior
    public boolean checkExtraStartConditions(ServerLevel serverLevel, E e) {
        return e.getOffhandItem().isEmpty();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.behavior.Behavior
    public void start(ServerLevel serverLevel, E e, long j) {
        Brain<Piglin> var5 = e.getBrain();
        Optional<Integer> var6 = var5.getMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
        if (!var6.isPresent()) {
            var5.setMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, 0);
        } else {
            int var7 = (Integer)var6.get();
            if (var7 > this.maxTimeToReachItem) {
                var5.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
                var5.eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
                var5.setMemoryWithExpiry(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, true, (long)this.disableTime);
            } else {
                var5.setMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, var7 + 1);
            }
        }
    }
}
