package net.minecraft.world.entity.projectile;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/EyeOfEnder.class */
public class EyeOfEnder extends Entity implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(EyeOfEnder.class, EntityDataSerializers.ITEM_STACK);

    /* renamed from: tx */
    private double tx;

    /* renamed from: ty */
    private double ty;

    /* renamed from: tz */
    private double tz;
    private int life;
    private boolean surviveAfterDeath;

    public EyeOfEnder(EntityType<? extends EyeOfEnder> entityType, Level level) {
        super(entityType, level);
    }

    public EyeOfEnder(Level level, double d, double d2, double d3) {
        this(EntityType.EYE_OF_ENDER, level);
        this.life = 0;
        setPos(d, d2, d3);
    }

    public void setItem(ItemStack itemStack) {
        if (itemStack.getItem() != Items.ENDER_EYE || itemStack.hasTag()) {
            getEntityData().set(DATA_ITEM_STACK, Util.make(itemStack.copy(), itemStack2 -> {
                itemStack2.setCount(1);
            }));
        }
    }

    private ItemStack getItemRaw() {
        return (ItemStack) getEntityData().get(DATA_ITEM_STACK);
    }

    @Override // net.minecraft.world.entity.projectile.ItemSupplier
    public ItemStack getItem() {
        ItemStack itemRaw = getItemRaw();
        return itemRaw.isEmpty() ? new ItemStack(Items.ENDER_EYE) : itemRaw;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRenderAtSqrDistance(double d) {
        double size = getBoundingBox().getSize() * 4.0d;
        if (Double.isNaN(size)) {
            size = 4.0d;
        }
        double d2 = size * 64.0d;
        return d < d2 * d2;
    }

    public void signalTo(BlockPos blockPos) {
        double x = blockPos.getX();
        int y = blockPos.getY();
        double z = blockPos.getZ();
        double x2 = x - getX();
        double z2 = z - getZ();
        float sqrt = Mth.sqrt((x2 * x2) + (z2 * z2));
        if (sqrt > 12.0f) {
            this.tx = getX() + ((x2 / sqrt) * 12.0d);
            this.tz = getZ() + ((z2 / sqrt) * 12.0d);
            this.ty = getY() + 8.0d;
        } else {
            this.tx = x;
            this.ty = y;
            this.tz = z;
        }
        this.life = 0;
        this.surviveAfterDeath = this.random.nextInt(5) > 0;
    }

    @Override // net.minecraft.world.entity.Entity
    public void lerpMotion(double d, double d2, double d3) {
        setDeltaMovement(d, d2, d3);
        if (this.xRotO == 0.0f && this.yRotO == 0.0f) {
            float sqrt = Mth.sqrt((d * d) + (d3 * d3));
            this.yRot = (float) (Mth.atan2(d, d3) * 57.2957763671875d);
            this.xRot = (float) (Mth.atan2(d2, sqrt) * 57.2957763671875d);
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick() {
        super.tick();
        Vec3 deltaMovement = getDeltaMovement();
        double x = getX() + deltaMovement.x;
        double y = getY() + deltaMovement.y;
        double z = getZ() + deltaMovement.z;
        float sqrt = Mth.sqrt(getHorizontalDistanceSqr(deltaMovement));
        this.xRot = Projectile.lerpRotation(this.xRotO, (float) (Mth.atan2(deltaMovement.y, sqrt) * 57.2957763671875d));
        this.yRot = Projectile.lerpRotation(this.yRotO, (float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 57.2957763671875d));
        if (!this.level.isClientSide) {
            double d = this.tx - x;
            double d2 = this.tz - z;
            float sqrt2 = (float) Math.sqrt((d * d) + (d2 * d2));
            float atan2 = (float) Mth.atan2(d2, d);
            double lerp = Mth.lerp(0.0025d, sqrt, sqrt2);
            double d3 = deltaMovement.y;
            if (sqrt2 < 1.0f) {
                lerp *= 0.8d;
                d3 *= 0.8d;
            }
            deltaMovement = new Vec3(Math.cos(atan2) * lerp, d3 + (((getY() < this.ty ? 1 : -1) - d3) * 0.014999999664723873d), Math.sin(atan2) * lerp);
            setDeltaMovement(deltaMovement);
        }
        if (isInWater()) {
            for (int i = 0; i < 4; i++) {
                this.level.addParticle(ParticleTypes.BUBBLE, x - (deltaMovement.x * 0.25d), y - (deltaMovement.y * 0.25d), z - (deltaMovement.z * 0.25d), deltaMovement.x, deltaMovement.y, deltaMovement.z);
            }
        } else {
            this.level.addParticle(ParticleTypes.PORTAL, ((x - (deltaMovement.x * 0.25d)) + (this.random.nextDouble() * 0.6d)) - 0.3d, (y - (deltaMovement.y * 0.25d)) - 0.5d, ((z - (deltaMovement.z * 0.25d)) + (this.random.nextDouble() * 0.6d)) - 0.3d, deltaMovement.x, deltaMovement.y, deltaMovement.z);
        }
        if (!this.level.isClientSide) {
            setPos(x, y, z);
            this.life++;
            if (this.life > 80 && !this.level.isClientSide) {
                playSound(SoundEvents.ENDER_EYE_DEATH, 1.0f, 1.0f);
                remove();
                if (this.surviveAfterDeath) {
                    this.level.addFreshEntity(new ItemEntity(this.level, getX(), getY(), getZ(), getItem()));
                    return;
                } else {
                    this.level.levelEvent(2003, blockPosition(), 0);
                    return;
                }
            }
            return;
        }
        setPosRaw(x, y, z);
    }

    @Override // net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        ItemStack itemRaw = getItemRaw();
        if (!itemRaw.isEmpty()) {
            compoundTag.put("Item", itemRaw.save(new CompoundTag()));
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        setItem(ItemStack.of(compoundTag.getCompound("Item")));
    }

    @Override // net.minecraft.world.entity.Entity
    public float getBrightness() {
        return 1.0f;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isAttackable() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
