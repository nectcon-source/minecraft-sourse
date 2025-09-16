package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/SecondaryPoiSensor.class */
public class SecondaryPoiSensor extends Sensor<Villager> {
    public SecondaryPoiSensor() {
        super(40);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public void doTick(ServerLevel serverLevel, Villager villager) {
        ResourceKey<Level> dimension = serverLevel.dimension();
        BlockPos blockPosition = villager.blockPosition();
        ArrayList newArrayList = Lists.newArrayList();
        for (int i = -4; i <= 4; i++) {
            for (int i2 = -2; i2 <= 2; i2++) {
                for (int i3 = -4; i3 <= 4; i3++) {
                    BlockPos offset = blockPosition.offset(i, i2, i3);
                    if (villager.getVillagerData().getProfession().getSecondaryPoi().contains(serverLevel.getBlockState(offset).getBlock())) {
                        newArrayList.add(GlobalPos.of(dimension, offset));
                    }
                }
            }
        }
        Brain<?> brain = villager.getBrain();
        if (!newArrayList.isEmpty()) {
            brain.setMemory( MemoryModuleType.SECONDARY_JOB_SITE,  newArrayList);
        } else {
            brain.eraseMemory(MemoryModuleType.SECONDARY_JOB_SITE);
        }
    }

    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.SECONDARY_JOB_SITE);
    }
}
