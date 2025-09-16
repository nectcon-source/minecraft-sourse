package net.minecraft.world.entity.vehicle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.util.Pair;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/AbstractMinecart.class */
public abstract class AbstractMinecart extends Entity {
    private boolean flipped;
    private int lSteps;

    /* renamed from: lx */
    private double lx;

    /* renamed from: ly */
    private double ly;

    /* renamed from: lz */
    private double lz;
    private double lyr;
    private double lxr;
    private double lxd;
    private double lyd;
    private double lzd;
    private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_BLOCK = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_OFFSET = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ID_CUSTOM_DISPLAY = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.BOOLEAN);
    private static final ImmutableMap<Pose, ImmutableList<Integer>> POSE_DISMOUNT_HEIGHTS = ImmutableMap.of(Pose.STANDING, ImmutableList.of(0, 1, -1), Pose.CROUCHING, ImmutableList.of(0, 1, -1), Pose.SWIMMING, ImmutableList.of(0, 1));
    private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS =  Util.make(Maps.newEnumMap(RailShape.class), enumMap -> {
        Vec3i normal = Direction.WEST.getNormal();
        Vec3i normal2 = Direction.EAST.getNormal();
        Vec3i normal3 = Direction.NORTH.getNormal();
        Vec3i normal4 = Direction.SOUTH.getNormal();
        Vec3i below = normal.below();
        Vec3i below2 = normal2.below();
        Vec3i below3 = normal3.below();
        Vec3i below4 = normal4.below();
        enumMap.put( RailShape.NORTH_SOUTH,  Pair.of(normal3, normal4));
        enumMap.put( RailShape.EAST_WEST,  Pair.of(normal, normal2));
        enumMap.put( RailShape.ASCENDING_EAST,  Pair.of(below, normal2));
        enumMap.put( RailShape.ASCENDING_WEST,  Pair.of(normal, below2));
        enumMap.put( RailShape.ASCENDING_NORTH,  Pair.of(normal3, below4));
        enumMap.put( RailShape.ASCENDING_SOUTH,  Pair.of(below3, normal4));
        enumMap.put( RailShape.SOUTH_EAST,  Pair.of(normal4, normal2));
        enumMap.put( RailShape.SOUTH_WEST,  Pair.of(normal4, normal));
        enumMap.put( RailShape.NORTH_WEST,  Pair.of(normal3, normal));
        enumMap.put( RailShape.NORTH_EAST,  Pair.of(normal3, normal2));
    });

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/AbstractMinecart$Type.class */
    public enum Type {
        RIDEABLE,
        CHEST,
        FURNACE,
        TNT,
        SPAWNER,
        HOPPER,
        COMMAND_BLOCK
    }

    public abstract Type getMinecartType();

    protected AbstractMinecart(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    protected AbstractMinecart(EntityType<?> entityType, Level level, double d, double d2, double d3) {
        this(entityType, level);
        setPos(d, d2, d3);
        setDeltaMovement(Vec3.ZERO);
        this.xo = d;
        this.yo = d2;
        this.zo = d3;
    }

    public static AbstractMinecart createMinecart(Level level, double d, double d2, double d3, Type type) {
        if (type == Type.CHEST) {
            return new MinecartChest(level, d, d2, d3);
        }
        if (type == Type.FURNACE) {
            return new MinecartFurnace(level, d, d2, d3);
        }
        if (type == Type.TNT) {
            return new MinecartTNT(level, d, d2, d3);
        }
        if (type == Type.SPAWNER) {
            return new MinecartSpawner(level, d, d2, d3);
        }
        if (type == Type.HOPPER) {
            return new MinecartHopper(level, d, d2, d3);
        }
        if (type == Type.COMMAND_BLOCK) {
            return new MinecartCommandBlock(level, d, d2, d3);
        }
        return new Minecart(level, d, d2, d3);
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
        this.entityData.define(DATA_ID_DISPLAY_BLOCK, Integer.valueOf(Block.getId(Blocks.AIR.defaultBlockState())));
        this.entityData.define(DATA_ID_DISPLAY_OFFSET, 6);
        this.entityData.define(DATA_ID_CUSTOM_DISPLAY, false);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean canCollideWith(Entity entity) {
        return Boat.canVehicleCollide(this, entity);
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
        return 0.0d;
    }

    @Override // net.minecraft.world.entity.Entity
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Direction var2 = this.getMotionDirection();
        if (var2.getAxis() == Direction.Axis.Y) {
            return super.getDismountLocationForPassenger(livingEntity);
        } else {
            int[][] var3 = DismountHelper.offsetsForDirection(var2);
            BlockPos var4x = this.blockPosition();
            BlockPos.MutableBlockPos var5xx = new BlockPos.MutableBlockPos();
            ImmutableList<Pose> var6xxx = livingEntity.getDismountPoses();
            UnmodifiableIterator var7 = var6xxx.iterator();

            while(var7.hasNext()) {
                Pose var8xxxx = (Pose)var7.next();
                EntityDimensions var9xxxxx = livingEntity.getDimensions(var8xxxx);
                float var10xxxxxx = Math.min(var9xxxxx.width, 1.0F) / 2.0F;
                UnmodifiableIterator var11 = ((ImmutableList)POSE_DISMOUNT_HEIGHTS.get(var8xxxx)).iterator();

                while(var11.hasNext()) {
                    int var12xxxxxxx = (int) var11.next();

                    for(int[] var16 : var3) {
                        var5xx.set(var4x.getX() + var16[0], var4x.getY() + var12xxxxxxx, var4x.getZ() + var16[1]);
                        double var17xxxxxxxx = this.level
                                .getBlockFloorHeight(DismountHelper.nonClimbableShape(this.level, var5xx), () -> DismountHelper.nonClimbableShape(this.level, var5xx.below()));
                        if (DismountHelper.isBlockFloorValid(var17xxxxxxxx)) {
                            AABB var19xxxxxxxxx = new AABB((double)(-var10xxxxxx), 0.0, (double)(-var10xxxxxx), (double)var10xxxxxx, (double)var9xxxxx.height, (double)var10xxxxxx);
                            Vec3 var20xxxxxxxxxx = Vec3.upFromBottomCenterOf(var5xx, var17xxxxxxxx);
                            if (DismountHelper.canDismountTo(this.level, livingEntity, var19xxxxxxxxx.move(var20xxxxxxxxxx))) {
                                livingEntity.setPose(var8xxxx);
                                return var20xxxxxxxxxx;
                            }
                        }
                    }
                }
            }

            double var21xxxx = this.getBoundingBox().maxY;
            var5xx.set((double)var4x.getX(), var21xxxx, (double)var4x.getZ());
            UnmodifiableIterator var22 = var6xxx.iterator();

            while(var22.hasNext()) {
                Pose var23xxxxx = (Pose)var22.next();
                double var24xxxxxx = (double)livingEntity.getDimensions(var23xxxxx).height;
                int var25xxxxxxx = Mth.ceil(var21xxxx - (double)var5xx.getY() + var24xxxxxx);
                double var26xxxxxxxx = DismountHelper.findCeilingFrom(var5xx, var25xxxxxxx, var1x -> this.level.getBlockState(var1x).getCollisionShape(this.level, var1x));
                if (var21xxxx + var24xxxxxx <= var26xxxxxxxx) {
                    livingEntity.setPose(var23xxxxx);
                    break;
                }
            }

            return super.getDismountLocationForPassenger(livingEntity);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.level.isClientSide || this.removed) {
            return true;
        }
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        setHurtDir(-getHurtDir());
        setHurtTime(10);
        markHurt();
        setDamage(getDamage() + (f * 10.0f));
        boolean z = (damageSource.getEntity() instanceof Player) && ((Player) damageSource.getEntity()).abilities.instabuild;
        if (z || getDamage() > 40.0f) {
            ejectPassengers();
            if (!z || hasCustomName()) {
                destroy(damageSource);
                return true;
            }
            remove();
            return true;
        }
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    protected float getBlockSpeedFactor() {
        if (this.level.getBlockState(blockPosition()).is(BlockTags.RAILS)) {
            return 1.0f;
        }
        return super.getBlockSpeedFactor();
    }

    public void destroy(DamageSource damageSource) {
        remove();
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemStack = new ItemStack(Items.MINECART);
            if (hasCustomName()) {
                itemStack.setHoverName(getCustomName());
            }
            spawnAtLocation(itemStack);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void animateHurt() {
        setHurtDir(-getHurtDir());
        setHurtTime(10);
        setDamage(getDamage() + (getDamage() * 10.0f));
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPickable() {
        return !this.removed;
    }

    private static Pair<Vec3i, Vec3i> exits(RailShape railShape) {
        return EXITS.get(railShape);
    }

    @Override // net.minecraft.world.entity.Entity
    public Direction getMotionDirection() {
        return this.flipped ? getDirection().getOpposite().getClockWise() : getDirection().getClockWise();
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick() {
        if (getHurtTime() > 0) {
            setHurtTime(getHurtTime() - 1);
        }
        if (getDamage() > 0.0f) {
            setDamage(getDamage() - 1.0f);
        }
        if (getY() < -64.0d) {
            outOfWorld();
        }
        handleNetherPortal();
        if (this.level.isClientSide) {
            if (this.lSteps > 0) {
                double x = getX() + ((this.lx - getX()) / this.lSteps);
                double y = getY() + ((this.ly - getY()) / this.lSteps);
                double z = getZ() + ((this.lz - getZ()) / this.lSteps);
                this.yRot = (float) (this.yRot + (Mth.wrapDegrees(this.lyr - this.yRot) / this.lSteps));
                this.xRot = (float) (this.xRot + ((this.lxr - this.xRot) / this.lSteps));
                this.lSteps--;
                setPos(x, y, z);
                setRot(this.yRot, this.xRot);
                return;
            }
            reapplyPosition();
            setRot(this.yRot, this.xRot);
            return;
        }
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0d, -0.04d, 0.0d));
        }
        int floor = Mth.floor(getX());
        int floor2 = Mth.floor(getY());
        int floor3 = Mth.floor(getZ());
        if (this.level.getBlockState(new BlockPos(floor, floor2 - 1, floor3)).is(BlockTags.RAILS)) {
            floor2--;
        }
        BlockPos blockPos = new BlockPos(floor, floor2, floor3);
        BlockState blockState = this.level.getBlockState(blockPos);
        if (BaseRailBlock.isRail(blockState)) {
            moveAlongTrack(blockPos, blockState);
            if (blockState.is(Blocks.ACTIVATOR_RAIL)) {
                activateMinecart(floor, floor2, floor3, ((Boolean) blockState.getValue(PoweredRailBlock.POWERED)).booleanValue());
            }
        } else {
            comeOffTrack();
        }
        checkInsideBlocks();
        this.xRot = 0.0f;
        double x2 = this.xo - getX();
        double z2 = this.zo - getZ();
        if ((x2 * x2) + (z2 * z2) > 0.001d) {
            this.yRot = (float) ((Mth.atan2(z2, x2) * 180.0d) / 3.141592653589793d);
            if (this.flipped) {
                this.yRot += 180.0f;
            }
        }
        double wrapDegrees = Mth.wrapDegrees(this.yRot - this.yRotO);
        if (wrapDegrees < -170.0d || wrapDegrees >= 170.0d) {
            this.yRot += 180.0f;
            this.flipped = !this.flipped;
        }
        setRot(this.yRot, this.xRot);
        if (getMinecartType() == Type.RIDEABLE && getHorizontalDistanceSqr(getDeltaMovement()) > 0.01d) {
            List<Entity> entities = this.level.getEntities(this, getBoundingBox().inflate(0.20000000298023224d, 0.0d, 0.20000000298023224d), EntitySelector.pushableBy(this));
            if (!entities.isEmpty()) {
                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = entities.get(i);
                    if ((entity instanceof Player) || (entity instanceof IronGolem) || (entity instanceof AbstractMinecart) || isVehicle() || entity.isPassenger()) {
                        entity.push(this);
                    } else {
                        entity.startRiding(this);
                    }
                }
            }
        } else {
            for (Entity entity2 : this.level.getEntities(this, getBoundingBox().inflate(0.20000000298023224d, 0.0d, 0.20000000298023224d))) {
                if (!hasPassenger(entity2) && entity2.isPushable() && (entity2 instanceof AbstractMinecart)) {
                    entity2.push(this);
                }
            }
        }
        updateInWaterStateAndDoFluidPushing();
        if (isInLava()) {
            lavaHurt();
            this.fallDistance *= 0.5f;
        }
        this.firstTick = false;
    }

    protected double getMaxSpeed() {
        return 0.4d;
    }

    public void activateMinecart(int i, int i2, int i3, boolean z) {
    }

    protected void comeOffTrack() {
        double maxSpeed = getMaxSpeed();
        Vec3 deltaMovement = getDeltaMovement();
        setDeltaMovement(Mth.clamp(deltaMovement.x, -maxSpeed, maxSpeed), deltaMovement.y, Mth.clamp(deltaMovement.z, -maxSpeed, maxSpeed));
        if (this.onGround) {
            setDeltaMovement(getDeltaMovement().scale(0.5d));
        }
        move(MoverType.SELF, getDeltaMovement());
        if (!this.onGround) {
            setDeltaMovement(getDeltaMovement().scale(0.95d));
        }
    }

    protected void moveAlongTrack(BlockPos blockPos, BlockState blockState) {
        double d;
        this.fallDistance = 0.0f;
        double x = getX();
        double y = getY();
        double z = getZ();
        Vec3 pos = getPos(x, y, z);
        double y2 = blockPos.getY();
        boolean z2 = false;
        boolean z3 = false;
        BaseRailBlock baseRailBlock = (BaseRailBlock) blockState.getBlock();
        if (baseRailBlock == Blocks.POWERED_RAIL) {
            z2 = ((Boolean) blockState.getValue(PoweredRailBlock.POWERED)).booleanValue();
            z3 = !z2;
        }
        Vec3 deltaMovement = getDeltaMovement();
        RailShape railShape = (RailShape) blockState.getValue(baseRailBlock.getShapeProperty());
        switch (railShape) {
            case ASCENDING_EAST:
                setDeltaMovement(deltaMovement.add(-0.0078125d, 0.0d, 0.0d));
                y2 += 1.0d;
                break;
            case ASCENDING_WEST:
                setDeltaMovement(deltaMovement.add(0.0078125d, 0.0d, 0.0d));
                y2 += 1.0d;
                break;
            case ASCENDING_NORTH:
                setDeltaMovement(deltaMovement.add(0.0d, 0.0d, 0.0078125d));
                y2 += 1.0d;
                break;
            case ASCENDING_SOUTH:
                setDeltaMovement(deltaMovement.add(0.0d, 0.0d, -0.0078125d));
                y2 += 1.0d;
                break;
        }
        Vec3 deltaMovement2 = getDeltaMovement();
        Pair<Vec3i, Vec3i> exits = exits(railShape);
        Vec3i vec3i = (Vec3i) exits.getFirst();
        Vec3i vec3i2 = (Vec3i) exits.getSecond();
        double x2 = vec3i2.getX() - vec3i.getX();
        double z4 = vec3i2.getZ() - vec3i.getZ();
        double sqrt = Math.sqrt((x2 * x2) + (z4 * z4));
        if ((deltaMovement2.x * x2) + (deltaMovement2.z * z4) < 0.0d) {
            x2 = -x2;
            z4 = -z4;
        }
        double min = Math.min(2.0d, Math.sqrt(getHorizontalDistanceSqr(deltaMovement2)));
        setDeltaMovement(new Vec3((min * x2) / sqrt, deltaMovement2.y, (min * z4) / sqrt));
        Entity entity = getPassengers().isEmpty() ? null : getPassengers().get(0);
        if (entity instanceof Player) {
            Vec3 deltaMovement3 = entity.getDeltaMovement();
            double horizontalDistanceSqr = getHorizontalDistanceSqr(deltaMovement3);
            double horizontalDistanceSqr2 = getHorizontalDistanceSqr(getDeltaMovement());
            if (horizontalDistanceSqr > 1.0E-4d && horizontalDistanceSqr2 < 0.01d) {
                setDeltaMovement(getDeltaMovement().add(deltaMovement3.x * 0.1d, 0.0d, deltaMovement3.z * 0.1d));
                z3 = false;
            }
        }
        if (z3) {
            if (Math.sqrt(getHorizontalDistanceSqr(getDeltaMovement())) < 0.03d) {
                setDeltaMovement(Vec3.ZERO);
            } else {
                setDeltaMovement(getDeltaMovement().multiply(0.5d, 0.0d, 0.5d));
            }
        }
        double x3 = blockPos.getX() + 0.5d + (vec3i.getX() * 0.5d);
        double z5 = blockPos.getZ() + 0.5d + (vec3i.getZ() * 0.5d);
        double x4 = blockPos.getX() + 0.5d + (vec3i2.getX() * 0.5d);
        double z6 = blockPos.getZ() + 0.5d + (vec3i2.getZ() * 0.5d);
        double d2 = x4 - x3;
        double d3 = z6 - z5;
        if (d2 == 0.0d) {
            d = z - blockPos.getZ();
        } else if (d3 == 0.0d) {
            d = x - blockPos.getX();
        } else {
            d = (((x - x3) * d2) + ((z - z5) * d3)) * 2.0d;
        }
        setPos(x3 + (d2 * d), y2, z5 + (d3 * d));
        double d4 = isVehicle() ? 0.75d : 1.0d;
        double maxSpeed = getMaxSpeed();
        Vec3 deltaMovement4 = getDeltaMovement();
        move(MoverType.SELF, new Vec3(Mth.clamp(d4 * deltaMovement4.x, -maxSpeed, maxSpeed), 0.0d, Mth.clamp(d4 * deltaMovement4.z, -maxSpeed, maxSpeed)));
        if (vec3i.getY() != 0 && Mth.floor(getX()) - blockPos.getX() == vec3i.getX() && Mth.floor(getZ()) - blockPos.getZ() == vec3i.getZ()) {
            setPos(getX(), getY() + vec3i.getY(), getZ());
        } else if (vec3i2.getY() != 0 && Mth.floor(getX()) - blockPos.getX() == vec3i2.getX() && Mth.floor(getZ()) - blockPos.getZ() == vec3i2.getZ()) {
            setPos(getX(), getY() + vec3i2.getY(), getZ());
        }
        applyNaturalSlowdown();
        Vec3 pos2 = getPos(getX(), getY(), getZ());
        if (pos2 != null && pos != null) {
            double d5 = (pos.y - pos2.y) * 0.05d;
            Vec3 deltaMovement5 = getDeltaMovement();
            double sqrt2 = Math.sqrt(getHorizontalDistanceSqr(deltaMovement5));
            if (sqrt2 > 0.0d) {
                setDeltaMovement(deltaMovement5.multiply((sqrt2 + d5) / sqrt2, 1.0d, (sqrt2 + d5) / sqrt2));
            }
            setPos(getX(), pos2.y, getZ());
        }
        int floor = Mth.floor(getX());
        int floor2 = Mth.floor(getZ());
        if (floor != blockPos.getX() || floor2 != blockPos.getZ()) {
            Vec3 deltaMovement6 = getDeltaMovement();
            double sqrt3 = Math.sqrt(getHorizontalDistanceSqr(deltaMovement6));
            setDeltaMovement(sqrt3 * (floor - blockPos.getX()), deltaMovement6.y, sqrt3 * (floor2 - blockPos.getZ()));
        }
        if (z2) {
            Vec3 deltaMovement7 = getDeltaMovement();
            double sqrt4 = Math.sqrt(getHorizontalDistanceSqr(deltaMovement7));
            if (sqrt4 > 0.01d) {
                setDeltaMovement(deltaMovement7.add((deltaMovement7.x / sqrt4) * 0.06d, 0.0d, (deltaMovement7.z / sqrt4) * 0.06d));
                return;
            }
            Vec3 deltaMovement8 = getDeltaMovement();
            double d6 = deltaMovement8.x;
            double d7 = deltaMovement8.z;
            if (railShape == RailShape.EAST_WEST) {
                if (isRedstoneConductor(blockPos.west())) {
                    d6 = 0.02d;
                } else if (isRedstoneConductor(blockPos.east())) {
                    d6 = -0.02d;
                }
            } else if (railShape == RailShape.NORTH_SOUTH) {
                if (isRedstoneConductor(blockPos.north())) {
                    d7 = 0.02d;
                } else if (isRedstoneConductor(blockPos.south())) {
                    d7 = -0.02d;
                }
            } else {
                return;
            }
            setDeltaMovement(d6, deltaMovement8.y, d7);
        }
    }

    private boolean isRedstoneConductor(BlockPos blockPos) {
        return this.level.getBlockState(blockPos).isRedstoneConductor(this.level, blockPos);
    }

    protected void applyNaturalSlowdown() {
        double d = isVehicle() ? 0.997d : 0.96d;
        setDeltaMovement(getDeltaMovement().multiply(d, 0.0d, d));
    }

    @Nullable
    public Vec3 getPosOffs(double d, double d2, double d3, double d4) {
        int floor = Mth.floor(d);
        int floor2 = Mth.floor(d2);
        int floor3 = Mth.floor(d3);
        if (this.level.getBlockState(new BlockPos(floor, floor2 - 1, floor3)).is(BlockTags.RAILS)) {
            floor2--;
        }
        BlockState blockState = this.level.getBlockState(new BlockPos(floor, floor2, floor3));
        if (BaseRailBlock.isRail(blockState)) {
            RailShape railShape = (RailShape) blockState.getValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty());
            double d5 = floor2;
            if (railShape.isAscending()) {
                d5 = floor2 + 1;
            }
            Pair<Vec3i, Vec3i> exits = exits(railShape);
            Vec3i vec3i = (Vec3i) exits.getFirst();
            Vec3i vec3i2 = (Vec3i) exits.getSecond();
            double x = vec3i2.getX() - vec3i.getX();
            double z = vec3i2.getZ() - vec3i.getZ();
            double sqrt = Math.sqrt((x * x) + (z * z));
            double d6 = x / sqrt;
            double d7 = z / sqrt;
            double d8 = d + (d6 * d4);
            double d9 = d3 + (d7 * d4);
            if (vec3i.getY() != 0 && Mth.floor(d8) - floor == vec3i.getX() && Mth.floor(d9) - floor3 == vec3i.getZ()) {
                d5 += vec3i.getY();
            } else if (vec3i2.getY() != 0 && Mth.floor(d8) - floor == vec3i2.getX() && Mth.floor(d9) - floor3 == vec3i2.getZ()) {
                d5 += vec3i2.getY();
            }
            return getPos(d8, d5, d9);
        }
        return null;
    }

    @Nullable
    public Vec3 getPos(double d, double d2, double d3) {
        double d4;
        int floor = Mth.floor(d);
        int floor2 = Mth.floor(d2);
        int floor3 = Mth.floor(d3);
        if (this.level.getBlockState(new BlockPos(floor, floor2 - 1, floor3)).is(BlockTags.RAILS)) {
            floor2--;
        }
        BlockState blockState = this.level.getBlockState(new BlockPos(floor, floor2, floor3));
        if (BaseRailBlock.isRail(blockState)) {
            Pair<Vec3i, Vec3i> exits = exits((RailShape) blockState.getValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty()));
            Vec3i vec3i = (Vec3i) exits.getFirst();
            Vec3i vec3i2 = (Vec3i) exits.getSecond();
            double x = floor + 0.5d + (vec3i.getX() * 0.5d);
            double y = floor2 + 0.0625d + (vec3i.getY() * 0.5d);
            double z = floor3 + 0.5d + (vec3i.getZ() * 0.5d);
            double x2 = floor + 0.5d + (vec3i2.getX() * 0.5d);
            double y2 = floor2 + 0.0625d + (vec3i2.getY() * 0.5d);
            double z2 = floor3 + 0.5d + (vec3i2.getZ() * 0.5d);
            double d5 = x2 - x;
            double d6 = (y2 - y) * 2.0d;
            double d7 = z2 - z;
            if (d5 == 0.0d) {
                d4 = d3 - floor3;
            } else if (d7 == 0.0d) {
                d4 = d - floor;
            } else {
                d4 = (((d - x) * d5) + ((d3 - z) * d7)) * 2.0d;
            }
            double d8 = x + (d5 * d4);
            double d9 = y + (d6 * d4);
            double d10 = z + (d7 * d4);
            if (d6 < 0.0d) {
                d9 += 1.0d;
            } else if (d6 > 0.0d) {
                d9 += 0.5d;
            }
            return new Vec3(d8, d9, d10);
        }
        return null;
    }

    @Override // net.minecraft.world.entity.Entity
    public AABB getBoundingBoxForCulling() {
        AABB boundingBox = getBoundingBox();
        if (hasCustomDisplay()) {
            return boundingBox.inflate(Math.abs(getDisplayOffset()) / 16.0d);
        }
        return boundingBox;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.getBoolean("CustomDisplayTile")) {
            setDisplayBlockState(NbtUtils.readBlockState(compoundTag.getCompound("DisplayState")));
            setDisplayOffset(compoundTag.getInt("DisplayOffset"));
        }
    }

    @Override // net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (hasCustomDisplay()) {
            compoundTag.putBoolean("CustomDisplayTile", true);
            compoundTag.put("DisplayState", NbtUtils.writeBlockState(getDisplayBlockState()));
            compoundTag.putInt("DisplayOffset", getDisplayOffset());
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void push(Entity entity) {
        if (this.level.isClientSide || entity.noPhysics || this.noPhysics || hasPassenger(entity)) {
            return;
        }
        double x = entity.getX() - getX();
        double z = entity.getZ() - getZ();
        double d = (x * x) + (z * z);
        if (d >= 9.999999747378752E-5d) {
            double sqrt = Mth.sqrt(d);
            double d2 = x / sqrt;
            double d3 = z / sqrt;
            double d4 = 1.0d / sqrt;
            if (d4 > 1.0d) {
                d4 = 1.0d;
            }
            double d5 = d2 * d4;
            double d6 = d3 * d4;
            double d7 = d5 * 0.10000000149011612d;
            double d8 = d6 * 0.10000000149011612d;
            double d9 = d7 * (1.0f - this.pushthrough);
            double d10 = d8 * (1.0f - this.pushthrough);
            double d11 = d9 * 0.5d;
            double d12 = d10 * 0.5d;
            if (!(entity instanceof AbstractMinecart)) {
                push(-d11, 0.0d, -d12);
                entity.push(d11 / 4.0d, 0.0d, d12 / 4.0d);
                return;
            }
            if (Math.abs(new Vec3(entity.getX() - getX(), 0.0d, entity.getZ() - getZ()).normalize().dot(new Vec3(Mth.cos(this.yRot * 0.017453292f), 0.0d, Mth.sin(this.yRot * 0.017453292f)).normalize())) < 0.800000011920929d) {
                return;
            }
            Vec3 deltaMovement = getDeltaMovement();
            Vec3 deltaMovement2 = entity.getDeltaMovement();
            if (((AbstractMinecart) entity).getMinecartType() == Type.FURNACE && getMinecartType() != Type.FURNACE) {
                setDeltaMovement(deltaMovement.multiply(0.2d, 1.0d, 0.2d));
                push(deltaMovement2.x - d11, 0.0d, deltaMovement2.z - d12);
                entity.setDeltaMovement(deltaMovement2.multiply(0.95d, 1.0d, 0.95d));
            } else if (((AbstractMinecart) entity).getMinecartType() != Type.FURNACE && getMinecartType() == Type.FURNACE) {
                entity.setDeltaMovement(deltaMovement2.multiply(0.2d, 1.0d, 0.2d));
                entity.push(deltaMovement.x + d11, 0.0d, deltaMovement.z + d12);
                setDeltaMovement(deltaMovement.multiply(0.95d, 1.0d, 0.95d));
            } else {
                double d13 = (deltaMovement2.x + deltaMovement.x) / 2.0d;
                double d14 = (deltaMovement2.z + deltaMovement.z) / 2.0d;
                setDeltaMovement(deltaMovement.multiply(0.2d, 1.0d, 0.2d));
                push(d13 - d11, 0.0d, d14 - d12);
                entity.setDeltaMovement(deltaMovement2.multiply(0.2d, 1.0d, 0.2d));
                entity.push(d13 + d11, 0.0d, d14 + d12);
            }
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public void lerpTo(double d, double d2, double d3, float f, float f2, int i, boolean z) {
        this.lx = d;
        this.ly = d2;
        this.lz = d3;
        this.lyr = f;
        this.lxr = f2;
        this.lSteps = i + 2;
        setDeltaMovement(this.lxd, this.lyd, this.lzd);
    }

    @Override // net.minecraft.world.entity.Entity
    public void lerpMotion(double d, double d2, double d3) {
        this.lxd = d;
        this.lyd = d2;
        this.lzd = d3;
        setDeltaMovement(this.lxd, this.lyd, this.lzd);
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

    public void setHurtDir(int i) {
        this.entityData.set(DATA_ID_HURTDIR, Integer.valueOf(i));
    }

    public int getHurtDir() {
        return ((Integer) this.entityData.get(DATA_ID_HURTDIR)).intValue();
    }

    public BlockState getDisplayBlockState() {
        if (!hasCustomDisplay()) {
            return getDefaultDisplayBlockState();
        }
        return Block.stateById(((Integer) getEntityData().get(DATA_ID_DISPLAY_BLOCK)).intValue());
    }

    public BlockState getDefaultDisplayBlockState() {
        return Blocks.AIR.defaultBlockState();
    }

    public int getDisplayOffset() {
        if (!hasCustomDisplay()) {
            return getDefaultDisplayOffset();
        }
        return ((Integer) getEntityData().get(DATA_ID_DISPLAY_OFFSET)).intValue();
    }

    public int getDefaultDisplayOffset() {
        return 6;
    }

    public void setDisplayBlockState(BlockState blockState) {
        getEntityData().set(DATA_ID_DISPLAY_BLOCK, Integer.valueOf(Block.getId(blockState)));
        setCustomDisplay(true);
    }

    public void setDisplayOffset(int i) {
        getEntityData().set(DATA_ID_DISPLAY_OFFSET, Integer.valueOf(i));
        setCustomDisplay(true);
    }

    public boolean hasCustomDisplay() {
        return ((Boolean) getEntityData().get(DATA_ID_CUSTOM_DISPLAY)).booleanValue();
    }

    public void setCustomDisplay(boolean z) {
        getEntityData().set(DATA_ID_CUSTOM_DISPLAY, Boolean.valueOf(z));
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
