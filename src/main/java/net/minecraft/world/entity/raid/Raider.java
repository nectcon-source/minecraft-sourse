package net.minecraft.world.entity.raid;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PathfindToRaidGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/raid/Raider.class */
public abstract class Raider extends PatrollingMonster {
    protected static final EntityDataAccessor<Boolean> IS_CELEBRATING = SynchedEntityData.defineId(Raider.class, EntityDataSerializers.BOOLEAN);
    private static final Predicate<ItemEntity> ALLOWED_ITEMS = itemEntity -> {
        return !itemEntity.hasPickUpDelay() && itemEntity.isAlive() && ItemStack.matches(itemEntity.getItem(), Raid.getLeaderBannerInstance());
    };

    @Nullable
    protected Raid raid;
    private int wave;
    private boolean canJoinRaid;
    private int ticksOutsideRaid;

    public abstract void applyRaidBuffs(int i, boolean z);

    public abstract SoundEvent getCelebrateSound();

    protected Raider(EntityType<? extends Raider> entityType, Level level) {
        super(entityType, level);
    }

    @Override // net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new ObtainRaidLeaderBannerGoal(this));
        this.goalSelector.addGoal(3, new PathfindToRaidGoal(this));
        this.goalSelector.addGoal(4, new RaiderMoveThroughVillageGoal(this, 1.0499999523162842d, 1));
        this.goalSelector.addGoal(5, new RaiderCelebration(this));
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CELEBRATING, false);
    }

    public boolean canJoinRaid() {
        return this.canJoinRaid;
    }

    public void setCanJoinRaid(boolean z) {
        this.canJoinRaid = z;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        Raid raidAt;
        if ((this.level instanceof ServerLevel) && isAlive()) {
            Raid currentRaid = getCurrentRaid();
            if (canJoinRaid()) {
                if (currentRaid == null) {
                    if (this.level.getGameTime() % 20 == 0 && (raidAt = ((ServerLevel) this.level).getRaidAt(blockPosition())) != null && Raids.canJoinRaid(this, raidAt)) {
                        raidAt.joinRaid(raidAt.getGroupsSpawned(), this, null, true);
                    }
                } else {
                    LivingEntity target = getTarget();
                    if (target != null && (target.getType() == EntityType.PLAYER || target.getType() == EntityType.IRON_GOLEM)) {
                        this.noActionTime = 0;
                    }
                }
            }
        }
        super.aiStep();
    }

    @Override // net.minecraft.world.entity.monster.Monster
    protected void updateNoActionTime() {
        this.noActionTime += 2;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    public void die(DamageSource damageSource) {
        int i;
        if (this.level instanceof ServerLevel) {
            Entity entity = damageSource.getEntity();
            Raid currentRaid = getCurrentRaid();
            if (currentRaid != null) {
                if (isPatrolLeader()) {
                    currentRaid.removeLeader(getWave());
                }
                if (entity != null && entity.getType() == EntityType.PLAYER) {
                    currentRaid.addHeroOfTheVillage(entity);
                }
                currentRaid.removeFromRaid(this, false);
            }
            if (isPatrolLeader() && currentRaid == null && ((ServerLevel) this.level).getRaidAt(blockPosition()) == null) {
                ItemStack itemBySlot = getItemBySlot(EquipmentSlot.HEAD);
                Player player = null;
                if (entity instanceof Player) {
                    player = (Player) entity;
                } else if (entity instanceof Wolf) {
                    Wolf wolf = (Wolf) entity;
                    LivingEntity owner = wolf.getOwner();
                    if (wolf.isTame() && (owner instanceof Player)) {
                        player = (Player) owner;
                    }
                }
                if (!itemBySlot.isEmpty() && ItemStack.matches(itemBySlot, Raid.getLeaderBannerInstance()) && player != null) {
                    MobEffectInstance effect = player.getEffect(MobEffects.BAD_OMEN);
                    if (effect != null) {
                        i = 1 + effect.getAmplifier();
                        player.removeEffectNoUpdate(MobEffects.BAD_OMEN);
                    } else {
                        i = 1 - 1;
                    }
                    MobEffectInstance mobEffectInstance = new MobEffectInstance(MobEffects.BAD_OMEN, 120000, Mth.clamp(i, 0, 4), false, false, true);
                    if (!this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
                        player.addEffect(mobEffectInstance);
                    }
                }
            }
        }
        super.die(damageSource);
    }

    @Override // net.minecraft.world.entity.monster.PatrollingMonster
    public boolean canJoinPatrol() {
        return !hasActiveRaid();
    }

    public void setCurrentRaid(@Nullable Raid raid) {
        this.raid = raid;
    }

    @Nullable
    public Raid getCurrentRaid() {
        return this.raid;
    }

    public boolean hasActiveRaid() {
        return getCurrentRaid() != null && getCurrentRaid().isActive();
    }

    public void setWave(int i) {
        this.wave = i;
    }

    public int getWave() {
        return this.wave;
    }

    public boolean isCelebrating() {
        return ((Boolean) this.entityData.get(IS_CELEBRATING)).booleanValue();
    }

    public void setCelebrating(boolean z) {
        this.entityData.set(IS_CELEBRATING, Boolean.valueOf(z));
    }

    @Override // net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("Wave", this.wave);
        compoundTag.putBoolean("CanJoinRaid", this.canJoinRaid);
        if (this.raid != null) {
            compoundTag.putInt("RaidId", this.raid.getId());
        }
    }

    @Override // net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.wave = compoundTag.getInt("Wave");
        this.canJoinRaid = compoundTag.getBoolean("CanJoinRaid");
        if (compoundTag.contains("RaidId", 3)) {
            if (this.level instanceof ServerLevel) {
                this.raid = ((ServerLevel) this.level).getRaids().get(compoundTag.getInt("RaidId"));
            }
            if (this.raid != null) {
                this.raid.addWaveMob(this.wave, this, false);
                if (isPatrolLeader()) {
                    this.raid.setLeader(this.wave, this);
                }
            }
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack item = itemEntity.getItem();
        boolean z = hasActiveRaid() && getCurrentRaid().getLeader(getWave()) != null;
        if (hasActiveRaid() && !z && ItemStack.matches(item, Raid.getLeaderBannerInstance())) {
            EquipmentSlot equipmentSlot = EquipmentSlot.HEAD;
            ItemStack itemBySlot = getItemBySlot(equipmentSlot);
            double equipmentDropChance = getEquipmentDropChance(equipmentSlot);
            if (!itemBySlot.isEmpty() && Math.max(this.random.nextFloat() - 0.1f, 0.0f) < equipmentDropChance) {
                spawnAtLocation(itemBySlot);
            }
            onItemPickup(itemEntity);
            setItemSlot(equipmentSlot, item);
            take(itemEntity, item.getCount());
            itemEntity.remove();
            getCurrentRaid().setLeader(getWave(), this);
            setPatrolLeader(true);
            return;
        }
        super.pickUpItem(itemEntity);
    }

    @Override // net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    public boolean removeWhenFarAway(double d) {
        if (getCurrentRaid() == null) {
            return super.removeWhenFarAway(d);
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Mob
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || getCurrentRaid() != null;
    }

    public int getTicksOutsideRaid() {
        return this.ticksOutsideRaid;
    }

    public void setTicksOutsideRaid(int i) {
        this.ticksOutsideRaid = i;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (hasActiveRaid()) {
            getCurrentRaid().updateBossbar();
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        setCanJoinRaid((getType() == EntityType.WITCH && mobSpawnType == MobSpawnType.NATURAL) ? false : true);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/raid/Raider$ObtainRaidLeaderBannerGoal.class */
    public class ObtainRaidLeaderBannerGoal<T extends Raider> extends Goal {
        private final T mob;

        public ObtainRaidLeaderBannerGoal(T t) {
            this.mob = t;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            Raid currentRaid = this.mob.getCurrentRaid();
            if (!this.mob.hasActiveRaid() || this.mob.getCurrentRaid().isOver() || !this.mob.canBeLeader() || ItemStack.matches(this.mob.getItemBySlot(EquipmentSlot.HEAD), Raid.getLeaderBannerInstance())) {
                return false;
            }
            Raider leader = currentRaid.getLeader(this.mob.getWave());
            if (leader == null || !leader.isAlive()) {
                List<ItemEntity> entitiesOfClass = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(16.0d, 8.0d, 16.0d), Raider.ALLOWED_ITEMS);
                if (!entitiesOfClass.isEmpty()) {
                    return this.mob.getNavigation().moveTo(entitiesOfClass.get(0), 1.149999976158142d);
                }
                return false;
            }
            return false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (this.mob.getNavigation().getTargetPos().closerThan(this.mob.position(), 1.414d)) {
                List<ItemEntity> entitiesOfClass = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(4.0d, 4.0d, 4.0d), Raider.ALLOWED_ITEMS);
                if (!entitiesOfClass.isEmpty()) {
                    this.mob.pickUpItem(entitiesOfClass.get(0));
                }
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/raid/Raider$RaiderCelebration.class */
    public class RaiderCelebration extends Goal {
        private final Raider mob;

        RaiderCelebration(Raider raider) {
            this.mob = raider;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            Raid currentRaid = this.mob.getCurrentRaid();
            return this.mob.isAlive() && this.mob.getTarget() == null && currentRaid != null && currentRaid.isLoss();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.mob.setCelebrating(true);
            super.start();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            this.mob.setCelebrating(false);
            super.stop();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (!this.mob.isSilent() && this.mob.random.nextInt(100) == 0) {
                Raider.this.playSound(Raider.this.getCelebrateSound(), Raider.this.getSoundVolume(), Raider.this.getVoicePitch());
            }
            if (!this.mob.isPassenger() && this.mob.random.nextInt(50) == 0) {
                this.mob.getJumpControl().jump();
            }
            super.tick();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/raid/Raider$HoldGroundAttackGoal.class */
    public class HoldGroundAttackGoal extends Goal {
        private final Raider mob;
        private final float hostileRadiusSqr;
        public final TargetingConditions shoutTargeting = new TargetingConditions().range(8.0d).allowNonAttackable().allowInvulnerable().allowSameTeam().allowUnseeable().ignoreInvisibilityTesting();

        public HoldGroundAttackGoal(AbstractIllager abstractIllager, float f) {
            this.mob = abstractIllager;
            this.hostileRadiusSqr = f * f;
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            LivingEntity lastHurtByMob = this.mob.getLastHurtByMob();
            return this.mob.getCurrentRaid() == null && this.mob.isPatrolling() && this.mob.getTarget() != null && !this.mob.isAggressive() && (lastHurtByMob == null || lastHurtByMob.getType() != EntityType.PLAYER);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            super.start();
            this.mob.getNavigation().stop();
            Iterator<Raider> it = this.mob.level.getNearbyEntities(Raider.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0d, 8.0d, 8.0d)).iterator();
            while (it.hasNext()) {
                it.next().setTarget(this.mob.getTarget());
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            super.stop();
            LivingEntity target = this.mob.getTarget();
            if (target != null) {
                for (Raider raider : this.mob.level.getNearbyEntities(Raider.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0d, 8.0d, 8.0d))) {
                    raider.setTarget(target);
                    raider.setAggressive(true);
                }
                this.mob.setAggressive(true);
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return;
            }
            if (this.mob.distanceToSqr(target) > this.hostileRadiusSqr) {
                this.mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
                if (this.mob.random.nextInt(50) == 0) {
                    this.mob.playAmbientSound();
                }
            } else {
                this.mob.setAggressive(true);
            }
            super.tick();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/raid/Raider$RaiderMoveThroughVillageGoal.class */
    static class RaiderMoveThroughVillageGoal extends Goal {
        private final Raider raider;
        private final double speedModifier;
        private BlockPos poiPos;
        private final List<BlockPos> visited = Lists.newArrayList();
        private final int distanceToPoi;
        private boolean stuck;

        public RaiderMoveThroughVillageGoal(Raider raider, double d, int i) {
            this.raider = raider;
            this.speedModifier = d;
            this.distanceToPoi = i;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            updateVisited();
            return isValidRaid() && hasSuitablePoi() && this.raider.getTarget() == null;
        }

        private boolean isValidRaid() {
            return this.raider.hasActiveRaid() && !this.raider.getCurrentRaid().isOver();
        }

        private boolean hasSuitablePoi() {
            ServerLevel serverLevel = (ServerLevel) this.raider.level;
            Optional<BlockPos> random = serverLevel.getPoiManager().getRandom(poiType -> {
                return poiType == PoiType.HOME;
            }, this::hasNotVisited, PoiManager.Occupancy.ANY, this.raider.blockPosition(), 48, this.raider.random);
            if (!random.isPresent()) {
                return false;
            }
            this.poiPos = random.get().immutable();
            return true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return (this.raider.getNavigation().isDone() || this.raider.getTarget() != null || this.poiPos.closerThan(this.raider.position(), (double) (this.raider.getBbWidth() + ((float) this.distanceToPoi))) || this.stuck) ? false : true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            if (this.poiPos.closerThan(this.raider.position(), this.distanceToPoi)) {
                this.visited.add(this.poiPos);
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            super.start();
            this.raider.setNoActionTime(0);
            this.raider.getNavigation().moveTo(this.poiPos.getX(), this.poiPos.getY(), this.poiPos.getZ(), this.speedModifier);
            this.stuck = false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (this.raider.getNavigation().isDone()) {
                Vec3 atBottomCenterOf = Vec3.atBottomCenterOf(this.poiPos);
                Vec3 posTowards = RandomPos.getPosTowards(this.raider, 16, 7, atBottomCenterOf, 0.3141592741012573d);
                if (posTowards == null) {
                    posTowards = RandomPos.getPosTowards(this.raider, 8, 7, atBottomCenterOf);
                }
                if (posTowards == null) {
                    this.stuck = true;
                } else {
                    this.raider.getNavigation().moveTo(posTowards.x, posTowards.y, posTowards.z, this.speedModifier);
                }
            }
        }

        private boolean hasNotVisited(BlockPos blockPos) {
            Iterator<BlockPos> it = this.visited.iterator();
            while (it.hasNext()) {
                if (Objects.equals(blockPos, it.next())) {
                    return false;
                }
            }
            return true;
        }

        private void updateVisited() {
            if (this.visited.size() > 2) {
                this.visited.remove(0);
            }
        }
    }
}
