package net.minecraft.world.entity.monster;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Evoker.class */
public class Evoker extends SpellcasterIllager {
    private Sheep wololoTarget;

    public Evoker(EntityType<? extends Evoker> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
    }

    @Override // net.minecraft.world.entity.monster.AbstractIllager, net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EvokerCastingSpellGoal());
        this.goalSelector.addGoal(2, new AvoidEntityGoal(this, Player.class, 8.0f, 0.6d, 1.0d));
        this.goalSelector.addGoal(4, new EvokerSummonSpellGoal());
        this.goalSelector.addGoal(5, new EvokerAttackSpellGoal());
        this.goalSelector.addGoal(6, new EvokerWololoSpellGoal());
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6d));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.5d).add(Attributes.FOLLOW_RANGE, 12.0d).add(Attributes.MAX_HEALTH, 24.0d);
    }

    @Override // net.minecraft.world.entity.monster.SpellcasterIllager, net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override // net.minecraft.world.entity.monster.SpellcasterIllager, net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
    }

    @Override // net.minecraft.world.entity.raid.Raider
    public SoundEvent getCelebrateSound() {
        return SoundEvents.EVOKER_CELEBRATE;
    }

    @Override // net.minecraft.world.entity.monster.SpellcasterIllager, net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
    }

    @Override // net.minecraft.world.entity.monster.SpellcasterIllager, net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        super.customServerAiStep();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isAlliedTo(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity == this || super.isAlliedTo(entity)) {
            return true;
        }
        if (entity instanceof Vex) {
            return isAlliedTo(((Vex) entity).getOwner());
        }
        return (entity instanceof LivingEntity) && ((LivingEntity) entity).getMobType() == MobType.ILLAGER && getTeam() == null && entity.getTeam() == null;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.EVOKER_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.EVOKER_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.EVOKER_HURT;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setWololoTarget(@Nullable Sheep sheep) {
        this.wololoTarget = sheep;
    }

    /* JADX INFO: Access modifiers changed from: private */
    @Nullable
    public Sheep getWololoTarget() {
        return this.wololoTarget;
    }

    @Override // net.minecraft.world.entity.monster.SpellcasterIllager
    protected SoundEvent getCastingSoundEvent() {
        return SoundEvents.EVOKER_CAST_SPELL;
    }

    @Override // net.minecraft.world.entity.raid.Raider
    public void applyRaidBuffs(int i, boolean z) {
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Evoker$EvokerCastingSpellGoal.class */
    class EvokerCastingSpellGoal extends SpellcasterIllager.SpellcasterCastingSpellGoal {
        private EvokerCastingSpellGoal() {
            super();
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterCastingSpellGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (Evoker.this.getTarget() == null) {
                if (Evoker.this.getWololoTarget() != null) {
                    Evoker.this.getLookControl().setLookAt(Evoker.this.getWololoTarget(), Evoker.this.getMaxHeadYRot(), Evoker.this.getMaxHeadXRot());
                    return;
                }
                return;
            }
            Evoker.this.getLookControl().setLookAt(Evoker.this.getTarget(), Evoker.this.getMaxHeadYRot(), Evoker.this.getMaxHeadXRot());
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Evoker$EvokerAttackSpellGoal.class */
    class EvokerAttackSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private EvokerAttackSpellGoal() {
            super();
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected int getCastingTime() {
            return 40;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected int getCastingInterval() {
            return 100;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected void performSpellCasting() {
            LivingEntity var1 = Evoker.this.getTarget();
            double var2x = Math.min(var1.getY(), Evoker.this.getY());
            double var4xx = Math.max(var1.getY(), Evoker.this.getY()) + 1.0;
            float var6xxx = (float)Mth.atan2(var1.getZ() - Evoker.this.getZ(), var1.getX() - Evoker.this.getX());
            if (Evoker.this.distanceToSqr(var1) < 9.0) {
                for(int var7xxxx = 0; var7xxxx < 5; ++var7xxxx) {
                    float var8xxxxx = var6xxx + (float)var7xxxx * (float) Math.PI * 0.4F;
                    this.createSpellEntity(
                            Evoker.this.getX() + (double)Mth.cos(var8xxxxx) * 1.5, Evoker.this.getZ() + (double)Mth.sin(var8xxxxx) * 1.5, var2x, var4xx, var8xxxxx, 0
                    );
                }

                for(int var11xxxx = 0; var11xxxx < 8; ++var11xxxx) {
                    float var13xxxxx = var6xxx + (float)var11xxxx * (float) Math.PI * 2.0F / 8.0F + (float) (Math.PI * 2.0 / 5.0);
                    this.createSpellEntity(
                            Evoker.this.getX() + (double)Mth.cos(var13xxxxx) * 2.5, Evoker.this.getZ() + (double)Mth.sin(var13xxxxx) * 2.5, var2x, var4xx, var13xxxxx, 3
                    );
                }
            } else {
                for(int var12 = 0; var12 < 16; ++var12) {
                    double var14x = 1.25 * (double)(var12 + 1);
                    int var10xx = 1 * var12;
                    this.createSpellEntity(Evoker.this.getX() + (double)Mth.cos(var6xxx) * var14x, Evoker.this.getZ() + (double)Mth.sin(var6xxx) * var14x, var2x, var4xx, var6xxx, var10xx);
                }
            }
        }

        private void createSpellEntity(double d, double d2, double d3, double d4, float f, int i) {
            BlockPos blockPos = new BlockPos(d, d4, d2);
            boolean z = false;
            double d5 = 0.0d;
            while (true) {
                BlockPos below = blockPos.below();
                if (Evoker.this.level.getBlockState(below).isFaceSturdy(Evoker.this.level, below, Direction.UP)) {
                    if (!Evoker.this.level.isEmptyBlock(blockPos)) {
                        VoxelShape collisionShape = Evoker.this.level.getBlockState(blockPos).getCollisionShape(Evoker.this.level, blockPos);
                        if (!collisionShape.isEmpty()) {
                            d5 = collisionShape.max(Direction.Axis.Y);
                        }
                    }
                    z = true;
                } else {
                    blockPos = blockPos.below();
                    if (blockPos.getY() < Mth.floor(d3) - 1) {
                        break;
                    }
                }
            }
            if (z) {
                Evoker.this.level.addFreshEntity(new EvokerFangs(Evoker.this.level, d, blockPos.getY() + d5, d2, f, i, Evoker.this));
            }
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.EVOKER_PREPARE_ATTACK;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.FANGS;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Evoker$EvokerSummonSpellGoal.class */
    class EvokerSummonSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private final TargetingConditions vexCountTargeting;

        private EvokerSummonSpellGoal() {
            super();
            this.vexCountTargeting = new TargetingConditions().range(16.0d).allowUnseeable().ignoreInvisibilityTesting().allowInvulnerable().allowSameTeam();
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (super.canUse()) {
                return Evoker.this.random.nextInt(8) + 1 > Evoker.this.level.getNearbyEntities(Vex.class, this.vexCountTargeting, Evoker.this, Evoker.this.getBoundingBox().inflate(16.0d)).size();
            }
            return false;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected int getCastingTime() {
            return 100;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected int getCastingInterval() {
            return 340;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected void performSpellCasting() {
            ServerLevelAccessor serverLevelAccessor = (ServerLevel) Evoker.this.level;
            for (int i = 0; i < 3; i++) {
                BlockPos offset = Evoker.this.blockPosition().offset((-2) + Evoker.this.random.nextInt(5), 1, (-2) + Evoker.this.random.nextInt(5));
                Vex create = EntityType.VEX.create(Evoker.this.level);
                create.moveTo(offset, 0.0f, 0.0f);
                create.finalizeSpawn(serverLevelAccessor, Evoker.this.level.getCurrentDifficultyAt(offset), MobSpawnType.MOB_SUMMONED, null, null);
                create.setOwner(Evoker.this);
                create.setBoundOrigin(offset);
                create.setLimitedLife(20 * (30 + Evoker.this.random.nextInt(90)));
                serverLevelAccessor.addFreshEntityWithPassengers(create);
            }
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.EVOKER_PREPARE_SUMMON;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.SUMMON_VEX;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Evoker$EvokerWololoSpellGoal.class */
    public class EvokerWololoSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private final TargetingConditions wololoTargeting;

        public EvokerWololoSpellGoal() {
            super();
            this.wololoTargeting = new TargetingConditions().range(16.0d).allowInvulnerable().selector(livingEntity -> {
                return ((Sheep) livingEntity).getColor() == DyeColor.BLUE;
            });
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public /* bridge */ /* synthetic */ void tick() {
            super.tick();
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public /* bridge */ /* synthetic */ void start() {
            super.start();
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (Evoker.this.getTarget() != null || Evoker.this.isCastingSpell() || Evoker.this.tickCount < this.nextAttackTickCount || !Evoker.this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            }
            List<Sheep> nearbyEntities = Evoker.this.level.getNearbyEntities(Sheep.class, this.wololoTargeting, Evoker.this, Evoker.this.getBoundingBox().inflate(16.0d, 4.0d, 16.0d));
            if (!nearbyEntities.isEmpty()) {
                Evoker.this.setWololoTarget(nearbyEntities.get(Evoker.this.random.nextInt(nearbyEntities.size())));
                return true;
            }
            return false;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return Evoker.this.getWololoTarget() != null && this.attackWarmupDelay > 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            super.stop();
            Evoker.this.setWololoTarget(null);
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected void performSpellCasting() {
            Sheep wololoTarget = Evoker.this.getWololoTarget();
            if (wololoTarget != null && wololoTarget.isAlive()) {
                wololoTarget.setColor(DyeColor.RED);
            }
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected int getCastWarmupTime() {
            return 40;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected int getCastingTime() {
            return 60;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected int getCastingInterval() {
            return 140;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.EVOKER_PREPARE_WOLOLO;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.WOLOLO;
        }
    }
}
