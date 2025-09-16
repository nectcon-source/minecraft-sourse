package net.minecraft.world.entity.projectile;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/ShulkerBullet.class */
public class ShulkerBullet extends Projectile {
    private Entity finalTarget;

    @Nullable
    private Direction currentMoveDirection;
    private int flightSteps;
    private double targetDeltaX;
    private double targetDeltaY;
    private double targetDeltaZ;

    @Nullable
    private UUID targetId;

    public ShulkerBullet(EntityType<? extends ShulkerBullet> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public ShulkerBullet(Level level, double d, double d2, double d3, double d4, double d5, double d6) {
        this(EntityType.SHULKER_BULLET, level);
        moveTo(d, d2, d3, this.yRot, this.xRot);
        setDeltaMovement(d4, d5, d6);
    }

    public ShulkerBullet(Level level, LivingEntity livingEntity, Entity entity, Direction.Axis axis) {
        this(EntityType.SHULKER_BULLET, level);
        setOwner(livingEntity);
        BlockPos blockPosition = livingEntity.blockPosition();
        moveTo(blockPosition.getX() + 0.5d, blockPosition.getY() + 0.5d, blockPosition.getZ() + 0.5d, this.yRot, this.xRot);
        this.finalTarget = entity;
        this.currentMoveDirection = Direction.UP;
        selectNextMoveDirection(axis);
    }

    @Override // net.minecraft.world.entity.Entity
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.finalTarget != null) {
            compoundTag.putUUID("Target", this.finalTarget.getUUID());
        }
        if (this.currentMoveDirection != null) {
            compoundTag.putInt("Dir", this.currentMoveDirection.get3DDataValue());
        }
        compoundTag.putInt("Steps", this.flightSteps);
        compoundTag.putDouble("TXD", this.targetDeltaX);
        compoundTag.putDouble("TYD", this.targetDeltaY);
        compoundTag.putDouble("TZD", this.targetDeltaZ);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.flightSteps = compoundTag.getInt("Steps");
        this.targetDeltaX = compoundTag.getDouble("TXD");
        this.targetDeltaY = compoundTag.getDouble("TYD");
        this.targetDeltaZ = compoundTag.getDouble("TZD");
        if (compoundTag.contains("Dir", 99)) {
            this.currentMoveDirection = Direction.from3DDataValue(compoundTag.getInt("Dir"));
        }
        if (compoundTag.hasUUID("Target")) {
            this.targetId = compoundTag.getUUID("Target");
        }
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
    }

    private void setMoveDirection(@Nullable Direction direction) {
        this.currentMoveDirection = direction;
    }

    private void selectNextMoveDirection(@Nullable Direction.Axis axis) {
        BlockPos blockPos;
        double d = 0.5d;
        if (this.finalTarget == null) {
            blockPos = blockPosition().below();
        } else {
            d = this.finalTarget.getBbHeight() * 0.5d;
            blockPos = new BlockPos(this.finalTarget.getX(), this.finalTarget.getY() + d, this.finalTarget.getZ());
        }
        double x = blockPos.getX() + 0.5d;
        double y = blockPos.getY() + d;
        double z = blockPos.getZ() + 0.5d;
        Direction direction = null;
        if (!blockPos.closerThan(position(), 2.0d)) {
            BlockPos blockPosition = blockPosition();
            List<Direction> newArrayList = Lists.newArrayList();
            if (axis != Direction.Axis.X) {
                if (blockPosition.getX() < blockPos.getX() && this.level.isEmptyBlock(blockPosition.east())) {
                    newArrayList.add(Direction.EAST);
                } else if (blockPosition.getX() > blockPos.getX() && this.level.isEmptyBlock(blockPosition.west())) {
                    newArrayList.add(Direction.WEST);
                }
            }
            if (axis != Direction.Axis.Y) {
                if (blockPosition.getY() < blockPos.getY() && this.level.isEmptyBlock(blockPosition.above())) {
                    newArrayList.add(Direction.UP);
                } else if (blockPosition.getY() > blockPos.getY() && this.level.isEmptyBlock(blockPosition.below())) {
                    newArrayList.add(Direction.DOWN);
                }
            }
            if (axis != Direction.Axis.Z) {
                if (blockPosition.getZ() < blockPos.getZ() && this.level.isEmptyBlock(blockPosition.south())) {
                    newArrayList.add(Direction.SOUTH);
                } else if (blockPosition.getZ() > blockPos.getZ() && this.level.isEmptyBlock(blockPosition.north())) {
                    newArrayList.add(Direction.NORTH);
                }
            }
            direction = Direction.getRandom(this.random);
            if (newArrayList.isEmpty()) {
                for (int i = 5; !this.level.isEmptyBlock(blockPosition.relative(direction)) && i > 0; i--) {
                    direction = Direction.getRandom(this.random);
                }
            } else {
                direction = newArrayList.get(this.random.nextInt(newArrayList.size()));
            }
            x = getX() + direction.getStepX();
            y = getY() + direction.getStepY();
            z = getZ() + direction.getStepZ();
        }
        setMoveDirection(direction);
        double x2 = x - getX();
        double y2 = y - getY();
        double z2 = z - getZ();
        double sqrt = Mth.sqrt((x2 * x2) + (y2 * y2) + (z2 * z2));
        if (sqrt == 0.0d) {
            this.targetDeltaX = 0.0d;
            this.targetDeltaY = 0.0d;
            this.targetDeltaZ = 0.0d;
        } else {
            this.targetDeltaX = (x2 / sqrt) * 0.15d;
            this.targetDeltaY = (y2 / sqrt) * 0.15d;
            this.targetDeltaZ = (z2 / sqrt) * 0.15d;
        }
        this.hasImpulse = true;
        this.flightSteps = 10 + (this.random.nextInt(5) * 10);
    }

    @Override // net.minecraft.world.entity.Entity
    public void checkDespawn() {
        if (this.level.getDifficulty() == Difficulty.PEACEFUL) {
            remove();
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            if (this.finalTarget == null && this.targetId != null) {
                this.finalTarget = ((ServerLevel) this.level).getEntity(this.targetId);
                if (this.finalTarget == null) {
                    this.targetId = null;
                }
            }
            if (this.finalTarget != null && this.finalTarget.isAlive() && (!(this.finalTarget instanceof Player) || !((Player) this.finalTarget).isSpectator())) {
                this.targetDeltaX = Mth.clamp(this.targetDeltaX * 1.025d, -1.0d, 1.0d);
                this.targetDeltaY = Mth.clamp(this.targetDeltaY * 1.025d, -1.0d, 1.0d);
                this.targetDeltaZ = Mth.clamp(this.targetDeltaZ * 1.025d, -1.0d, 1.0d);
                Vec3 deltaMovement = getDeltaMovement();
                setDeltaMovement(deltaMovement.add((this.targetDeltaX - deltaMovement.x) * 0.2d, (this.targetDeltaY - deltaMovement.y) * 0.2d, (this.targetDeltaZ - deltaMovement.z) * 0.2d));
            } else if (!isNoGravity()) {
                setDeltaMovement(getDeltaMovement().add(0.0d, -0.04d, 0.0d));
            }
            HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
            if (hitResult.getType() != HitResult.Type.MISS) {
                onHit(hitResult);
            }
        }
        checkInsideBlocks();
        Vec3 deltaMovement2 = getDeltaMovement();
        setPos(getX() + deltaMovement2.x, getY() + deltaMovement2.y, getZ() + deltaMovement2.z);
        ProjectileUtil.rotateTowardsMovement(this, 0.5f);
        if (this.level.isClientSide) {
            this.level.addParticle(ParticleTypes.END_ROD, getX() - deltaMovement2.x, (getY() - deltaMovement2.y) + 0.15d, getZ() - deltaMovement2.z, 0.0d, 0.0d, 0.0d);
            return;
        }
        if (this.finalTarget != null && !this.finalTarget.removed) {
            if (this.flightSteps > 0) {
                this.flightSteps--;
                if (this.flightSteps == 0) {
                    selectNextMoveDirection(this.currentMoveDirection == null ? null : this.currentMoveDirection.getAxis());
                }
            }
            if (this.currentMoveDirection != null) {
                BlockPos blockPosition = blockPosition();
                Direction.Axis axis = this.currentMoveDirection.getAxis();
                if (this.level.loadedAndEntityCanStandOn(blockPosition.relative(this.currentMoveDirection), this)) {
                    selectNextMoveDirection(axis);
                    return;
                }
                BlockPos blockPosition2 = this.finalTarget.blockPosition();
                if ((axis == Direction.Axis.X && blockPosition.getX() == blockPosition2.getX()) || ((axis == Direction.Axis.Z && blockPosition.getZ() == blockPosition2.getZ()) || (axis == Direction.Axis.Y && blockPosition.getY() == blockPosition2.getY()))) {
                    selectNextMoveDirection(axis);
                }
            }
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !entity.noPhysics;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isOnFire() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 16384.0d;
    }

    @Override // net.minecraft.world.entity.Entity
    public float getBrightness() {
        return 1.0f;
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        Entity owner = getOwner();
        LivingEntity livingEntity = owner instanceof LivingEntity ? (LivingEntity) owner : null;
        if (entity.hurt(DamageSource.indirectMobAttack(this, livingEntity).setProjectile(), 4.0f)) {
            doEnchantDamageEffects(livingEntity, entity);
            if (entity instanceof LivingEntity) {
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200));
            }
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        ((ServerLevel) this.level).sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 2, 0.2d, 0.2d, 0.2d, 0.0d);
        playSound(SoundEvents.SHULKER_BULLET_HIT, 1.0f, 1.0f);
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        remove();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPickable() {
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (!this.level.isClientSide) {
            playSound(SoundEvents.SHULKER_BULLET_HURT, 1.0f, 1.0f);
            ((ServerLevel) this.level).sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 15, 0.2d, 0.2d, 0.2d, 0.0d);
            remove();
            return true;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
