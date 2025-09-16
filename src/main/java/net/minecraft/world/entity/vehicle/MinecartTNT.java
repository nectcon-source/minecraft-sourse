package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/MinecartTNT.class */
public class MinecartTNT extends AbstractMinecart {
    private int fuse;

    public MinecartTNT(EntityType<? extends MinecartTNT> entityType, Level level) {
        super(entityType, level);
        this.fuse = -1;
    }

    public MinecartTNT(Level level, double d, double d2, double d3) {
        super(EntityType.TNT_MINECART, level, d, d2, d3);
        this.fuse = -1;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.TNT;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.TNT.defaultBlockState();
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (this.fuse > 0) {
            this.fuse--;
            this.level.addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5d, getZ(), 0.0d, 0.0d, 0.0d);
        } else if (this.fuse == 0) {
            explode(getHorizontalDistanceSqr(getDeltaMovement()));
        }
        if (this.horizontalCollision) {
            double horizontalDistanceSqr = getHorizontalDistanceSqr(getDeltaMovement());
            if (horizontalDistanceSqr >= 0.009999999776482582d) {
                explode(horizontalDistanceSqr);
            }
        }
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        Entity directEntity = damageSource.getDirectEntity();
        if (directEntity instanceof AbstractArrow) {
            AbstractArrow abstractArrow = (AbstractArrow) directEntity;
            if (abstractArrow.isOnFire()) {
                explode(abstractArrow.getDeltaMovement().lengthSqr());
            }
        }
        return super.hurt(damageSource, f);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public void destroy(DamageSource damageSource) {
        double horizontalDistanceSqr = getHorizontalDistanceSqr(getDeltaMovement());
        if (damageSource.isFire() || damageSource.isExplosion() || horizontalDistanceSqr >= 0.009999999776482582d) {
            if (this.fuse < 0) {
                primeFuse();
                this.fuse = this.random.nextInt(20) + this.random.nextInt(20);
                return;
            }
            return;
        }
        super.destroy(damageSource);
        if (!damageSource.isExplosion() && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            spawnAtLocation(Blocks.TNT);
        }
    }

    protected void explode(double d) {
        if (!this.level.isClientSide) {
            double sqrt = Math.sqrt(d);
            if (sqrt > 5.0d) {
                sqrt = 5.0d;
            }
            this.level.explode(this, getX(), getY(), getZ(), (float) (4.0d + (this.random.nextDouble() * 1.5d * sqrt)), Explosion.BlockInteraction.BREAK);
            remove();
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        if (f >= 3.0f) {
            float f3 = f / 10.0f;
            explode(f3 * f3);
        }
        return super.causeFallDamage(f, f2);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public void activateMinecart(int i, int i2, int i3, boolean z) {
        if (z && this.fuse < 0) {
            primeFuse();
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 10) {
            primeFuse();
        } else {
            super.handleEntityEvent(b);
        }
    }

    public void primeFuse() {
        this.fuse = 80;
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte) 10);
            if (!isSilent()) {
                this.level.playSound(null, getX(), getY(), getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    public int getFuse() {
        return this.fuse;
    }

    public boolean isPrimed() {
        return this.fuse > -1;
    }

    @Override // net.minecraft.world.entity.Entity
    public float getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState, float f) {
        if (isPrimed() && (blockState.is(BlockTags.RAILS) || blockGetter.getBlockState(blockPos.above()).is(BlockTags.RAILS))) {
            return 0.0f;
        }
        return super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState, f);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
        if (isPrimed() && (blockState.is(BlockTags.RAILS) || blockGetter.getBlockState(blockPos.above()).is(BlockTags.RAILS))) {
            return false;
        }
        return super.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, f);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("TNTFuse", 99)) {
            this.fuse = compoundTag.getInt("TNTFuse");
        }
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("TNTFuse", this.fuse);
    }
}
