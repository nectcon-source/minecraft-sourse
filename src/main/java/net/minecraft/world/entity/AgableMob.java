package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/AgableMob.class */
public abstract class AgableMob extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(AgableMob.class, EntityDataSerializers.BOOLEAN);
    protected int age;
    protected int forcedAge;
    protected int forcedAgeTimer;

    @Nullable
    public abstract AgableMob getBreedOffspring(ServerLevel serverLevel, AgableMob agableMob);

    protected AgableMob(EntityType<? extends AgableMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.Mob
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        if (spawnGroupData == null) {
            spawnGroupData = new AgableMobGroupData(true);
        }
        AgableMobGroupData agableMobGroupData = (AgableMobGroupData) spawnGroupData;
        if (agableMobGroupData.isShouldSpawnBaby() && agableMobGroupData.getGroupSize() > 0 && this.random.nextFloat() <= agableMobGroupData.getBabySpawnChance()) {
            setAge(-24000);
        }
        agableMobGroupData.increaseGroupSizeByOne();
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BABY_ID, false);
    }

    public boolean canBreed() {
        return false;
    }

    public int getAge() {
        if (this.level.isClientSide) {
            return ((Boolean) this.entityData.get(DATA_BABY_ID)).booleanValue() ? -1 : 1;
        }
        return this.age;
    }

    public void ageUp(int i, boolean z) {
        int age = getAge();
        int i2 = age + (i * 20);
        if (i2 > 0) {
            i2 = 0;
        }
        int i3 = i2 - age;
        setAge(i2);
        if (z) {
            this.forcedAge += i3;
            if (this.forcedAgeTimer == 0) {
                this.forcedAgeTimer = 40;
            }
        }
        if (getAge() == 0) {
            setAge(this.forcedAge);
        }
    }

    public void ageUp(int i) {
        ageUp(i, false);
    }

    public void setAge(int i) {
        int i2 = this.age;
        this.age = i;
        if ((i2 < 0 && i >= 0) || (i2 >= 0 && i < 0)) {
            this.entityData.set(DATA_BABY_ID, Boolean.valueOf(i < 0));
            ageBoundaryReached();
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Age", getAge());
        compoundTag.putInt("ForcedAge", this.forcedAge);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        setAge(compoundTag.getInt("Age"));
        this.forcedAge = compoundTag.getInt("ForcedAge");
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_BABY_ID.equals(entityDataAccessor)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        if (this.level.isClientSide) {
            if (this.forcedAgeTimer > 0) {
                if (this.forcedAgeTimer % 4 == 0) {
                    this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, getRandomX(1.0d), getRandomY() + 0.5d, getRandomZ(1.0d), 0.0d, 0.0d, 0.0d);
                }
                this.forcedAgeTimer--;
                return;
            }
            return;
        }
        if (isAlive()) {
            int age = getAge();
            if (age < 0) {
                setAge(age + 1);
            } else if (age > 0) {
                setAge(age - 1);
            }
        }
    }

    protected void ageBoundaryReached() {
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public boolean isBaby() {
        return getAge() < 0;
    }

    @Override // net.minecraft.world.entity.Mob
    public void setBaby(boolean z) {
        setAge(z ? -24000 : 0);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/AgableMob$AgableMobGroupData.class */
    public static class AgableMobGroupData implements SpawnGroupData {
        private int groupSize;
        private final boolean shouldSpawnBaby;
        private final float babySpawnChance;

        private AgableMobGroupData(boolean z, float f) {
            this.shouldSpawnBaby = z;
            this.babySpawnChance = f;
        }

        public AgableMobGroupData(boolean z) {
            this(z, 0.05f);
        }

        public AgableMobGroupData(float f) {
            this(true, f);
        }

        public int getGroupSize() {
            return this.groupSize;
        }

        public void increaseGroupSizeByOne() {
            this.groupSize++;
        }

        public boolean isShouldSpawnBaby() {
            return this.shouldSpawnBaby;
        }

        public float getBabySpawnChance() {
            return this.babySpawnChance;
        }
    }
}
