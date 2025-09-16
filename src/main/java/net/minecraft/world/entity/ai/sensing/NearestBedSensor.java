package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/sensing/NearestBedSensor.class */
public class NearestBedSensor extends Sensor<Mob> {
    private final Long2LongMap batchCache;
    private int triedCount;
    private long lastUpdate;

    public NearestBedSensor() {
        super(20);
        this.batchCache = new Long2LongOpenHashMap();
    }

    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.NEAREST_BED);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.entity.p000ai.sensing.Sensor
    public void doTick(ServerLevel serverLevel, Mob mob) {
        if (!mob.isBaby()) {
            return;
        }
        this.triedCount = 0;
        this.lastUpdate = serverLevel.getGameTime() + serverLevel.getRandom().nextInt(20);
        PoiManager poiManager = serverLevel.getPoiManager();
        Path createPath = mob.getNavigation().createPath(poiManager.findAll(PoiType.HOME.getPredicate(), blockPos -> {
            long asLong = blockPos.asLong();
            if (this.batchCache.containsKey(asLong)) {
                return false;
            }
            int i = this.triedCount + 1;
            this.triedCount = i;
            if (i >= 5) {
                return false;
            }
            this.batchCache.put(asLong, this.lastUpdate + 40);
            return true;
        }, mob.blockPosition(), 48, PoiManager.Occupancy.ANY), PoiType.HOME.getValidRange());
        if (createPath == null || !createPath.canReach()) {
            if (this.triedCount < 5) {
                this.batchCache.long2LongEntrySet().removeIf(entry -> {
                    return entry.getLongValue() < this.lastUpdate;
                });
            }
        } else {
            BlockPos target = createPath.getTarget();
            if (poiManager.getType(target).isPresent()) {
                mob.getBrain().setMemory( MemoryModuleType.NEAREST_BED, target);
            }
        }
    }
}
