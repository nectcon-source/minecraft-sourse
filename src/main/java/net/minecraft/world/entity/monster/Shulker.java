package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ShulkerSharedHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Shulker.class */
public class Shulker extends AbstractGolem implements Enemy {
    private static final UUID COVERED_ARMOR_MODIFIER_UUID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
    private static final AttributeModifier COVERED_ARMOR_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", 20.0d, AttributeModifier.Operation.ADDITION);
    protected static final EntityDataAccessor<Direction> DATA_ATTACH_FACE_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.DIRECTION);
    protected static final EntityDataAccessor<Optional<BlockPos>> DATA_ATTACH_POS_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    protected static final EntityDataAccessor<Byte> DATA_PEEK_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Byte> DATA_COLOR_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
    private float currentPeekAmountO;
    private float currentPeekAmount;
    private BlockPos oldAttachPosition;
    private int clientSideTeleportInterpolation;

    public Shulker(EntityType<? extends Shulker> entityType, Level level) {
        super(entityType, level);
        this.oldAttachPosition = null;
        this.xpReward = 5;
    }

    @Override // net.minecraft.world.entity.Mob
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(4, new ShulkerAttackGoal());
        this.goalSelector.addGoal(7, new ShulkerPeekGoal());
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new ShulkerNearestAttackGoal(this));
        this.targetSelector.addGoal(3, new ShulkerDefenseAttackGoal(this));
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override // net.minecraft.world.entity.animal.AbstractGolem, net.minecraft.world.entity.Mob
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHULKER_AMBIENT;
    }

    @Override // net.minecraft.world.entity.Mob
    public void playAmbientSound() {
        if (!isClosed()) {
            super.playAmbientSound();
        }
    }

    @Override // net.minecraft.world.entity.animal.AbstractGolem, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHULKER_DEATH;
    }

    @Override // net.minecraft.world.entity.animal.AbstractGolem, net.minecraft.world.entity.LivingEntity
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (isClosed()) {
            return SoundEvents.SHULKER_HURT_CLOSED;
        }
        return SoundEvents.SHULKER_HURT;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ATTACH_FACE_ID, Direction.DOWN);
        this.entityData.define(DATA_ATTACH_POS_ID, Optional.empty());
        this.entityData.define(DATA_PEEK_ID, (byte) 0);
        this.entityData.define(DATA_COLOR_ID, (byte) 16);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0d);
    }

    @Override // net.minecraft.world.entity.Mob
    protected BodyRotationControl createBodyControl() {
        return new ShulkerBodyRotationControl(this);
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(DATA_ATTACH_FACE_ID, Direction.from3DDataValue(compoundTag.getByte("AttachFace")));
        this.entityData.set(DATA_PEEK_ID, Byte.valueOf(compoundTag.getByte("Peek")));
        this.entityData.set(DATA_COLOR_ID, Byte.valueOf(compoundTag.getByte("Color")));
        if (compoundTag.contains("APX")) {
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(new BlockPos(compoundTag.getInt("APX"), compoundTag.getInt("APY"), compoundTag.getInt("APZ"))));
            return;
        }
        this.entityData.set(DATA_ATTACH_POS_ID, Optional.empty());
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putByte("AttachFace", (byte) ((Direction) this.entityData.get(DATA_ATTACH_FACE_ID)).get3DDataValue());
        compoundTag.putByte("Peek", ((Byte) this.entityData.get(DATA_PEEK_ID)).byteValue());
        compoundTag.putByte("Color", ((Byte) this.entityData.get(DATA_COLOR_ID)).byteValue());
        BlockPos attachPosition = getAttachPosition();
        if (attachPosition != null) {
            compoundTag.putInt("APX", attachPosition.getX());
            compoundTag.putInt("APY", attachPosition.getY());
            compoundTag.putInt("APZ", attachPosition.getZ());
        }
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        BlockPos blockPos = (BlockPos) ((Optional) this.entityData.get(DATA_ATTACH_POS_ID)).orElse(null);
        if (blockPos == null && !this.level.isClientSide) {
            blockPos = blockPosition();
            this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(blockPos));
        }
        if (isPassenger()) {
            blockPos = null;
            float f = getVehicle().yRot;
            this.yRot = f;
            this.yBodyRot = f;
            this.yBodyRotO = f;
            this.clientSideTeleportInterpolation = 0;
        } else if (!this.level.isClientSide) {
            BlockState blockState = this.level.getBlockState(blockPos);
            if (!blockState.isAir()) {
                if (blockState.is(Blocks.MOVING_PISTON)) {
                    Direction direction = (Direction) blockState.getValue(PistonBaseBlock.FACING);
                    if (this.level.isEmptyBlock(blockPos.relative(direction))) {
                        blockPos = blockPos.relative(direction);
                        this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(blockPos));
                    } else {
                        teleportSomewhere();
                    }
                } else if (blockState.is(Blocks.PISTON_HEAD)) {
                    Direction direction2 = (Direction) blockState.getValue(PistonHeadBlock.FACING);
                    if (this.level.isEmptyBlock(blockPos.relative(direction2))) {
                        blockPos = blockPos.relative(direction2);
                        this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(blockPos));
                    } else {
                        teleportSomewhere();
                    }
                } else {
                    teleportSomewhere();
                }
            }
            if (!canAttachOnBlockFace(blockPos, getAttachFace())) {
                Direction findAttachableFace = findAttachableFace(blockPos);
                if (findAttachableFace != null) {
                    this.entityData.set(DATA_ATTACH_FACE_ID, findAttachableFace);
                } else {
                    teleportSomewhere();
                }
            }
        }
        float rawPeekAmount = getRawPeekAmount() * 0.01f;
        this.currentPeekAmountO = this.currentPeekAmount;
        if (this.currentPeekAmount > rawPeekAmount) {
            this.currentPeekAmount = Mth.clamp(this.currentPeekAmount - 0.05f, rawPeekAmount, 1.0f);
        } else if (this.currentPeekAmount < rawPeekAmount) {
            this.currentPeekAmount = Mth.clamp(this.currentPeekAmount + 0.05f, 0.0f, rawPeekAmount);
        }
        if (blockPos != null) {
            if (this.level.isClientSide) {
                if (this.clientSideTeleportInterpolation > 0 && this.oldAttachPosition != null) {
                    this.clientSideTeleportInterpolation--;
                } else {
                    this.oldAttachPosition = blockPos;
                }
            }
            setPosAndOldPos(blockPos.getX() + 0.5d, blockPos.getY(), blockPos.getZ() + 0.5d);
            double sin = 0.5d - (Mth.sin((0.5f + this.currentPeekAmount) * 3.1415927f) * 0.5d);
            Direction opposite = getAttachFace().getOpposite();
            setBoundingBox(new AABB(getX() - 0.5d, getY(), getZ() - 0.5d, getX() + 0.5d, getY() + 1.0d, getZ() + 0.5d).expandTowards(opposite.getStepX() * sin, opposite.getStepY() * sin, opposite.getStepZ() * sin));
            double sin2 = sin - (0.5d - (Mth.sin((0.5f + this.currentPeekAmountO) * 3.1415927f) * 0.5d));
            if (sin2 > 0.0d) {
                List<Entity> entities = this.level.getEntities(this, getBoundingBox());
                if (!entities.isEmpty()) {
                    for (Entity entity : entities) {
                        if (!(entity instanceof Shulker) && !entity.noPhysics) {
                            entity.move(MoverType.SHULKER, new Vec3(sin2 * opposite.getStepX(), sin2 * opposite.getStepY(), sin2 * opposite.getStepZ()));
                        }
                    }
                }
            }
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void move(MoverType moverType, Vec3 vec3) {
        if (moverType == MoverType.SHULKER_BOX) {
            teleportSomewhere();
        } else {
            super.move(moverType, vec3);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void setPos(double d, double d2, double d3) {
        super.setPos(d, d2, d3);
        if (this.entityData == null || this.tickCount == 0) {
            return;
        }
        Optional<BlockPos> optional = (Optional) this.entityData.get(DATA_ATTACH_POS_ID);
        Optional<BlockPos> of = Optional.of(new BlockPos(d, d2, d3));
        if (!of.equals(optional)) {
            this.entityData.set(DATA_ATTACH_POS_ID, of);
            this.entityData.set(DATA_PEEK_ID, (byte) 0);
            this.hasImpulse = true;
        }
    }

    @Nullable
    protected Direction findAttachableFace(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (canAttachOnBlockFace(blockPos, direction)) {
                return direction;
            }
        }
        return null;
    }

    private boolean canAttachOnBlockFace(BlockPos blockPos, Direction direction) {
        return this.level.loadedAndEntityCanStandOnFace(blockPos.relative(direction), this, direction.getOpposite()) && this.level.noCollision(this, ShulkerSharedHelper.openBoundingBox(blockPos, direction.getOpposite()));
    }

    protected boolean teleportSomewhere() {
        Direction findAttachableFace;
        if (isNoAi() || !isAlive()) {
            return true;
        }
        BlockPos blockPosition = blockPosition();
        for (int i = 0; i < 5; i++) {
            BlockPos offset = blockPosition.offset(8 - this.random.nextInt(17), 8 - this.random.nextInt(17), 8 - this.random.nextInt(17));
            if (offset.getY() > 0 && this.level.isEmptyBlock(offset) && this.level.getWorldBorder().isWithinBounds(offset) && this.level.noCollision(this, new AABB(offset)) && (findAttachableFace = findAttachableFace(offset)) != null) {
                this.entityData.set(DATA_ATTACH_FACE_ID, findAttachableFace);
                playSound(SoundEvents.SHULKER_TELEPORT, 1.0f, 1.0f);
                this.entityData.set(DATA_ATTACH_POS_ID, Optional.of(offset));
                this.entityData.set(DATA_PEEK_ID, (byte) 0);
                setTarget(null);
                return true;
            }
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Mob, net.minecraft.world.entity.LivingEntity
    public void aiStep() {
        super.aiStep();
        setDeltaMovement(Vec3.ZERO);
        if (!isNoAi()) {
            this.yBodyRotO = 0.0f;
            this.yBodyRot = 0.0f;
        }
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        BlockPos attachPosition;
        if (DATA_ATTACH_POS_ID.equals(entityDataAccessor) && this.level.isClientSide && !isPassenger() && (attachPosition = getAttachPosition()) != null) {
            if (this.oldAttachPosition == null) {
                this.oldAttachPosition = attachPosition;
            } else {
                this.clientSideTeleportInterpolation = 6;
            }
            setPosAndOldPos(attachPosition.getX() + 0.5d, attachPosition.getY(), attachPosition.getZ() + 0.5d);
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void lerpTo(double d, double d2, double d3, float f, float f2, int i, boolean z) {
        this.lerpSteps = 0;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if ((isClosed() && (damageSource.getDirectEntity() instanceof AbstractArrow)) || !super.hurt(damageSource, f)) {
            return false;
        }
        if (getHealth() < getMaxHealth() * 0.5d && this.random.nextInt(4) == 0) {
            teleportSomewhere();
            return true;
        }
        return true;
    }

    private boolean isClosed() {
        return getRawPeekAmount() == 0;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean canBeCollidedWith() {
        return isAlive();
    }

    public Direction getAttachFace() {
        return (Direction) this.entityData.get(DATA_ATTACH_FACE_ID);
    }

    @Nullable
    public BlockPos getAttachPosition() {
        return (BlockPos) ((Optional) this.entityData.get(DATA_ATTACH_POS_ID)).orElse(null);
    }

    public void setAttachPosition(@Nullable BlockPos blockPos) {
        this.entityData.set(DATA_ATTACH_POS_ID, Optional.ofNullable(blockPos));
    }

    public int getRawPeekAmount() {
        return ((Byte) this.entityData.get(DATA_PEEK_ID)).byteValue();
    }

    public void setRawPeekAmount(int i) {
        if (!this.level.isClientSide) {
            getAttribute(Attributes.ARMOR).removeModifier(COVERED_ARMOR_MODIFIER);
            if (i == 0) {
                getAttribute(Attributes.ARMOR).addPermanentModifier(COVERED_ARMOR_MODIFIER);
                playSound(SoundEvents.SHULKER_CLOSE, 1.0f, 1.0f);
            } else {
                playSound(SoundEvents.SHULKER_OPEN, 1.0f, 1.0f);
            }
        }
        this.entityData.set(DATA_PEEK_ID, Byte.valueOf((byte) i));
    }

    public float getClientPeekAmount(float f) {
        return Mth.lerp(f, this.currentPeekAmountO, this.currentPeekAmount);
    }

    public int getClientSideTeleportInterpolation() {
        return this.clientSideTeleportInterpolation;
    }

    public BlockPos getOldAttachPosition() {
        return this.oldAttachPosition;
    }

    @Override // net.minecraft.world.entity.LivingEntity
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return 0.5f;
    }

    @Override // net.minecraft.world.entity.Mob
    public int getMaxHeadXRot() {
        return 180;
    }

    @Override // net.minecraft.world.entity.Mob
    public int getMaxHeadYRot() {
        return 180;
    }

    @Override // net.minecraft.world.entity.LivingEntity, net.minecraft.world.entity.Entity
    public void push(Entity entity) {
    }

    @Override // net.minecraft.world.entity.Entity
    public float getPickRadius() {
        return 0.0f;
    }

    public boolean hasValidInterpolationPositions() {
        return (this.oldAttachPosition == null || getAttachPosition() == null) ? false : true;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Shulker$ShulkerBodyRotationControl.class */
    class ShulkerBodyRotationControl extends BodyRotationControl {
        public ShulkerBodyRotationControl(Mob mob) {
            super(mob);
        }

        @Override // net.minecraft.world.entity.p000ai.control.BodyRotationControl
        public void clientTick() {
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Shulker$ShulkerPeekGoal.class */
    class ShulkerPeekGoal extends Goal {
        private int peekTime;

        private ShulkerPeekGoal() {
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            return Shulker.this.getTarget() == null && Shulker.this.random.nextInt(40) == 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canContinueToUse() {
            return Shulker.this.getTarget() == null && this.peekTime > 0;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.peekTime = 20 * (1 + Shulker.this.random.nextInt(3));
            Shulker.this.setRawPeekAmount(30);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            if (Shulker.this.getTarget() == null) {
                Shulker.this.setRawPeekAmount(0);
            }
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            this.peekTime--;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Shulker$ShulkerAttackGoal.class */
    class ShulkerAttackGoal extends Goal {
        private int attackTime;

        public ShulkerAttackGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            LivingEntity target = Shulker.this.getTarget();
            if (target == null || !target.isAlive() || Shulker.this.level.getDifficulty() == Difficulty.PEACEFUL) {
                return false;
            }
            return true;
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void start() {
            this.attackTime = 20;
            Shulker.this.setRawPeekAmount(100);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void stop() {
            Shulker.this.setRawPeekAmount(0);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.Goal
        public void tick() {
            if (Shulker.this.level.getDifficulty() == Difficulty.PEACEFUL) {
                return;
            }
            this.attackTime--;
            LivingEntity target = Shulker.this.getTarget();
            Shulker.this.getLookControl().setLookAt(target, 180.0f, 180.0f);
            if (Shulker.this.distanceToSqr(target) < 400.0d) {
                if (this.attackTime <= 0) {
                    this.attackTime = 20 + ((Shulker.this.random.nextInt(10) * 20) / 2);
                    Shulker.this.level.addFreshEntity(new ShulkerBullet(Shulker.this.level, Shulker.this, target, Shulker.this.getAttachFace().getAxis()));
                    Shulker.this.playSound(SoundEvents.SHULKER_SHOOT, 2.0f, ((Shulker.this.random.nextFloat() - Shulker.this.random.nextFloat()) * 0.2f) + 1.0f);
                }
            } else {
                Shulker.this.setTarget(null);
            }
            super.tick();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Shulker$ShulkerNearestAttackGoal.class */
    class ShulkerNearestAttackGoal extends NearestAttackableTargetGoal<Player> {
        public ShulkerNearestAttackGoal(Shulker shulker) {
            super(shulker, Player.class, true);
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (Shulker.this.level.getDifficulty() == Difficulty.PEACEFUL) {
                return false;
            }
            return super.canUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal
        protected AABB getTargetSearchArea(double d) {
            Direction attachFace = ((Shulker) this.mob).getAttachFace();
            if (attachFace.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().inflate(4.0d, d, d);
            }
            if (attachFace.getAxis() == Direction.Axis.Z) {
                return this.mob.getBoundingBox().inflate(d, d, 4.0d);
            }
            return this.mob.getBoundingBox().inflate(d, 4.0d, d);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/monster/Shulker$ShulkerDefenseAttackGoal.class */
    static class ShulkerDefenseAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
        public ShulkerDefenseAttackGoal(Shulker shulker) {
            super(shulker, LivingEntity.class, 10, true, false, livingEntity -> {
                return livingEntity instanceof Enemy;
            });
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
        public boolean canUse() {
            if (this.mob.getTeam() == null) {
                return false;
            }
            return super.canUse();
        }

        @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal
        protected AABB getTargetSearchArea(double d) {
            Direction attachFace = ((Shulker) this.mob).getAttachFace();
            if (attachFace.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().inflate(4.0d, d, d);
            }
            if (attachFace.getAxis() == Direction.Axis.Z) {
                return this.mob.getBoundingBox().inflate(d, d, 4.0d);
            }
            return this.mob.getBoundingBox().inflate(d, 4.0d, d);
        }
    }

    @Nullable
    public DyeColor getColor() {
        Byte b = (Byte) this.entityData.get(DATA_COLOR_ID);
        if (b.byteValue() == 16 || b.byteValue() > 15) {
            return null;
        }
        return DyeColor.byId(b.byteValue());
    }
}
