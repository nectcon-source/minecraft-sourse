package net.minecraft.world.entity.decoration;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/decoration/HangingEntity.class */
public abstract class HangingEntity extends Entity {
    protected static final Predicate<Entity> HANGING_ENTITY = entity -> {
        return entity instanceof HangingEntity;
    };
    private int checkInterval;
    protected BlockPos pos;
    protected Direction direction;

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract void dropItem(@Nullable Entity entity);

    public abstract void playPlacementSound();

    protected HangingEntity(EntityType<? extends HangingEntity> entityType, Level level) {
        super(entityType, level);
        this.direction = Direction.SOUTH;
    }

    protected HangingEntity(EntityType<? extends HangingEntity> entityType, Level level, BlockPos blockPos) {
        this(entityType, level);
        this.pos = blockPos;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
    }

    protected void setDirection(Direction direction) {
        Validate.notNull(direction);
        Validate.isTrue(direction.getAxis().isHorizontal());
        this.direction = direction;
        this.yRot = this.direction.get2DDataValue() * 90;
        this.yRotO = this.yRot;
        recalculateBoundingBox();
    }

    protected void recalculateBoundingBox() {
        if (this.direction == null) {
            return;
        }
        double offs = offs(getWidth());
        double x = (this.pos.getX() + 0.5d) - (this.direction.getStepX() * 0.46875d);
        double z = (this.pos.getZ() + 0.5d) - (this.direction.getStepZ() * 0.46875d);
        double y = this.pos.getY() + 0.5d + offs(getHeight());
        Direction counterClockWise = this.direction.getCounterClockWise();
        double stepX = x + (offs * counterClockWise.getStepX());
        double stepZ = z + (offs * counterClockWise.getStepZ());
        setPosRaw(stepX, y, stepZ);
        double width = getWidth();
        double height = getHeight();
        double width2 = getWidth();
        if (this.direction.getAxis() == Direction.Axis.Z) {
            width2 = 1.0d;
        } else {
            width = 1.0d;
        }
        double d = width / 32.0d;
        double d2 = height / 32.0d;
        double d3 = width2 / 32.0d;
        setBoundingBox(new AABB(stepX - d, y - d2, stepZ - d3, stepX + d, y + d2, stepZ + d3));
    }

    private double offs(int i) {
        return i % 32 == 0 ? 0.5d : 0.0d;
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick() {
        if (!this.level.isClientSide) {
            if (getY() < -64.0d) {
                outOfWorld();
            }
            int i = this.checkInterval;
            this.checkInterval = i + 1;
            if (i == 100) {
                this.checkInterval = 0;
                if (!this.removed && !survives()) {
                    remove();
                    dropItem(null);
                }
            }
        }
    }

    public boolean survives() {
        if (!this.level.noCollision(this)) {
            return false;
        }
        int max = Math.max(1, getWidth() / 16);
        int max2 = Math.max(1, getHeight() / 16);
        BlockPos relative = this.pos.relative(this.direction.getOpposite());
        Direction counterClockWise = this.direction.getCounterClockWise();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < max; i++) {
            for (int i2 = 0; i2 < max2; i2++) {
                mutableBlockPos.set(relative).move(counterClockWise, i + ((max - 1) / (-2))).move(Direction.UP, i2 + ((max2 - 1) / (-2)));
                BlockState blockState = this.level.getBlockState(mutableBlockPos);
                if (!blockState.getMaterial().isSolid() && !DiodeBlock.isDiode(blockState)) {
                    return false;
                }
            }
        }
        return this.level.getEntities(this, getBoundingBox(), HANGING_ENTITY).isEmpty();
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPickable() {
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean skipAttackInteraction(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (!this.level.mayInteract(player, this.pos)) {
                return true;
            }
            return hurt(DamageSource.playerAttack(player), 0.0f);
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public Direction getDirection() {
        return this.direction;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        if (!this.removed && !this.level.isClientSide) {
            remove();
            markHurt();
            dropItem(damageSource.getEntity());
            return true;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public void move(MoverType moverType, Vec3 vec3) {
        if (!this.level.isClientSide && !this.removed && vec3.lengthSqr() > 0.0d) {
            remove();
            dropItem(null);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void push(double d, double d2, double d3) {
        if (!this.level.isClientSide && !this.removed && (d * d) + (d2 * d2) + (d3 * d3) > 0.0d) {
            remove();
            dropItem(null);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        BlockPos pos = getPos();
        compoundTag.putInt("TileX", pos.getX());
        compoundTag.putInt("TileY", pos.getY());
        compoundTag.putInt("TileZ", pos.getZ());
    }

    @Override // net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.pos = new BlockPos(compoundTag.getInt("TileX"), compoundTag.getInt("TileY"), compoundTag.getInt("TileZ"));
    }

    @Override // net.minecraft.world.entity.Entity
    public ItemEntity spawnAtLocation(ItemStack itemStack, float f) {
        ItemEntity itemEntity = new ItemEntity(this.level, getX() + (this.direction.getStepX() * 0.15f), getY() + f, getZ() + (this.direction.getStepZ() * 0.15f), itemStack);
        itemEntity.setDefaultPickUpDelay();
        this.level.addFreshEntity(itemEntity);
        return itemEntity;
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public void setPos(double d, double d2, double d3) {
        this.pos = new BlockPos(d, d2, d3);
        recalculateBoundingBox();
        this.hasImpulse = true;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Override // net.minecraft.world.entity.Entity
    public float rotate(Rotation rotation) {
        if (this.direction.getAxis() != Direction.Axis.Y) {
            switch (rotation) {
                case CLOCKWISE_180:
                    this.direction = this.direction.getOpposite();
                    break;
                case COUNTERCLOCKWISE_90:
                    this.direction = this.direction.getCounterClockWise();
                    break;
                case CLOCKWISE_90:
                    this.direction = this.direction.getClockWise();
                    break;
            }
        }
        float wrapDegrees = Mth.wrapDegrees(this.yRot);
        switch (rotation) {
            case CLOCKWISE_180:
                return wrapDegrees + 180.0f;
            case COUNTERCLOCKWISE_90:
                return wrapDegrees + 90.0f;
            case CLOCKWISE_90:
                return wrapDegrees + 270.0f;
            default:
                return wrapDegrees;
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public float mirror(Mirror mirror) {
        return rotate(mirror.getRotation(this.direction));
    }

    @Override // net.minecraft.world.entity.Entity
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
    }

    @Override // net.minecraft.world.entity.Entity
    public void refreshDimensions() {
    }
}
