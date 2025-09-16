package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/BaseSpawner.class */
public abstract class BaseSpawner {
    private static final Logger LOGGER = LogManager.getLogger();
    private double spin;
    private double oSpin;

    @Nullable
    private Entity displayEntity;
    private int spawnDelay = 20;
    private final List<SpawnData> spawnPotentials = Lists.newArrayList();
    private SpawnData nextSpawnData = new SpawnData();
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;

    public abstract void broadcastEvent(int i);

    public abstract Level getLevel();

    public abstract BlockPos getPos();

    @Nullable
    private ResourceLocation getEntityId() {
        String string = this.nextSpawnData.getTag().getString("id");
        try {
            if (StringUtil.isNullOrEmpty(string)) {
                return null;
            }
            return new ResourceLocation(string);
        } catch (ResourceLocationException e) {
            BlockPos pos = getPos();
            LOGGER.warn("Invalid entity id '{}' at spawner {}:[{},{},{}]", string, getLevel().dimension().location(), Integer.valueOf(pos.getX()), Integer.valueOf(pos.getY()), Integer.valueOf(pos.getZ()));
            return null;
        }
    }

    public void setEntityId(EntityType<?> entityType) {
        this.nextSpawnData.getTag().putString("id", Registry.ENTITY_TYPE.getKey(entityType).toString());
    }

    private boolean isNearPlayer() {
        BlockPos pos = getPos();
        return getLevel().hasNearbyAlivePlayer(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, this.requiredPlayerRange);
    }

    public void tick() {
        if (!isNearPlayer()) {
            this.oSpin = this.spin;
            return;
        }
        Level level = getLevel();
        BlockPos pos = getPos();
        if (!(level instanceof ServerLevel)) {
            double x = pos.getX() + level.random.nextDouble();
            double y = pos.getY() + level.random.nextDouble();
            double z = pos.getZ() + level.random.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0d, 0.0d, 0.0d);
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0d, 0.0d, 0.0d);
            if (this.spawnDelay > 0) {
                this.spawnDelay--;
            }
            this.oSpin = this.spin;
            this.spin = (this.spin + (1000.0f / (this.spawnDelay + 200.0f))) % 360.0d;
            return;
        }
        if (this.spawnDelay == -1) {
            delay();
        }
        if (this.spawnDelay > 0) {
            this.spawnDelay--;
            return;
        }
        boolean z2 = false;
        for (int i = 0; i < this.spawnCount; i++) {
            CompoundTag tag = this.nextSpawnData.getTag();
            Optional<EntityType<?>> m59by = EntityType.by(tag);
            if (!m59by.isPresent()) {
                delay();
                return;
            }
            ListTag list = tag.getList("Pos", 6);
            int size = list.size();
            double d = size >= 1 ? list.getDouble(0) : pos.getX() + ((level.random.nextDouble() - level.random.nextDouble()) * this.spawnRange) + 0.5d;
            double d2 = size >= 2 ? list.getDouble(1) : (pos.getY() + level.random.nextInt(3)) - 1;
            double d3 = size >= 3 ? list.getDouble(2) : pos.getZ() + ((level.random.nextDouble() - level.random.nextDouble()) * this.spawnRange) + 0.5d;
            if (level.noCollision(m59by.get().getAABB(d, d2, d3))) {
                ServerLevel serverLevel = (ServerLevel) level;
                if (SpawnPlacements.checkSpawnRules(m59by.get(), serverLevel, MobSpawnType.SPAWNER, new BlockPos(d, d2, d3), level.getRandom())) {
                    Entity loadEntityRecursive = EntityType.loadEntityRecursive(tag, level, entity -> {
                        entity.moveTo(d, d2, d3, entity.yRot, entity.xRot);
                        return entity;
                    });
                    if (loadEntityRecursive == null) {
                        delay();
                        return;
                    }
                    if (level.getEntitiesOfClass(loadEntityRecursive.getClass(), new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).inflate(this.spawnRange)).size() >= this.maxNearbyEntities) {
                        delay();
                        return;
                    }
                    loadEntityRecursive.moveTo(loadEntityRecursive.getX(), loadEntityRecursive.getY(), loadEntityRecursive.getZ(), level.random.nextFloat() * 360.0f, 0.0f);
                    if (loadEntityRecursive instanceof Mob) {
                        Mob mob = (Mob) loadEntityRecursive;
                        if (mob.checkSpawnRules(level, MobSpawnType.SPAWNER) && mob.checkSpawnObstruction(level)) {
                            if (this.nextSpawnData.getTag().size() == 1 && this.nextSpawnData.getTag().contains("id", 8)) {
                                ((Mob) loadEntityRecursive).finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(loadEntityRecursive.blockPosition()), MobSpawnType.SPAWNER, null, null);
                            }
                        }
                    }
                    if (!serverLevel.tryAddFreshEntityWithPassengers(loadEntityRecursive)) {
                        delay();
                        return;
                    }
                    level.levelEvent(2004, pos, 0);
                    if (loadEntityRecursive instanceof Mob) {
                        ((Mob) loadEntityRecursive).spawnAnim();
                    }
                    z2 = true;
                } else {
                    continue;
                }
            }
        }
        if (z2) {
            delay();
        }
    }

    private void delay() {
        if (this.maxSpawnDelay <= this.minSpawnDelay) {
            this.spawnDelay = this.minSpawnDelay;
        } else {
            this.spawnDelay = this.minSpawnDelay + getLevel().random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        }
        if (!this.spawnPotentials.isEmpty()) {
            setNextSpawnData((SpawnData) WeighedRandom.getRandomItem(getLevel().random, this.spawnPotentials));
        }
        broadcastEvent(1);
    }

    public void load(CompoundTag compoundTag) {
        this.spawnDelay = compoundTag.getShort("Delay");
        this.spawnPotentials.clear();
        if (compoundTag.contains("SpawnPotentials", 9)) {
            ListTag list = compoundTag.getList("SpawnPotentials", 10);
            for (int i = 0; i < list.size(); i++) {
                this.spawnPotentials.add(new SpawnData(list.getCompound(i)));
            }
        }
        if (compoundTag.contains("SpawnData", 10)) {
            setNextSpawnData(new SpawnData(1, compoundTag.getCompound("SpawnData")));
        } else if (!this.spawnPotentials.isEmpty()) {
            setNextSpawnData((SpawnData) WeighedRandom.getRandomItem(getLevel().random, this.spawnPotentials));
        }
        if (compoundTag.contains("MinSpawnDelay", 99)) {
            this.minSpawnDelay = compoundTag.getShort("MinSpawnDelay");
            this.maxSpawnDelay = compoundTag.getShort("MaxSpawnDelay");
            this.spawnCount = compoundTag.getShort("SpawnCount");
        }
        if (compoundTag.contains("MaxNearbyEntities", 99)) {
            this.maxNearbyEntities = compoundTag.getShort("MaxNearbyEntities");
            this.requiredPlayerRange = compoundTag.getShort("RequiredPlayerRange");
        }
        if (compoundTag.contains("SpawnRange", 99)) {
            this.spawnRange = compoundTag.getShort("SpawnRange");
        }
        if (getLevel() != null) {
            this.displayEntity = null;
        }
    }

    public CompoundTag save(CompoundTag compoundTag) {
        if (getEntityId() == null) {
            return compoundTag;
        }
        compoundTag.putShort("Delay", (short) this.spawnDelay);
        compoundTag.putShort("MinSpawnDelay", (short) this.minSpawnDelay);
        compoundTag.putShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
        compoundTag.putShort("SpawnCount", (short) this.spawnCount);
        compoundTag.putShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
        compoundTag.putShort("RequiredPlayerRange", (short) this.requiredPlayerRange);
        compoundTag.putShort("SpawnRange", (short) this.spawnRange);
        compoundTag.put("SpawnData", this.nextSpawnData.getTag().copy());
        ListTag listTag = new ListTag();
        if (this.spawnPotentials.isEmpty()) {
            listTag.add(this.nextSpawnData.save());
        } else {
            Iterator<SpawnData> it = this.spawnPotentials.iterator();
            while (it.hasNext()) {
                listTag.add(it.next().save());
            }
        }
        compoundTag.put("SpawnPotentials", listTag);
        return compoundTag;
    }

    @Nullable
    public Entity getOrCreateDisplayEntity() {
        if (this.displayEntity == null) {
            this.displayEntity = EntityType.loadEntityRecursive(this.nextSpawnData.getTag(), getLevel(), Function.identity());
            if (this.nextSpawnData.getTag().size() != 1 || !this.nextSpawnData.getTag().contains("id", 8) || (this.displayEntity instanceof Mob)) {
            }
        }
        return this.displayEntity;
    }

    public boolean onEventTriggered(int i) {
        if (i == 1 && getLevel().isClientSide) {
            this.spawnDelay = this.minSpawnDelay;
            return true;
        }
        return false;
    }

    public void setNextSpawnData(SpawnData spawnData) {
        this.nextSpawnData = spawnData;
    }

    public double getSpin() {
        return this.spin;
    }

    public double getoSpin() {
        return this.oSpin;
    }
}
