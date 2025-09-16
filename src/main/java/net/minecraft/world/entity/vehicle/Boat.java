package net.minecraft.world.entity.vehicle;

import com.google.common.collect.UnmodifiableIterator;
import java.util.List;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/Boat.class */
public class Boat extends Entity {
    private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_LEFT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_RIGHT = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ID_BUBBLE_TIME = SynchedEntityData.defineId(Boat.class, EntityDataSerializers.INT);
    private final float[] paddlePositions;
    private float invFriction;
    private float outOfControlTicks;
    private float deltaRotation;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputUp;
    private boolean inputDown;
    private double waterLevel;
    private float landFriction;
    private Status status;
    private Status oldStatus;
    private double lastYd;
    private boolean isAboveBubbleColumn;
    private boolean bubbleColumnDirectionIsDown;
    private float bubbleMultiplier;
    private float bubbleAngle;
    private float bubbleAngleO;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/Boat$Status.class */
    public enum Status {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR
    }

    public Boat(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
        this.paddlePositions = new float[2];
        this.blocksBuilding = true;
    }

    public Boat(Level level, double d, double d2, double d3) {
        this(EntityType.BOAT, level);
        setPos(d, d2, d3);
        setDeltaMovement(Vec3.ZERO);
        this.xo = d;
        this.yo = d2;
        this.zo = d3;
    }

    @Override // net.minecraft.world.entity.Entity
    protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return entityDimensions.height;
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        this.entityData.define(DATA_ID_HURT, 0);
        this.entityData.define(DATA_ID_HURTDIR, 1);
        this.entityData.define(DATA_ID_DAMAGE, Float.valueOf(0.0f));
        this.entityData.define(DATA_ID_TYPE, Integer.valueOf(Type.OAK.ordinal()));
        this.entityData.define(DATA_ID_PADDLE_LEFT, false);
        this.entityData.define(DATA_ID_PADDLE_RIGHT, false);
        this.entityData.define(DATA_ID_BUBBLE_TIME, 0);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean canCollideWith(Entity entity) {
        return canVehicleCollide(this, entity);
    }

    public static boolean canVehicleCollide(Entity entity, Entity entity2) {
        return (entity2.canBeCollidedWith() || entity2.isPushable()) && !entity.isPassengerOfSameVehicle(entity2);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPushable() {
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    protected Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(axis, foundRectangle));
    }

    @Override // net.minecraft.world.entity.Entity
    public double getPassengersRidingOffset() {
        return -0.1d;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        if (this.level.isClientSide || this.removed) {
            return true;
        }
        setHurtDir(-getHurtDir());
        setHurtTime(10);
        setDamage(getDamage() + (f * 10.0f));
        markHurt();
        boolean z = (damageSource.getEntity() instanceof Player) && ((Player) damageSource.getEntity()).abilities.instabuild;
        if (z || getDamage() > 40.0f) {
            if (!z && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                spawnAtLocation(getDropItem());
            }
            remove();
            return true;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public void onAboveBubbleCol(boolean z) {
        if (!this.level.isClientSide) {
            this.isAboveBubbleColumn = true;
            this.bubbleColumnDirectionIsDown = z;
            if (getBubbleTime() == 0) {
                setBubbleTime(60);
            }
        }
        this.level.addParticle(ParticleTypes.SPLASH, getX() + this.random.nextFloat(), getY() + 0.7d, getZ() + this.random.nextFloat(), 0.0d, 0.0d, 0.0d);
        if (this.random.nextInt(20) == 0) {
            this.level.playLocalSound(getX(), getY(), getZ(), getSwimSplashSound(), getSoundSource(), 1.0f, 0.8f + (0.4f * this.random.nextFloat()), false);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void push(Entity entity) {
        if (entity instanceof Boat) {
            if (entity.getBoundingBox().minY < getBoundingBox().maxY) {
                super.push(entity);
            }
        } else if (entity.getBoundingBox().minY <= getBoundingBox().minY) {
            super.push(entity);
        }
    }

    public Item getDropItem() {
        switch (getBoatType()) {
            case OAK:
            default:
                return Items.OAK_BOAT;
            case SPRUCE:
                return Items.SPRUCE_BOAT;
            case BIRCH:
                return Items.BIRCH_BOAT;
            case JUNGLE:
                return Items.JUNGLE_BOAT;
            case ACACIA:
                return Items.ACACIA_BOAT;
            case DARK_OAK:
                return Items.DARK_OAK_BOAT;
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void animateHurt() {
        setHurtDir(-getHurtDir());
        setHurtTime(10);
        setDamage(getDamage() * 11.0f);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPickable() {
        return !this.removed;
    }

    @Override // net.minecraft.world.entity.Entity
    public void lerpTo(double d, double d2, double d3, float f, float f2, int i, boolean z) {
        this.lerpX = d;
        this.lerpY = d2;
        this.lerpZ = d3;
        this.lerpYRot = f;
        this.lerpXRot = f2;
        this.lerpSteps = 10;
    }

    @Override // net.minecraft.world.entity.Entity
    public Direction getMotionDirection() {
        return getDirection().getClockWise();
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick() {
        this.oldStatus = this.status;
        this.status = this.getStatus();
        if (this.status != Boat.Status.UNDER_WATER && this.status != Boat.Status.UNDER_FLOWING_WATER) {
            this.outOfControlTicks = 0.0F;
        } else {
            ++this.outOfControlTicks;
        }

        if (!this.level.isClientSide && this.outOfControlTicks >= 60.0F) {
            this.ejectPassengers();
        }

        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        super.tick();
        this.tickLerp();
        if (this.isControlledByLocalInstance()) {
            if (this.getPassengers().isEmpty() || !(this.getPassengers().get(0) instanceof Player)) {
                this.setPaddleState(false, false);
            }

            this.floatBoat();
            if (this.level.isClientSide) {
                this.controlBoat();
                this.level.sendPacketToServer(new ServerboundPaddleBoatPacket(this.getPaddleState(0), this.getPaddleState(1)));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }

        this.tickBubbleColumn();

        for(int var1 = 0; var1 <= 1; ++var1) {
            if (this.getPaddleState(var1)) {
                if (!this.isSilent()
                        && (double)(this.paddlePositions[var1] % (float) (Math.PI * 2)) <= (float) (Math.PI / 4)
                        && ((double)this.paddlePositions[var1] + (float) (Math.PI / 8)) % (float) (Math.PI * 2) >= (float) (Math.PI / 4)) {
                    SoundEvent var2x = this.getPaddleSound();
                    if (var2x != null) {
                        Vec3 var3xx = this.getViewVector(1.0F);
                        double var4xxx = var1 == 1 ? -var3xx.z : var3xx.z;
                        double var6xxxx = var1 == 1 ? var3xx.x : -var3xx.x;
                        this.level
                                .playSound(
                                        null, this.getX() + var4xxx, this.getY(), this.getZ() + var6xxxx, var2x, this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat()
                                );
                    }
                }

                this.paddlePositions[var1] = (float)((double)this.paddlePositions[var1] + (float) (Math.PI / 8));
            } else {
                this.paddlePositions[var1] = 0.0F;
            }
        }

        this.checkInsideBlocks();
        List<Entity> var8 = this.level.getEntities(this, this.getBoundingBox().inflate(0.2F, -0.01F, 0.2F), EntitySelector.pushableBy(this));
        if (!var8.isEmpty()) {
            boolean var9x = !this.level.isClientSide && !(this.getControllingPassenger() instanceof Player);

            for(int var10xx = 0; var10xx < var8.size(); ++var10xx) {
                Entity var11xxx = var8.get(var10xx);
                if (!var11xxx.hasPassenger(this)) {
                    if (var9x
                            && this.getPassengers().size() < 2
                            && !var11xxx.isPassenger()
                            && var11xxx.getBbWidth() < this.getBbWidth()
                            && var11xxx instanceof LivingEntity
                            && !(var11xxx instanceof WaterAnimal)
                  && !(var11xxx instanceof Player)) {
                        var11xxx.startRiding(this);
                    } else {
                        this.push(var11xxx);
                    }
                }
            }
        }
    }

    private void tickBubbleColumn() {
        if (this.level.isClientSide) {
            if (getBubbleTime() > 0) {
                this.bubbleMultiplier += 0.05f;
            } else {
                this.bubbleMultiplier -= 0.1f;
            }
            this.bubbleMultiplier = Mth.clamp(this.bubbleMultiplier, 0.0f, 1.0f);
            this.bubbleAngleO = this.bubbleAngle;
            this.bubbleAngle = 10.0f * ((float) Math.sin(0.5f * this.level.getGameTime())) * this.bubbleMultiplier;
            return;
        }
        if (!this.isAboveBubbleColumn) {
            setBubbleTime(0);
        }
        int bubbleTime = getBubbleTime();
        if (bubbleTime > 0) {
            int i = bubbleTime - 1;
            setBubbleTime(i);
            if ((60 - i) - 1 > 0 && i == 0) {
                setBubbleTime(0);
                Vec3 deltaMovement = getDeltaMovement();
                if (this.bubbleColumnDirectionIsDown) {
                    setDeltaMovement(deltaMovement.add(0.0d, -0.7d, 0.0d));
                    ejectPassengers();
                } else {
                    setDeltaMovement(deltaMovement.x, hasPassenger(Player.class) ? 2.7d : 0.6d, deltaMovement.z);
                }
            }
            this.isAboveBubbleColumn = false;
        }
    }

    @Nullable
    protected SoundEvent getPaddleSound() {
        switch (getStatus()) {
            case IN_WATER:
            case UNDER_WATER:
            case UNDER_FLOWING_WATER:
                return SoundEvents.BOAT_PADDLE_WATER;
            case ON_LAND:
                return SoundEvents.BOAT_PADDLE_LAND;
            case IN_AIR:
            default:
                return null;
        }
    }

    private void tickLerp() {
        if (isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            setPacketCoordinates(getX(), getY(), getZ());
        }
        if (this.lerpSteps <= 0) {
            return;
        }
        double x = getX() + ((this.lerpX - getX()) / this.lerpSteps);
        double y = getY() + ((this.lerpY - getY()) / this.lerpSteps);
        double z = getZ() + ((this.lerpZ - getZ()) / this.lerpSteps);
        this.yRot = (float) (this.yRot + (Mth.wrapDegrees(this.lerpYRot - this.yRot) / this.lerpSteps));
        this.xRot = (float) (this.xRot + ((this.lerpXRot - this.xRot) / this.lerpSteps));
        this.lerpSteps--;
        setPos(x, y, z);
        setRot(this.yRot, this.xRot);
    }

    public void setPaddleState(boolean z, boolean z2) {
        this.entityData.set(DATA_ID_PADDLE_LEFT, Boolean.valueOf(z));
        this.entityData.set(DATA_ID_PADDLE_RIGHT, Boolean.valueOf(z2));
    }

    public float getRowingTime(int i, float f) {
        if (getPaddleState(i)) {
            return (float) Mth.clampedLerp(this.paddlePositions[i] - 0.39269909262657166d, this.paddlePositions[i], f);
        }
        return 0.0f;
    }

    private Status getStatus() {
        Status isUnderwater = isUnderwater();
        if (isUnderwater != null) {
            this.waterLevel = getBoundingBox().maxY;
            return isUnderwater;
        }
        if (checkInWater()) {
            return Status.IN_WATER;
        }
        float groundFriction = getGroundFriction();
        if (groundFriction > 0.0f) {
            this.landFriction = groundFriction;
            return Status.ON_LAND;
        }
        return Status.IN_AIR;
    }

    /* JADX WARN: Code restructure failed: missing block: B:17:0x00c4, code lost:

        r14 = r14 + 1;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public float getWaterLevelAbove() {
        AABB var1 = this.getBoundingBox();
        int var2x = Mth.floor(var1.minX);
        int var3xx = Mth.ceil(var1.maxX);
        int var4xxx = Mth.floor(var1.maxY);
        int var5xxxx = Mth.ceil(var1.maxY - this.lastYd);
        int var6xxxxx = Mth.floor(var1.minZ);
        int var7xxxxxx = Mth.ceil(var1.maxZ);
        BlockPos.MutableBlockPos var8xxxxxxx = new BlockPos.MutableBlockPos();

        label39:
        for(int var9xxxxxxxx = var4xxx; var9xxxxxxxx < var5xxxx; ++var9xxxxxxxx) {
            float var10xxxxxxxxx = 0.0F;

            for(int var11xxxxxxxxxx = var2x; var11xxxxxxxxxx < var3xx; ++var11xxxxxxxxxx) {
                for(int var12xxxxxxxxxxx = var6xxxxx; var12xxxxxxxxxxx < var7xxxxxx; ++var12xxxxxxxxxxx) {
                    var8xxxxxxx.set(var11xxxxxxxxxx, var9xxxxxxxx, var12xxxxxxxxxxx);
                    FluidState var13xxxxxxxxxxxx = this.level.getFluidState(var8xxxxxxx);
                    if (var13xxxxxxxxxxxx.is(FluidTags.WATER)) {
                        var10xxxxxxxxx = Math.max(var10xxxxxxxxx, var13xxxxxxxxxxxx.getHeight(this.level, var8xxxxxxx));
                    }

                    if (var10xxxxxxxxx >= 1.0F) {
                        continue label39;
                    }
                }
            }

            if (var10xxxxxxxxx < 1.0F) {
                return (float)var8xxxxxxx.getY() + var10xxxxxxxxx;
            }
        }

        return (float)(var5xxxx + 1);
    }

    public float getGroundFriction() {
        AABB boundingBox = getBoundingBox();
        AABB aabb = new AABB(boundingBox.minX, boundingBox.minY - 0.001d, boundingBox.minZ, boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        int floor = Mth.floor(aabb.minX) - 1;
        int ceil = Mth.ceil(aabb.maxX) + 1;
        int floor2 = Mth.floor(aabb.minY) - 1;
        int ceil2 = Mth.ceil(aabb.maxY) + 1;
        int floor3 = Mth.floor(aabb.minZ) - 1;
        int ceil3 = Mth.ceil(aabb.maxZ) + 1;
        VoxelShape create = Shapes.create(aabb);
        float f = 0.0f;
        int i = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int i2 = floor;
        while (i2 < ceil) {
            int i3 = floor3;
            while (i3 < ceil3) {
                int i4 = ((i2 == floor || i2 == ceil - 1) ? 1 : 0) + ((i3 == floor3 || i3 == ceil3 - 1) ? 1 : 0);
                if (i4 != 2) {
                    for (int i5 = floor2; i5 < ceil2; i5++) {
                        if (i4 <= 0 || (i5 != floor2 && i5 != ceil2 - 1)) {
                            mutableBlockPos.set(i2, i5, i3);
                            BlockState blockState = this.level.getBlockState(mutableBlockPos);
                            if (!(blockState.getBlock() instanceof WaterlilyBlock) && Shapes.joinIsNotEmpty(blockState.getCollisionShape(this.level, mutableBlockPos).move(i2, i5, i3), create, BooleanOp.AND)) {
                                f += blockState.getBlock().getFriction();
                                i++;
                            }
                        }
                    }
                }
                i3++;
            }
            i2++;
        }
        return f / i;
    }

    private boolean checkInWater() {
        AABB boundingBox = getBoundingBox();
        int floor = Mth.floor(boundingBox.minX);
        int ceil = Mth.ceil(boundingBox.maxX);
        int floor2 = Mth.floor(boundingBox.minY);
        int ceil2 = Mth.ceil(boundingBox.minY + 0.001d);
        int floor3 = Mth.floor(boundingBox.minZ);
        int ceil3 = Mth.ceil(boundingBox.maxZ);
        boolean z = false;
        this.waterLevel = Double.MIN_VALUE;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = floor; i < ceil; i++) {
            for (int i2 = floor2; i2 < ceil2; i2++) {
                for (int i3 = floor3; i3 < ceil3; i3++) {
                    mutableBlockPos.set(i, i2, i3);
                    FluidState fluidState = this.level.getFluidState(mutableBlockPos);
                    if (fluidState.is(FluidTags.WATER)) {
                        float height = i2 + fluidState.getHeight(this.level, mutableBlockPos);
                        this.waterLevel = Math.max(height, this.waterLevel);
                        z |= boundingBox.minY < ((double) height);
                    }
                }
            }
        }
        return z;
    }

    @Nullable
    private Status isUnderwater() {
        AABB boundingBox = getBoundingBox();
        double d = boundingBox.maxY + 0.001d;
        int floor = Mth.floor(boundingBox.minX);
        int ceil = Mth.ceil(boundingBox.maxX);
        int floor2 = Mth.floor(boundingBox.maxY);
        int ceil2 = Mth.ceil(d);
        int floor3 = Mth.floor(boundingBox.minZ);
        int ceil3 = Mth.ceil(boundingBox.maxZ);
        boolean z = false;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = floor; i < ceil; i++) {
            for (int i2 = floor2; i2 < ceil2; i2++) {
                for (int i3 = floor3; i3 < ceil3; i3++) {
                    mutableBlockPos.set(i, i2, i3);
                    FluidState fluidState = this.level.getFluidState(mutableBlockPos);
                    if (fluidState.is(FluidTags.WATER) && d < mutableBlockPos.getY() + fluidState.getHeight(this.level, mutableBlockPos)) {
                        if (fluidState.isSource()) {
                            z = true;
                        } else {
                            return Status.UNDER_FLOWING_WATER;
                        }
                    }
                }
            }
        }
        if (z) {
            return Status.UNDER_WATER;
        }
        return null;
    }

    private void floatBoat() {
        double d = isNoGravity() ? 0.0d : -0.03999999910593033d;
        double d2 = 0.0d;
        this.invFriction = 0.05f;
        if (this.oldStatus == Status.IN_AIR && this.status != Status.IN_AIR && this.status != Status.ON_LAND) {
            this.waterLevel = getY(1.0d);
            setPos(getX(), (getWaterLevelAbove() - getBbHeight()) + 0.101d, getZ());
            setDeltaMovement(getDeltaMovement().multiply(1.0d, 0.0d, 1.0d));
            this.lastYd = 0.0d;
            this.status = Status.IN_WATER;
            return;
        }
        if (this.status == Status.IN_WATER) {
            d2 = (this.waterLevel - getY()) / getBbHeight();
            this.invFriction = 0.9f;
        } else if (this.status == Status.UNDER_FLOWING_WATER) {
            d = -7.0E-4d;
            this.invFriction = 0.9f;
        } else if (this.status == Status.UNDER_WATER) {
            d2 = 0.009999999776482582d;
            this.invFriction = 0.45f;
        } else if (this.status == Status.IN_AIR) {
            this.invFriction = 0.9f;
        } else if (this.status == Status.ON_LAND) {
            this.invFriction = this.landFriction;
            if (getControllingPassenger() instanceof Player) {
                this.landFriction /= 2.0f;
            }
        }
        Vec3 deltaMovement = getDeltaMovement();
        setDeltaMovement(deltaMovement.x * this.invFriction, deltaMovement.y + d, deltaMovement.z * this.invFriction);
        this.deltaRotation *= this.invFriction;
        if (d2 > 0.0d) {
            Vec3 deltaMovement2 = getDeltaMovement();
            setDeltaMovement(deltaMovement2.x, (deltaMovement2.y + (d2 * 0.06153846016296973d)) * 0.75d, deltaMovement2.z);
        }
    }

    private void controlBoat() {
        if (!isVehicle()) {
            return;
        }
        float f = 0.0f;
        if (this.inputLeft) {
            this.deltaRotation -= 1.0f;
        }
        if (this.inputRight) {
            this.deltaRotation += 1.0f;
        }
        if (this.inputRight != this.inputLeft && !this.inputUp && !this.inputDown) {
            f = 0.0f + 0.005f;
        }
        this.yRot += this.deltaRotation;
        if (this.inputUp) {
            f += 0.04f;
        }
        if (this.inputDown) {
            f -= 0.005f;
        }
        setDeltaMovement(getDeltaMovement().add(Mth.sin((-this.yRot) * 0.017453292f) * f, 0.0d, Mth.cos(this.yRot * 0.017453292f) * f));
        setPaddleState((this.inputRight && !this.inputLeft) || this.inputUp, (this.inputLeft && !this.inputRight) || this.inputUp);
    }

    @Override // net.minecraft.world.entity.Entity
    public void positionRider(Entity entity) {
        if (!hasPassenger(entity)) {
            return;
        }
        float f = 0.0f;
        float passengersRidingOffset = (float) ((this.removed ? 0.009999999776482582d : getPassengersRidingOffset()) + entity.getMyRidingOffset());
        if (getPassengers().size() > 1) {
            if (getPassengers().indexOf(entity) == 0) {
                f = 0.2f;
            } else {
                f = -0.6f;
            }
            if (entity instanceof Animal) {
                f = (float) (f + 0.2d);
            }
        }
        Vec3 yRot = new Vec3(f, 0.0d, 0.0d).yRot(((-this.yRot) * 0.017453292f) - 1.5707964f);
        entity.setPos(getX() + yRot.x, getY() + passengersRidingOffset, getZ() + yRot.z);
        entity.yRot += this.deltaRotation;
        entity.setYHeadRot(entity.getYHeadRot() + this.deltaRotation);
        clampRotation(entity);
        if ((entity instanceof Animal) && getPassengers().size() > 1) {
            int i = entity.getId() % 2 == 0 ? 90 : 270;
            entity.setYBodyRot(((Animal) entity).yBodyRot + i);
            entity.setYHeadRot(entity.getYHeadRot() + i);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Vec3 collisionHorizontalEscapeVector = getCollisionHorizontalEscapeVector(getBbWidth() * Mth.SQRT_OF_TWO, livingEntity.getBbWidth(), this.yRot);
        double x = getX() + collisionHorizontalEscapeVector.x;
        double z = getZ() + collisionHorizontalEscapeVector.z;
        BlockPos blockPos = new BlockPos(x, getBoundingBox().maxY, z);
        BlockPos below = blockPos.below();
        if (!this.level.isWaterAt(below)) {
            double y = blockPos.getY() + this.level.getBlockFloorHeight(blockPos);
            double y2 = blockPos.getY() + this.level.getBlockFloorHeight(below);
            UnmodifiableIterator it = livingEntity.getDismountPoses().iterator();
            while (it.hasNext()) {
                Pose pose = (Pose) it.next();
                Vec3 findDismountLocation = DismountHelper.findDismountLocation(this.level, x, y, z, livingEntity, pose);
                if (findDismountLocation != null) {
                    livingEntity.setPose(pose);
                    return findDismountLocation;
                }
                Vec3 findDismountLocation2 = DismountHelper.findDismountLocation(this.level, x, y2, z, livingEntity, pose);
                if (findDismountLocation2 != null) {
                    livingEntity.setPose(pose);
                    return findDismountLocation2;
                }
            }
        }
        return super.getDismountLocationForPassenger(livingEntity);
    }

    protected void clampRotation(Entity entity) {
        entity.setYBodyRot(this.yRot);
        float wrapDegrees = Mth.wrapDegrees(entity.yRot - this.yRot);
        float clamp = Mth.clamp(wrapDegrees, -105.0f, 105.0f);
        entity.yRotO += clamp - wrapDegrees;
        entity.yRot += clamp - wrapDegrees;
        entity.setYHeadRot(entity.yRot);
    }

    @Override // net.minecraft.world.entity.Entity
    public void onPassengerTurned(Entity entity) {
        clampRotation(entity);
    }

    @Override // net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putString("Type", getBoatType().getName());
    }

    @Override // net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.contains("Type", 8)) {
            setType(Type.byName(compoundTag.getString("Type")));
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (this.outOfControlTicks < 60.0f) {
            if (this.level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void checkFallDamage(double d, boolean z, BlockState blockState, BlockPos blockPos) {
        this.lastYd = getDeltaMovement().y;
        if (isPassenger()) {
            return;
        }
        if (z) {
            if (this.fallDistance > 3.0f) {
                if (this.status != Status.ON_LAND) {
                    this.fallDistance = 0.0f;
                    return;
                }
                causeFallDamage(this.fallDistance, 1.0f);
                if (!this.level.isClientSide && !this.removed) {
                    remove();
                    if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                        for (int i = 0; i < 3; i++) {
                            spawnAtLocation(getBoatType().getPlanks());
                        }
                        for (int i2 = 0; i2 < 2; i2++) {
                            spawnAtLocation(Items.STICK);
                        }
                    }
                }
            }
            this.fallDistance = 0.0f;
            return;
        }
        if (!this.level.getFluidState(blockPosition().below()).is(FluidTags.WATER) && d < 0.0d) {
            this.fallDistance = (float) (this.fallDistance - d);
        }
    }

    public boolean getPaddleState(int i) {
        return ((Boolean) this.entityData.get(i == 0 ? DATA_ID_PADDLE_LEFT : DATA_ID_PADDLE_RIGHT)).booleanValue() && getControllingPassenger() != null;
    }

    public void setDamage(float f) {
        this.entityData.set(DATA_ID_DAMAGE, Float.valueOf(f));
    }

    public float getDamage() {
        return ((Float) this.entityData.get(DATA_ID_DAMAGE)).floatValue();
    }

    public void setHurtTime(int i) {
        this.entityData.set(DATA_ID_HURT, Integer.valueOf(i));
    }

    public int getHurtTime() {
        return ((Integer) this.entityData.get(DATA_ID_HURT)).intValue();
    }

    private void setBubbleTime(int i) {
        this.entityData.set(DATA_ID_BUBBLE_TIME, Integer.valueOf(i));
    }

    private int getBubbleTime() {
        return ((Integer) this.entityData.get(DATA_ID_BUBBLE_TIME)).intValue();
    }

    public float getBubbleAngle(float f) {
        return Mth.lerp(f, this.bubbleAngleO, this.bubbleAngle);
    }

    public void setHurtDir(int i) {
        this.entityData.set(DATA_ID_HURTDIR, Integer.valueOf(i));
    }

    public int getHurtDir() {
        return ((Integer) this.entityData.get(DATA_ID_HURTDIR)).intValue();
    }

    public void setType(Type type) {
        this.entityData.set(DATA_ID_TYPE, Integer.valueOf(type.ordinal()));
    }

    public Type getBoatType() {
        return Type.byId(((Integer) this.entityData.get(DATA_ID_TYPE)).intValue());
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean canAddPassenger(Entity entity) {
        return getPassengers().size() < 2 && !isEyeInFluid(FluidTags.WATER);
    }

    @Override // net.minecraft.world.entity.Entity
    @Nullable
    public Entity getControllingPassenger() {
        List<Entity> passengers = getPassengers();
        if (passengers.isEmpty()) {
            return null;
        }
        return passengers.get(0);
    }

    public void setInput(boolean z, boolean z2, boolean z3, boolean z4) {
        this.inputLeft = z;
        this.inputRight = z2;
        this.inputUp = z3;
        this.inputDown = z4;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/Boat$Type.class */
    public enum Type {
        OAK(Blocks.OAK_PLANKS, "oak"),
        SPRUCE(Blocks.SPRUCE_PLANKS, "spruce"),
        BIRCH(Blocks.BIRCH_PLANKS, "birch"),
        JUNGLE(Blocks.JUNGLE_PLANKS, "jungle"),
        ACACIA(Blocks.ACACIA_PLANKS, "acacia"),
        DARK_OAK(Blocks.DARK_OAK_PLANKS, "dark_oak");

        private final String name;
        private final Block planks;

        Type(Block block, String str) {
            this.name = str;
            this.planks = block;
        }

        public String getName() {
            return this.name;
        }

        public Block getPlanks() {
            return this.planks;
        }

        @Override // java.lang.Enum
        public String toString() {
            return this.name;
        }

        public static Type byId(int i) {
            Type[] values = values();
            if (i < 0 || i >= values.length) {
                i = 0;
            }
            return values[i];
        }

        public static Type byName(String str) {
            Type[] values = values();
            for (int i = 0; i < values.length; i++) {
                if (values[i].getName().equals(str)) {
                    return values[i];
                }
            }
            return values[0];
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isUnderWater() {
        return this.status == Status.UNDER_WATER || this.status == Status.UNDER_FLOWING_WATER;
    }
}
