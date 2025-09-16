package net.minecraft.world.entity.decoration;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/decoration/LeashFenceKnotEntity.class */
public class LeashFenceKnotEntity extends HangingEntity {
    public LeashFenceKnotEntity(EntityType<? extends LeashFenceKnotEntity> entityType, Level level) {
        super(entityType, level);
    }

    public LeashFenceKnotEntity(Level level, BlockPos blockPos) {
        super(EntityType.LEASH_KNOT, level, blockPos);
        setPos(blockPos.getX() + 0.5d, blockPos.getY() + 0.5d, blockPos.getZ() + 0.5d);
        setBoundingBox(new AABB(getX() - 0.1875d, (getY() - 0.25d) + 0.125d, getZ() - 0.1875d, getX() + 0.1875d, getY() + 0.25d + 0.125d, getZ() + 0.1875d));
        this.forcedLoading = true;
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity, net.minecraft.world.entity.Entity
    public void setPos(double d, double d2, double d3) {
        super.setPos(Mth.floor(d) + 0.5d, Mth.floor(d2) + 0.5d, Mth.floor(d3) + 0.5d);
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    protected void recalculateBoundingBox() {
        setPosRaw(this.pos.getX() + 0.5d, this.pos.getY() + 0.5d, this.pos.getZ() + 0.5d);
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public void setDirection(Direction direction) {
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public int getWidth() {
        return 9;
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public int getHeight() {
        return 9;
    }

    @Override // net.minecraft.world.entity.Entity
    protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return -0.0625f;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 1024.0d;
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public void dropItem(@Nullable Entity entity) {
        playSound(SoundEvents.LEASH_KNOT_BREAK, 1.0f, 1.0f);
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override // net.minecraft.world.entity.Entity
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (this.level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        boolean z = false;
        List<Mob> entitiesOfClass = this.level.getEntitiesOfClass(Mob.class, new AABB(getX() - 7.0d, getY() - 7.0d, getZ() - 7.0d, getX() + 7.0d, getY() + 7.0d, getZ() + 7.0d));
        for (Mob mob : entitiesOfClass) {
            if (mob.getLeashHolder() == player) {
                mob.setLeashedTo(this, true);
                z = true;
            }
        }
        if (!z) {
            remove();
            if (player.abilities.instabuild) {
                for (Mob mob2 : entitiesOfClass) {
                    if (mob2.isLeashed() && mob2.getLeashHolder() == this) {
                        mob2.dropLeash(true, false);
                    }
                }
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public boolean survives() {
        return this.level.getBlockState(this.pos).getBlock().is(BlockTags.FENCES);
    }

    public static LeashFenceKnotEntity getOrCreateKnot(Level level, BlockPos blockPos) {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();
        for (LeashFenceKnotEntity leashFenceKnotEntity : level.getEntitiesOfClass(LeashFenceKnotEntity.class, new AABB(x - 1.0d, y - 1.0d, z - 1.0d, x + 1.0d, y + 1.0d, z + 1.0d))) {
            if (leashFenceKnotEntity.getPos().equals(blockPos)) {
                return leashFenceKnotEntity;
            }
        }
        LeashFenceKnotEntity leashFenceKnotEntity2 = new LeashFenceKnotEntity(level, blockPos);
        level.addFreshEntity(leashFenceKnotEntity2);
        leashFenceKnotEntity2.playPlacementSound();
        return leashFenceKnotEntity2;
    }

    @Override // net.minecraft.world.entity.decoration.HangingEntity
    public void playPlacementSound() {
        playSound(SoundEvents.LEASH_KNOT_PLACE, 1.0f, 1.0f);
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, getType(), 0, getPos());
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getRopeHoldPosition(float f) {
        return getPosition(f).add(0.0d, 0.2d, 0.0d);
    }
}
