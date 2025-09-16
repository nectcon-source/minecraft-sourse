package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/SpellcasterIllager.class */
public abstract class SpellcasterIllager extends AbstractIllager {
    private static final EntityDataAccessor<Byte> DATA_SPELL_CASTING_ID = SynchedEntityData.defineId(SpellcasterIllager.class, EntityDataSerializers.BYTE);
    protected int spellCastingTickCount;
    private IllagerSpell currentSpell;

    protected abstract SoundEvent getCastingSoundEvent();

    protected SpellcasterIllager(EntityType<? extends SpellcasterIllager> entityType, Level level) {
        super(entityType, level);
        this.currentSpell = IllagerSpell.NONE;
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SPELL_CASTING_ID, (byte) 0);
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.spellCastingTickCount = compoundTag.getInt("SpellTicks");
    }

    @Override // net.minecraft.world.entity.raid.Raider, net.minecraft.world.entity.monster.PatrollingMonster, net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("SpellTicks", this.spellCastingTickCount);
    }

    @Override // net.minecraft.world.entity.monster.AbstractIllager
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (isCastingSpell()) {
            return AbstractIllager.IllagerArmPose.SPELLCASTING;
        }
        if (isCelebrating()) {
            return AbstractIllager.IllagerArmPose.CELEBRATING;
        }
        return AbstractIllager.IllagerArmPose.CROSSED;
    }

    public boolean isCastingSpell() {
        return this.level.isClientSide ? ((Byte) this.entityData.get(DATA_SPELL_CASTING_ID)).byteValue() > 0 : this.spellCastingTickCount > 0;
    }

    public void setIsCastingSpell(IllagerSpell illagerSpell) {
        this.currentSpell = illagerSpell;
        this.entityData.set(DATA_SPELL_CASTING_ID, Byte.valueOf((byte) illagerSpell.f449id));
    }

    protected IllagerSpell getCurrentSpell() {
        if (!this.level.isClientSide) {
            return this.currentSpell;
        }
        return IllagerSpell.byId(((Byte) this.entityData.get(DATA_SPELL_CASTING_ID)).byteValue());
    }

    @Override // net.minecraft.world.entity.Mob
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.spellCastingTickCount > 0) {
            this.spellCastingTickCount--;
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (this.level.isClientSide && isCastingSpell()) {
            IllagerSpell currentSpell = getCurrentSpell();
            double d = currentSpell.spellColor[0];
            double d2 = currentSpell.spellColor[1];
            double d3 = currentSpell.spellColor[2];
            float cos = (this.yBodyRot * 0.017453292f) + (Mth.cos(this.tickCount * 0.6662f) * 0.25f);
            float cos2 = Mth.cos(cos);
            float sin = Mth.sin(cos);
            this.level.addParticle(ParticleTypes.ENTITY_EFFECT, getX() + (cos2 * 0.6d), getY() + 1.8d, getZ() + (sin * 0.6d), d, d2, d3);
            this.level.addParticle(ParticleTypes.ENTITY_EFFECT, getX() - (cos2 * 0.6d), getY() + 1.8d, getZ() - (sin * 0.6d), d, d2, d3);
        }
    }

    protected int getSpellCastingTime() {
        return this.spellCastingTickCount;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/SpellcasterIllager$SpellcasterCastingSpellGoal.class */
    public class SpellcasterCastingSpellGoal extends Goal {
        public SpellcasterCastingSpellGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return SpellcasterIllager.this.getSpellCastingTime() > 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            super.start();
            SpellcasterIllager.this.navigation.stop();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            super.stop();
            SpellcasterIllager.this.setIsCastingSpell(IllagerSpell.NONE);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (SpellcasterIllager.this.getTarget() != null) {
                SpellcasterIllager.this.getLookControl().setLookAt(SpellcasterIllager.this.getTarget(), SpellcasterIllager.this.getMaxHeadYRot(), SpellcasterIllager.this.getMaxHeadXRot());
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/SpellcasterIllager$SpellcasterUseSpellGoal.class */
    public abstract class SpellcasterUseSpellGoal extends Goal {
        protected int attackWarmupDelay;
        protected int nextAttackTickCount;

        protected abstract void performSpellCasting();

        protected abstract int getCastingTime();

        protected abstract int getCastingInterval();

        @Nullable
        protected abstract SoundEvent getSpellPrepareSound();

        protected abstract IllagerSpell getSpell();

        protected SpellcasterUseSpellGoal() {
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            LivingEntity target = SpellcasterIllager.this.getTarget();
            if (target == null || !target.isAlive() || SpellcasterIllager.this.isCastingSpell() || SpellcasterIllager.this.tickCount < this.nextAttackTickCount) {
                return false;
            }
            return true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            LivingEntity target = SpellcasterIllager.this.getTarget();
            return target != null && target.isAlive() && this.attackWarmupDelay > 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.attackWarmupDelay = getCastWarmupTime();
            SpellcasterIllager.this.spellCastingTickCount = getCastingTime();
            this.nextAttackTickCount = SpellcasterIllager.this.tickCount + getCastingInterval();
            SoundEvent spellPrepareSound = getSpellPrepareSound();
            if (spellPrepareSound != null) {
                SpellcasterIllager.this.playSound(spellPrepareSound, 1.0f, 1.0f);
            }
            SpellcasterIllager.this.setIsCastingSpell(getSpell());
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            this.attackWarmupDelay--;
            if (this.attackWarmupDelay == 0) {
                performSpellCasting();
                SpellcasterIllager.this.playSound(SpellcasterIllager.this.getCastingSoundEvent(), 1.0f, 1.0f);
            }
        }

        protected int getCastWarmupTime() {
            return 20;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/SpellcasterIllager$IllagerSpell.class */
    public enum IllagerSpell {
        NONE(0, 0.0d, 0.0d, 0.0d),
        SUMMON_VEX(1, 0.7d, 0.7d, 0.8d),
        FANGS(2, 0.4d, 0.3d, 0.35d),
        WOLOLO(3, 0.7d, 0.5d, 0.2d),
        DISAPPEAR(4, 0.3d, 0.3d, 0.8d),
        BLINDNESS(5, 0.1d, 0.1d, 0.2d);


        /* renamed from: id */
        private final int f449id;
        private final double[] spellColor;

        IllagerSpell(int i, double d, double d2, double d3) {
            this.f449id = i;
            this.spellColor = new double[]{d, d2, d3};
        }

        public static IllagerSpell byId(int i) {
            for (IllagerSpell illagerSpell : values()) {
                if (i == illagerSpell.f449id) {
                    return illagerSpell;
                }
            }
            return NONE;
        }
    }
}
