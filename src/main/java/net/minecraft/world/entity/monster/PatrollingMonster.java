package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/PatrollingMonster.class */
public abstract class PatrollingMonster extends Monster {
    private BlockPos patrolTarget;
    private boolean patrolLeader;
    private boolean patrolling;

    protected PatrollingMonster(EntityType<? extends PatrollingMonster> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(4, new LongDistancePatrolGoal(this, 0.7d, 0.595d));
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.patrolTarget != null) {
            compoundTag.put("PatrolTarget", NbtUtils.writeBlockPos(this.patrolTarget));
        }
        compoundTag.putBoolean("PatrolLeader", this.patrolLeader);
        compoundTag.putBoolean("Patrolling", this.patrolling);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("PatrolTarget")) {
            this.patrolTarget = NbtUtils.readBlockPos(compoundTag.getCompound("PatrolTarget"));
        }
        this.patrolLeader = compoundTag.getBoolean("PatrolLeader");
        this.patrolling = compoundTag.getBoolean("Patrolling");
    }

    @Override // net.minecraft.world.entity.Entity
    public double getMyRidingOffset() {
        return -0.45d;
    }

    public boolean canBeLeader() {
        return true;
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        if (mobSpawnType != MobSpawnType.PATROL && mobSpawnType != MobSpawnType.EVENT && mobSpawnType != MobSpawnType.STRUCTURE && this.random.nextFloat() < 0.06f && canBeLeader()) {
            this.patrolLeader = true;
        }
        if (isPatrolLeader()) {
            setItemSlot(EquipmentSlot.HEAD, Raid.getLeaderBannerInstance());
            setDropChance(EquipmentSlot.HEAD, 2.0f);
        }
        if (mobSpawnType == MobSpawnType.PATROL) {
            this.patrolling = true;
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    public static boolean checkPatrollingMonsterSpawnRules(EntityType<? extends PatrollingMonster> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random) {
        if (levelAccessor.getBrightness(LightLayer.BLOCK, blockPos) > 8) {
            return false;
        }
        return checkAnyLightMonsterSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, random);
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean removeWhenFarAway(double d) {
        return !this.patrolling || d > 16384.0d;
    }

    public void setPatrolTarget(BlockPos blockPos) {
        this.patrolTarget = blockPos;
        this.patrolling = true;
    }

    public BlockPos getPatrolTarget() {
        return this.patrolTarget;
    }

    public boolean hasPatrolTarget() {
        return this.patrolTarget != null;
    }

    public void setPatrolLeader(boolean z) {
        this.patrolLeader = z;
        this.patrolling = true;
    }

    public boolean isPatrolLeader() {
        return this.patrolLeader;
    }

    public boolean canJoinPatrol() {
        return true;
    }

    public void findPatrolTarget() {
        this.patrolTarget = blockPosition().offset((-500) + this.random.nextInt(1000), 0, (-500) + this.random.nextInt(1000));
        this.patrolling = true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isPatrolling() {
        return this.patrolling;
    }

    protected void setPatrolling(boolean z) {
        this.patrolling = z;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/PatrollingMonster$LongDistancePatrolGoal.class */
    public static class LongDistancePatrolGoal<T extends PatrollingMonster> extends Goal {
        private final T mob;
        private final double speedModifier;
        private final double leaderSpeedModifier;
        private long cooldownUntil = -1;

        public LongDistancePatrolGoal(T t, double d, double d2) {
            this.mob = t;
            this.speedModifier = d;
            this.leaderSpeedModifier = d2;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return this.mob.isPatrolling() && this.mob.getTarget() == null && !this.mob.isVehicle() && this.mob.hasPatrolTarget() && !((this.mob.level.getGameTime() > this.cooldownUntil ? 1 : (this.mob.level.getGameTime() == this.cooldownUntil ? 0 : -1)) < 0);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            boolean isPatrolLeader = this.mob.isPatrolLeader();
            PathNavigation navigation = this.mob.getNavigation();
            if (navigation.isDone()) {
                List<PatrollingMonster> findPatrolCompanions = findPatrolCompanions();
                if (this.mob.isPatrolling() && findPatrolCompanions.isEmpty()) {
                    this.mob.setPatrolling(false);
                    return;
                }
                if (!isPatrolLeader || !this.mob.getPatrolTarget().closerThan(this.mob.position(), 10.0d)) {
                    Vec3 atBottomCenterOf = Vec3.atBottomCenterOf(this.mob.getPatrolTarget());
                    Vec3 position = this.mob.position();
                    BlockPos heightmapPos = this.mob.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(position.subtract(atBottomCenterOf).yRot(90.0f).scale(0.4d).add(atBottomCenterOf).subtract(position).normalize().scale(10.0d).add(position)));
                    if (!navigation.moveTo(heightmapPos.getX(), heightmapPos.getY(), heightmapPos.getZ(), isPatrolLeader ? this.leaderSpeedModifier : this.speedModifier)) {
                        moveRandomly();
                        this.cooldownUntil = this.mob.level.getGameTime() + 200;
                        return;
                    } else {
                        if (isPatrolLeader) {
                            Iterator<PatrollingMonster> it = findPatrolCompanions.iterator();
                            while (it.hasNext()) {
                                it.next().setPatrolTarget(heightmapPos);
                            }
                            return;
                        }
                        return;
                    }
                }
                this.mob.findPatrolTarget();
            }
        }

        private List<PatrollingMonster> findPatrolCompanions() {
            return this.mob.level.getEntitiesOfClass(PatrollingMonster.class, this.mob.getBoundingBox().inflate(16.0d), patrollingMonster -> {
                return patrollingMonster.canJoinPatrol() && !patrollingMonster.is(this.mob);
            });
        }

        private boolean moveRandomly() {
            Random random = this.mob.getRandom();
            BlockPos heightmapPos = this.mob.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.mob.blockPosition().offset((-8) + random.nextInt(16), 0, (-8) + random.nextInt(16)));
            return this.mob.getNavigation().moveTo(heightmapPos.getX(), heightmapPos.getY(), heightmapPos.getZ(), this.speedModifier);
        }
    }
}
