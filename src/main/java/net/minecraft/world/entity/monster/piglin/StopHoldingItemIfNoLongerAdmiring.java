package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.Items;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/piglin/StopHoldingItemIfNoLongerAdmiring.class */
public class StopHoldingItemIfNoLongerAdmiring<E extends Piglin> extends Behavior<E> {
    public StopHoldingItemIfNoLongerAdmiring() {
        super(ImmutableMap.of(MemoryModuleType.ADMIRING_ITEM, MemoryStatus.VALUE_ABSENT));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.behavior.Behavior
    public boolean checkExtraStartConditions(ServerLevel serverLevel, E e) {
        return (e.getOffhandItem().isEmpty() || e.getOffhandItem().getItem() == Items.SHIELD) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.behavior.Behavior
    public void start(ServerLevel serverLevel, E e, long j) {
        PiglinAi.stopHoldingOffHandItem(e, true);
    }
}
