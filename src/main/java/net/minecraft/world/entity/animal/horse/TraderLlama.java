package net.minecraft.world.entity.animal.horse;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/TraderLlama.class */
public class TraderLlama extends Llama {
    private int despawnDelay;

    public TraderLlama(EntityType<? extends TraderLlama> entityType, Level level) {
        super(entityType, level);
        this.despawnDelay = 47999;
    }

    @Override // net.minecraft.world.entity.animal.horse.Llama
    public boolean isTraderLlama() {
        return true;
    }

    @Override // net.minecraft.world.entity.animal.horse.Llama
    protected Llama makeBabyLlama() {
        return EntityType.TRADER_LLAMA.create(this.level);
    }

    @Override // net.minecraft.world.entity.animal.horse.Llama, net.minecraft.world.entity.animal.horse.AbstractChestedHorse, net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("DespawnDelay", this.despawnDelay);
    }

    @Override // net.minecraft.world.entity.animal.horse.Llama, net.minecraft.world.entity.animal.horse.AbstractChestedHorse, net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("DespawnDelay", 99)) {
            this.despawnDelay = compoundTag.getInt("DespawnDelay");
        }
    }

    @Override // net.minecraft.world.entity.animal.horse.Llama, net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0d));
        this.targetSelector.addGoal(1, new TraderLlamaDefendWanderingTraderGoal(this));
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse
    protected void doPlayerRide(Player player) {
        if (getLeashHolder() instanceof WanderingTrader) {
            return;
        }
        super.doPlayerRide(player);
    }

    @Override // net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.animal.Animal, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            maybeDespawn();
        }
    }

    private void maybeDespawn() {
        if (!canDespawn()) {
            return;
        }
        this.despawnDelay = isLeashedToWanderingTrader() ? ((WanderingTrader) getLeashHolder()).getDespawnDelay() - 1 : this.despawnDelay - 1;
        if (this.despawnDelay <= 0) {
            dropLeash(true, false);
            remove();
        }
    }

    private boolean canDespawn() {
        return (isTamed() || isLeashedToSomethingOtherThanTheWanderingTrader() || hasOnePlayerPassenger()) ? false : true;
    }

    private boolean isLeashedToWanderingTrader() {
        return getLeashHolder() instanceof WanderingTrader;
    }

    private boolean isLeashedToSomethingOtherThanTheWanderingTrader() {
        return isLeashed() && !isLeashedToWanderingTrader();
    }

    @Override // net.minecraft.world.entity.animal.horse.Llama, net.minecraft.world.entity.animal.horse.AbstractHorse, net.minecraft.world.entity.AgableMob, net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        if (mobSpawnType == MobSpawnType.EVENT) {
            setAge(0);
        }
        if (spawnGroupData == null) {
            spawnGroupData = new AgableMob.AgableMobGroupData(false);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/animal/horse/TraderLlama$TraderLlamaDefendWanderingTraderGoal.class */
    public class TraderLlamaDefendWanderingTraderGoal extends TargetGoal {
        private final Llama llama;
        private LivingEntity ownerLastHurtBy;
        private int timestamp;

        public TraderLlamaDefendWanderingTraderGoal(Llama llama) {
            super(llama, false);
            this.llama = llama;
            setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (!this.llama.isLeashed()) {
                return false;
            }
            Entity leashHolder = this.llama.getLeashHolder();
            if (!(leashHolder instanceof WanderingTrader)) {
                return false;
            }
            WanderingTrader wanderingTrader = (WanderingTrader) leashHolder;
            this.ownerLastHurtBy = wanderingTrader.getLastHurtByMob();
            return wanderingTrader.getLastHurtByMobTimestamp() != this.timestamp && canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.mob.setTarget(this.ownerLastHurtBy);
            Entity leashHolder = this.llama.getLeashHolder();
            if (leashHolder instanceof WanderingTrader) {
                this.timestamp = ((WanderingTrader) leashHolder).getLastHurtByMobTimestamp();
            }
            super.start();
        }
    }
}
