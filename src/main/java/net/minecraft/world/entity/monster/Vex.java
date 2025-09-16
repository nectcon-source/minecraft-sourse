package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Vex.class */
public class Vex extends Monster {
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Vex.class, EntityDataSerializers.BYTE);
    private Mob owner;

    @Nullable
    private BlockPos boundOrigin;
    private boolean hasLimitedLife;
    private int limitedLifeTicks;

    public Vex(EntityType<? extends Vex> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new VexMoveControl(this);
        this.xpReward = 3;
    }

    @Override // net.minecraft.world.entity.Entity
    public void move(MoverType moverType, Vec3 vec3) {
        super.move(moverType, vec3);
        checkInsideBlocks();
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        setNoGravity(true);
        if (this.hasLimitedLife) {
            int i = this.limitedLifeTicks - 1;
            this.limitedLifeTicks = i;
            if (i <= 0) {
                this.limitedLifeTicks = 20;
                hurt(DamageSource.STARVE, 1.0f);
            }
        }
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new VexChargeAttackGoal());
        this.goalSelector.addGoal(8, new VexRandomMoveGoal());
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new VexCopyOwnerTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 14.0d).add(Attributes.ATTACK_DAMAGE, 4.0d);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte) 0);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("BoundX")) {
            this.boundOrigin = new BlockPos(compoundTag.getInt("BoundX"), compoundTag.getInt("BoundY"), compoundTag.getInt("BoundZ"));
        }
        if (compoundTag.contains("LifeTicks")) {
            setLimitedLife(compoundTag.getInt("LifeTicks"));
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.boundOrigin != null) {
            compoundTag.putInt("BoundX", this.boundOrigin.getX());
            compoundTag.putInt("BoundY", this.boundOrigin.getY());
            compoundTag.putInt("BoundZ", this.boundOrigin.getZ());
        }
        if (this.hasLimitedLife) {
            compoundTag.putInt("LifeTicks", this.limitedLifeTicks);
        }
    }

    public Mob getOwner() {
        return this.owner;
    }

    @Nullable
    public BlockPos getBoundOrigin() {
        return this.boundOrigin;
    }

    public void setBoundOrigin(@Nullable BlockPos blockPos) {
        this.boundOrigin = blockPos;
    }

    private boolean getVexFlag(int i) {
        return (((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue() & i) != 0;
    }

    private void setVexFlag(int i, boolean z) {
        int i2;
        int byteValue = ((Byte) this.entityData.get(DATA_FLAGS_ID)).byteValue();
        if (z) {
            i2 = byteValue | i;
        } else {
            i2 = byteValue & (i ^ (-1));
        }
        this.entityData.set(DATA_FLAGS_ID, Byte.valueOf((byte) (i2 & 255)));
    }

    public boolean isCharging() {
        return getVexFlag(1);
    }

    public void setIsCharging(boolean z) {
        setVexFlag(1, z);
    }

    public void setOwner(Mob mob) {
        this.owner = mob;
    }

    public void setLimitedLife(int i) {
        this.hasLimitedLife = true;
        this.limitedLifeTicks = i;
    }

    @Override // net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override // net.minecraft.world.entity.monster.Monster, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VEX_HURT;
    }

    @Override // net.minecraft.world.entity.Entity
    public float getBrightness() {
        return 1.0f;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Vex$VexMoveControl.class */
    class VexMoveControl extends MoveControl {
        public VexMoveControl(Vex vex) {
            super(vex);
        }

        @Override // net.minecraft.world.entity.p000ai.control.MoveControl
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                return;
            }
            Vec3 vec3 = new Vec3(this.wantedX - Vex.this.getX(), this.wantedY - Vex.this.getY(), this.wantedZ - Vex.this.getZ());
            double length = vec3.length();
            if (length < Vex.this.getBoundingBox().getSize()) {
                this.operation = MoveControl.Operation.WAIT;
                Vex.this.setDeltaMovement(Vex.this.getDeltaMovement().scale(0.5d));
                return;
            }
            Vex.this.setDeltaMovement(Vex.this.getDeltaMovement().add(vec3.scale((this.speedModifier * 0.05d) / length)));
            if (Vex.this.getTarget() == null) {
                Vec3 deltaMovement = Vex.this.getDeltaMovement();
                Vex.this.yRot = (-((float) Mth.atan2(deltaMovement.x, deltaMovement.z))) * 57.295776f;
                Vex.this.yBodyRot = Vex.this.yRot;
                return;
            }
            double x = Vex.this.getTarget().getX() - Vex.this.getX();
            double z = Vex.this.getTarget().getZ() - Vex.this.getZ();
            Vex.this.yRot = (-((float) Mth.atan2(x, z))) * 57.295776f;
            Vex.this.yBodyRot = Vex.this.yRot;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Vex$VexChargeAttackGoal.class */
    class VexChargeAttackGoal extends Goal {
        public VexChargeAttackGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return Vex.this.getTarget() != null && !Vex.this.getMoveControl().hasWanted() && Vex.this.random.nextInt(7) == 0 && Vex.this.distanceToSqr(Vex.this.getTarget()) > 4.0d;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return Vex.this.getMoveControl().hasWanted() && Vex.this.isCharging() && Vex.this.getTarget() != null && Vex.this.getTarget().isAlive();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            Vec3 eyePosition = Vex.this.getTarget().getEyePosition(1.0f);
            Vex.this.moveControl.setWantedPosition(eyePosition.x, eyePosition.y, eyePosition.z, 1.0d);
            Vex.this.setIsCharging(true);
            Vex.this.playSound(SoundEvents.VEX_CHARGE, 1.0f, 1.0f);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            Vex.this.setIsCharging(false);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            LivingEntity target = Vex.this.getTarget();
            if (Vex.this.getBoundingBox().intersects(target.getBoundingBox())) {
                Vex.this.doHurtTarget(target);
                Vex.this.setIsCharging(false);
            } else if (Vex.this.distanceToSqr(target) < 9.0d) {
                Vec3 eyePosition = target.getEyePosition(1.0f);
                Vex.this.moveControl.setWantedPosition(eyePosition.x, eyePosition.y, eyePosition.z, 1.0d);
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Vex$VexRandomMoveGoal.class */
    class VexRandomMoveGoal extends Goal {
        public VexRandomMoveGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return !Vex.this.getMoveControl().hasWanted() && Vex.this.random.nextInt(7) == 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return false;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            BlockPos var1 = net.minecraft.world.entity.monster.Vex.this.getBoundOrigin();
            if (var1 == null) {
                var1 = net.minecraft.world.entity.monster.Vex.this.blockPosition();
            }

            for(int var2 = 0; var2 < 3; ++var2) {
                BlockPos var3 = var1.offset(net.minecraft.world.entity.monster.Vex.this.random.nextInt(15) - 7, net.minecraft.world.entity.monster.Vex.this.random.nextInt(11) - 5, net.minecraft.world.entity.monster.Vex.this.random.nextInt(15) - 7);
                if (net.minecraft.world.entity.monster.Vex.this.level.isEmptyBlock(var3)) {
                    net.minecraft.world.entity.monster.Vex.this.moveControl.setWantedPosition((double)var3.getX() + (double)0.5F, (double)var3.getY() + (double)0.5F, (double)var3.getZ() + (double)0.5F, (double)0.25F);
                    if (net.minecraft.world.entity.monster.Vex.this.getTarget() == null) {
                        net.minecraft.world.entity.monster.Vex.this.getLookControl().setLookAt((double)var3.getX() + (double)0.5F, (double)var3.getY() + (double)0.5F, (double)var3.getZ() + (double)0.5F, 180.0F, 20.0F);
                    }
                    break;
                }
            }
        }
    }

    @Override // net.minecraft.world.entity.Mob
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag compoundTag) {
        populateDefaultEquipmentSlots(difficultyInstance);
        populateDefaultEquipmentEnchantments(difficultyInstance);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
    }

    @Override // net.minecraft.world.entity.Mob
    protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        setDropChance(EquipmentSlot.MAINHAND, 0.0f);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Vex$VexCopyOwnerTargetGoal.class */
    class VexCopyOwnerTargetGoal extends TargetGoal {
        private final TargetingConditions copyOwnerTargeting;

        public VexCopyOwnerTargetGoal(PathfinderMob pathfinderMob) {
            super(pathfinderMob, false);
            this.copyOwnerTargeting = new TargetingConditions().allowUnseeable().ignoreInvisibilityTesting();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return (Vex.this.owner == null || Vex.this.owner.getTarget() == null || !canAttack(Vex.this.owner.getTarget(), this.copyOwnerTargeting)) ? false : true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            Vex.this.setTarget(Vex.this.owner.getTarget());
            super.start();
        }
    }
}
