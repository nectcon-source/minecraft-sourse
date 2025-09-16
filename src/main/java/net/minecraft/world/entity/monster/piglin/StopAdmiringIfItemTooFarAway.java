package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/piglin/StopAdmiringIfItemTooFarAway.class */
public class StopAdmiringIfItemTooFarAway<E extends Piglin> extends Behavior<E> {
    private final int maxDistanceToItem;

    public StopAdmiringIfItemTooFarAway(int i) {
        super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryStatus.REGISTERED));
        this.maxDistanceToItem = i;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.behavior.Behavior
    public boolean checkExtraStartConditions(ServerLevel serverLevel, E e) {
        if (!e.getOffhandItem().isEmpty()) {
            return false;
        } else {
            Optional<ItemEntity> var3 = e.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
            if (!var3.isPresent()) {
                return true;
            } else {
                return !((ItemEntity) var3.get()).closerThan(e, (double) this.maxDistanceToItem);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.behavior.Behavior
    public void start(ServerLevel serverLevel, E e, long j) {
        e.getBrain().eraseMemory(MemoryModuleType.ADMIRING_ITEM);
    }
}
