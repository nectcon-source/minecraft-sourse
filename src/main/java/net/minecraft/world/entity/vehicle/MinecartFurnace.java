package net.minecraft.world.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/MinecartFurnace.class */
public class MinecartFurnace extends AbstractMinecart {
    private int fuel;
    public double xPush;
    public double zPush;
    private static final EntityDataAccessor<Boolean> DATA_ID_FUEL = SynchedEntityData.defineId(MinecartFurnace.class, EntityDataSerializers.BOOLEAN);
    private static final Ingredient INGREDIENT = Ingredient.of(Items.COAL, Items.CHARCOAL);

    public MinecartFurnace(EntityType<? extends MinecartFurnace> entityType, Level level) {
        super(entityType, level);
    }

    public MinecartFurnace(Level level, double d, double d2, double d3) {
        super(EntityType.FURNACE_MINECART, level, d, d2, d3);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.FURNACE;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_FUEL, false);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        if (!this.level.isClientSide()) {
            if (this.fuel > 0) {
                this.fuel--;
            }
            if (this.fuel <= 0) {
                this.xPush = 0.0d;
                this.zPush = 0.0d;
            }
            setHasFuel(this.fuel > 0);
        }
        if (hasFuel() && this.random.nextInt(4) == 0) {
            this.level.addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.8d, getZ(), 0.0d, 0.0d, 0.0d);
        }
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    protected double getMaxSpeed() {
        return 0.2d;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public void destroy(DamageSource damageSource) {
        super.destroy(damageSource);
        if (!damageSource.isExplosion() && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            spawnAtLocation(Blocks.FURNACE);
        }
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    protected void moveAlongTrack(BlockPos blockPos, BlockState blockState) {
        super.moveAlongTrack(blockPos, blockState);
        Vec3 deltaMovement = getDeltaMovement();
        double horizontalDistanceSqr = getHorizontalDistanceSqr(deltaMovement);
        double d = (this.xPush * this.xPush) + (this.zPush * this.zPush);
        if (d > 1.0E-4d && horizontalDistanceSqr > 0.001d) {
            double sqrt = Mth.sqrt(horizontalDistanceSqr);
            double sqrt2 = Mth.sqrt(d);
            this.xPush = (deltaMovement.x / sqrt) * sqrt2;
            this.zPush = (deltaMovement.z / sqrt) * sqrt2;
        }
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    protected void applyNaturalSlowdown() {
        double d = (this.xPush * this.xPush) + (this.zPush * this.zPush);
        if (d > 1.0E-7d) {
            double sqrt = Mth.sqrt(d);
            this.xPush /= sqrt;
            this.zPush /= sqrt;
            setDeltaMovement(getDeltaMovement().multiply(0.8d, 0.0d, 0.8d).add(this.xPush, 0.0d, this.zPush));
        } else {
            setDeltaMovement(getDeltaMovement().multiply(0.98d, 0.0d, 0.98d));
        }
        super.applyNaturalSlowdown();
    }

    @Override // net.minecraft.world.entity.Entity
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (INGREDIENT.test(itemInHand) && this.fuel + 3600 <= 32000) {
            if (!player.abilities.instabuild) {
                itemInHand.shrink(1);
            }
            this.fuel += 3600;
        }
        if (this.fuel > 0) {
            this.xPush = getX() - player.getX();
            this.zPush = getZ() - player.getZ();
        }
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putDouble("PushX", this.xPush);
        compoundTag.putDouble("PushZ", this.zPush);
        compoundTag.putShort("Fuel", (short) this.fuel);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.xPush = compoundTag.getDouble("PushX");
        this.zPush = compoundTag.getDouble("PushZ");
        this.fuel = compoundTag.getShort("Fuel");
    }

    protected boolean hasFuel() {
        return ((Boolean) this.entityData.get(DATA_ID_FUEL)).booleanValue();
    }

    protected void setHasFuel(boolean z) {
        this.entityData.set(DATA_ID_FUEL, Boolean.valueOf(z));
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public BlockState getDefaultDisplayBlockState() {
        return (BlockState) ((BlockState) Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH)).setValue(FurnaceBlock.LIT, Boolean.valueOf(hasFuel()));
    }
}
