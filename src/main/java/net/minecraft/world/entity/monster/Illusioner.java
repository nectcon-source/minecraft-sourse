package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Illusioner.class */
public class Illusioner extends SpellcasterIllager implements RangedAttackMob {
    private int clientSideIllusionTicks;
    private final Vec3[][] clientSideIllusionOffsets;

    public Illusioner(EntityType<? extends Illusioner> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
        this.clientSideIllusionOffsets = new Vec3[2][4];
        for (int i = 0; i < 4; i++) {
            this.clientSideIllusionOffsets[0][i] = Vec3.ZERO;
            this.clientSideIllusionOffsets[1][i] = Vec3.ZERO;
        }
    }

    @Override // net.minecraft.world.entity.monster.AbstractIllager, net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SpellcasterIllager.SpellcasterCastingSpellGoal());
        this.goalSelector.addGoal(4, new IllusionerMirrorSpellGoal());
        this.goalSelector.addGoal(5, new IllusionerBlindnessSpellGoal());
        this.goalSelector.addGoal(6, new RangedBowAttackGoal(this, 0.5d, 20, 15.0f));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6d));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, false).setUnseenMemoryTicks(300));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.5d).add(Attributes.FOLLOW_RANGE, 18.0d).add(Attributes.MAX_HEALTH, 32.0d);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override // net.minecraft.world.entity.monster.SpellcasterIllager, net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(3.0d, 0.0d, 3.0d);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        if (this.level.isClientSide && isInvisible()) {
            this.clientSideIllusionTicks--;
            if (this.clientSideIllusionTicks < 0) {
                this.clientSideIllusionTicks = 0;
            }
            if (this.hurtTime != 1 && this.tickCount % 1200 != 0) {
                if (this.hurtTime == this.hurtDuration - 1) {
                    this.clientSideIllusionTicks = 3;
                    for (int i = 0; i < 4; i++) {
                        this.clientSideIllusionOffsets[0][i] = this.clientSideIllusionOffsets[1][i];
                        this.clientSideIllusionOffsets[1][i] = new Vec3(0.0d, 0.0d, 0.0d);
                    }
                    return;
                }
                return;
            }
            this.clientSideIllusionTicks = 3;
            for (int i2 = 0; i2 < 4; i2++) {
                this.clientSideIllusionOffsets[0][i2] = this.clientSideIllusionOffsets[1][i2];
                this.clientSideIllusionOffsets[1][i2] = new Vec3(((-6.0f) + this.random.nextInt(13)) * 0.5d, Math.max(0, this.random.nextInt(6) - 4), ((-6.0f) + this.random.nextInt(13)) * 0.5d);
            }
            for (int i3 = 0; i3 < 16; i3++) {
                this.level.addParticle(ParticleTypes.CLOUD, getRandomX(0.5d), getRandomY(), getZ(0.5d), 0.0d, 0.0d, 0.0d);
            }
            this.level.playLocalSound(getX(), getY(), getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, getSoundSource(), 1.0f, 1.0f, false);
        }
    }

    @Override // net.minecraft.world.entity.raid.Raider
    public SoundEvent getCelebrateSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    public Vec3[] getIllusionOffsets(float f) {
        if (this.clientSideIllusionTicks <= 0) {
            return this.clientSideIllusionOffsets[1];
        }
        double pow = Math.pow((this.clientSideIllusionTicks - f) / 3.0f, 0.25d);
        Vec3[] vec3Arr = new Vec3[4];
        for (int i = 0; i < 4; i++) {
            vec3Arr[i] = this.clientSideIllusionOffsets[1][i].scale(1.0d - pow).add(this.clientSideIllusionOffsets[0][i].scale(pow));
        }
        return vec3Arr;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isAlliedTo(Entity entity) {
        if (super.isAlliedTo(entity)) {
            return true;
        }
        return (entity instanceof LivingEntity) && ((LivingEntity) entity).getMobType() == MobType.ILLAGER && getTeam() == null && entity.getTeam() == null;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.ILLUSIONER_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ILLUSIONER_HURT;
    }

    @Override // net.minecraft.world.entity.monster.SpellcasterIllager
    protected SoundEvent getCastingSoundEvent() {
        return SoundEvents.ILLUSIONER_CAST_SPELL;
    }

    @Override // net.minecraft.world.entity.raid.Raider
    public void applyRaidBuffs(int i, boolean z) {
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Illusioner$IllusionerMirrorSpellGoal.class */
    class IllusionerMirrorSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private IllusionerMirrorSpellGoal() {
            super();
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (!super.canUse() || Illusioner.this.hasEffect(MobEffects.INVISIBILITY)) {
                return false;
            }
            return true;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected int getCastingTime() {
            return 20;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected int getCastingInterval() {
            return 340;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected void performSpellCasting() {
            Illusioner.this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 1200));
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        @Nullable
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.ILLUSIONER_PREPARE_MIRROR;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.DISAPPEAR;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Illusioner$IllusionerBlindnessSpellGoal.class */
    class IllusionerBlindnessSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private int lastTargetId;

        private IllusionerBlindnessSpellGoal() {
            super();
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (!super.canUse() || Illusioner.this.getTarget() == null || Illusioner.this.getTarget().getId() == this.lastTargetId || !Illusioner.this.level.getCurrentDifficultyAt(Illusioner.this.blockPosition()).isHarderThan(Difficulty.NORMAL.ordinal())) {
                return false;
            }
            return true;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            super.start();
            this.lastTargetId = Illusioner.this.getTarget().getId();
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected int getCastingTime() {
            return 20;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected int getCastingInterval() {
            return 180;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected void performSpellCasting() {
            Illusioner.this.getTarget().addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400));
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.ILLUSIONER_PREPARE_BLINDNESS;
        }

        @Override // net.minecraft.world.entity.monster.SpellcasterIllager.SpellcasterUseSpellGoal
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.BLINDNESS;
        }
    }

    @Override // net.minecraft.world.entity.monster.RangedAttackMob
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        ItemStack var3 = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW)));
        AbstractArrow var4x = ProjectileUtil.getMobArrow(this, var3, f);
        double var5xx = livingEntity.getX() - this.getX();
        double var7xxx = livingEntity.getY(0.3333333333333333) - var4x.getY();
        double var9xxxx = livingEntity.getZ() - this.getZ();
        double var11xxxxx = (double)Mth.sqrt(var5xx * var5xx + var9xxxx * var9xxxx);
        var4x.shoot(var5xx, var7xxx + var11xxxxx * 0.2F, var9xxxx, 1.6F, (float)(14 - this.level.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(var4x);
    }

    @Override // net.minecraft.world.entity.monster.SpellcasterIllager, net.minecraft.world.entity.monster.AbstractIllager
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (isCastingSpell()) {
            return AbstractIllager.IllagerArmPose.SPELLCASTING;
        }
        if (isAggressive()) {
            return AbstractIllager.IllagerArmPose.BOW_AND_ARROW;
        }
        return AbstractIllager.IllagerArmPose.CROSSED;
    }
}
