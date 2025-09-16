package net.minecraft.world.entity.projectile;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/FishingHook.class */
public class FishingHook extends Projectile {
    private final Random syncronizedRandom;
    private boolean biting;
    private int outOfWaterTime;
    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_BITING = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.BOOLEAN);
    private int life;
    private int nibble;
    private int timeUntilLured;
    private int timeUntilHooked;
    private float fishAngle;
    private boolean openWater;
    private Entity hookedIn;
    private FishHookState currentState;
    private final int luck;
    private final int lureSpeed;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/FishingHook$FishHookState.class */
    enum FishHookState {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/projectile/FishingHook$OpenWaterType.class */
    enum OpenWaterType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID
    }

    private FishingHook(Level level, Player player, int i, int i2) {
        super(EntityType.FISHING_BOBBER, level);
        this.syncronizedRandom = new Random();
        this.openWater = true;
        this.currentState = FishHookState.FLYING;
        this.noCulling = true;
        setOwner(player);
        player.fishing = this;
        this.luck = Math.max(0, i);
        this.lureSpeed = Math.max(0, i2);
    }

    public FishingHook(Level level, Player player, double d, double d2, double d3) {
        this(level, player, 0, 0);
        setPos(d, d2, d3);
        this.xo = getX();
        this.yo = getY();
        this.zo = getZ();
    }

    public FishingHook(Player player, Level level, int i, int i2) {
        this(level, player, i, i2);
        float f = player.xRot;
        float f2 = player.yRot;
        float cos = Mth.cos(((-f2) * 0.017453292f) - 3.1415927f);
        float sin = Mth.sin(((-f2) * 0.017453292f) - 3.1415927f);
        float f3 = -Mth.cos((-f) * 0.017453292f);
        float sin2 = Mth.sin((-f) * 0.017453292f);
        moveTo(player.getX() - (sin * 0.3d), player.getEyeY(), player.getZ() - (cos * 0.3d), f2, f);
        Vec3 vec3 = new Vec3(-sin, Mth.clamp(-(sin2 / f3), -5.0f, 5.0f), -cos);
        double length = vec3.length();
        Vec3 multiply = vec3.multiply((0.6d / length) + 0.5d + (this.random.nextGaussian() * 0.0045d), (0.6d / length) + 0.5d + (this.random.nextGaussian() * 0.0045d), (0.6d / length) + 0.5d + (this.random.nextGaussian() * 0.0045d));
        setDeltaMovement(multiply);
        this.yRot = (float) (Mth.atan2(multiply.x, multiply.z) * 57.2957763671875d);
        this.xRot = (float) (Mth.atan2(multiply.y, Mth.sqrt(getHorizontalDistanceSqr(multiply))) * 57.2957763671875d);
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        getEntityData().define(DATA_HOOKED_ENTITY, 0);
        getEntityData().define(DATA_BITING, false);
    }

    @Override // net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_HOOKED_ENTITY.equals(entityDataAccessor)) {
            int intValue = ((Integer) getEntityData().get(DATA_HOOKED_ENTITY)).intValue();
            this.hookedIn = intValue > 0 ? this.level.getEntity(intValue - 1) : null;
        }
        if (DATA_BITING.equals(entityDataAccessor)) {
            this.biting = ((Boolean) getEntityData().get(DATA_BITING)).booleanValue();
            if (this.biting) {
                setDeltaMovement(getDeltaMovement().x, (-0.4f) * Mth.nextFloat(this.syncronizedRandom, 0.6f, 1.0f), getDeltaMovement().z);
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 4096.0d;
    }

    @Override // net.minecraft.world.entity.Entity
    public void lerpTo(double d, double d2, double d3, float f, float f2, int i, boolean z) {
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void tick() {
        this.syncronizedRandom.setSeed(getUUID().getLeastSignificantBits() ^ this.level.getGameTime());
        super.tick();
        Player playerOwner = getPlayerOwner();
        if (playerOwner == null) {
            remove();
            return;
        }
        if (!this.level.isClientSide && shouldStopFishing(playerOwner)) {
            return;
        }
        if (this.onGround) {
            this.life++;
            if (this.life >= 1200) {
                remove();
                return;
            }
        } else {
            this.life = 0;
        }
        float f = 0.0f;
        BlockPos blockPosition = blockPosition();
        FluidState fluidState = this.level.getFluidState(blockPosition);
        if (fluidState.is(FluidTags.WATER)) {
            f = fluidState.getHeight(this.level, blockPosition);
        }
        boolean z = f > 0.0f;
        if (this.currentState == FishHookState.FLYING) {
            if (this.hookedIn != null) {
                setDeltaMovement(Vec3.ZERO);
                this.currentState = FishHookState.HOOKED_IN_ENTITY;
                return;
            } else {
                if (z) {
                    setDeltaMovement(getDeltaMovement().multiply(0.3d, 0.2d, 0.3d));
                    this.currentState = FishHookState.BOBBING;
                    return;
                }
                checkCollision();
            }
        } else {
            if (this.currentState == FishHookState.HOOKED_IN_ENTITY) {
                if (this.hookedIn != null) {
                    if (this.hookedIn.removed) {
                        this.hookedIn = null;
                        this.currentState = FishHookState.FLYING;
                        return;
                    } else {
                        setPos(this.hookedIn.getX(), this.hookedIn.getY(0.8d), this.hookedIn.getZ());
                        return;
                    }
                }
                return;
            }
            if (this.currentState == FishHookState.BOBBING) {
                Vec3 deltaMovement = getDeltaMovement();
                double y = ((getY() + deltaMovement.y) - blockPosition.getY()) - f;
                if (Math.abs(y) < 0.01d) {
                    y += Math.signum(y) * 0.1d;
                }
                setDeltaMovement(deltaMovement.x * 0.9d, deltaMovement.y - ((y * this.random.nextFloat()) * 0.2d), deltaMovement.z * 0.9d);
                if (this.nibble > 0 || this.timeUntilHooked > 0) {
                    this.openWater = this.openWater && this.outOfWaterTime < 10 && calculateOpenWater(blockPosition);
                } else {
                    this.openWater = true;
                }
                if (z) {
                    this.outOfWaterTime = Math.max(0, this.outOfWaterTime - 1);
                    if (this.biting) {
                        setDeltaMovement(getDeltaMovement().add(0.0d, (-0.1d) * this.syncronizedRandom.nextFloat() * this.syncronizedRandom.nextFloat(), 0.0d));
                    }
                    if (!this.level.isClientSide) {
                        catchingFish(blockPosition);
                    }
                } else {
                    this.outOfWaterTime = Math.min(10, this.outOfWaterTime + 1);
                }
            }
        }
        if (!fluidState.is(FluidTags.WATER)) {
            setDeltaMovement(getDeltaMovement().add(0.0d, -0.03d, 0.0d));
        }
        move(MoverType.SELF, getDeltaMovement());
        updateRotation();
        if (this.currentState == FishHookState.FLYING && (this.onGround || this.horizontalCollision)) {
            setDeltaMovement(Vec3.ZERO);
        }
        setDeltaMovement(getDeltaMovement().scale(0.92d));
        reapplyPosition();
    }

    private boolean shouldStopFishing(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offhandItem = player.getOffhandItem();
        boolean z = mainHandItem.getItem() == Items.FISHING_ROD;
        boolean z2 = offhandItem.getItem() == Items.FISHING_ROD;
        if (player.removed || !player.isAlive() || ((!z && !z2) || distanceToSqr(player) > 1024.0d)) {
            remove();
            return true;
        }
        return false;
    }

    private void checkCollision() {
        onHit(ProjectileUtil.getHitResult(this, this::canHitEntity));
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) || (entity.isAlive() && (entity instanceof ItemEntity));
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (!this.level.isClientSide) {
            this.hookedIn = entityHitResult.getEntity();
            setHookedEntity();
        }
    }

    @Override // net.minecraft.world.entity.projectile.Projectile
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        setDeltaMovement(getDeltaMovement().normalize().scale(blockHitResult.distanceTo(this)));
    }

    private void setHookedEntity() {
        getEntityData().set(DATA_HOOKED_ENTITY, Integer.valueOf(this.hookedIn.getId() + 1));
    }

    private void catchingFish(BlockPos blockPos) {
        ServerLevel serverLevel = (ServerLevel) this.level;
        int i = 1;
        BlockPos above = blockPos.above();
        if (this.random.nextFloat() < 0.25f && this.level.isRainingAt(above)) {
            i = 1 + 1;
        }
        if (this.random.nextFloat() < 0.5f && !this.level.canSeeSky(above)) {
            i--;
        }
        if (this.nibble > 0) {
            this.nibble--;
            if (this.nibble <= 0) {
                this.timeUntilLured = 0;
                this.timeUntilHooked = 0;
                getEntityData().set(DATA_BITING, false);
                return;
            }
            return;
        }
        if (this.timeUntilHooked > 0) {
            this.timeUntilHooked -= i;
            if (this.timeUntilHooked > 0) {
                this.fishAngle = (float) (this.fishAngle + (this.random.nextGaussian() * 4.0d));
                float f = this.fishAngle * 0.017453292f;
                float sin = Mth.sin(f);
                float cos = Mth.cos(f);
                double x = getX() + (sin * this.timeUntilHooked * 0.1f);
                double floor = Mth.floor(getY()) + 1.0f;
                double z = getZ() + (cos * this.timeUntilHooked * 0.1f);
                if (serverLevel.getBlockState(new BlockPos(x, floor - 1.0d, z)).is(Blocks.WATER)) {
                    if (this.random.nextFloat() < 0.15f) {
                        serverLevel.sendParticles(ParticleTypes.BUBBLE, x, floor - 0.10000000149011612d, z, 1, sin, 0.1d, cos, 0.0d);
                    }
                    float f2 = sin * 0.04f;
                    serverLevel.sendParticles(ParticleTypes.FISHING, x, floor, z, 0, cos * 0.04f, 0.01d, -f2, 1.0d);
                    serverLevel.sendParticles(ParticleTypes.FISHING, x, floor, z, 0, -cos, 0.01d, f2, 1.0d);
                    return;
                }
                return;
            }
            playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25f, 1.0f + ((this.random.nextFloat() - this.random.nextFloat()) * 0.4f));
            double y = getY() + 0.5d;
            serverLevel.sendParticles(ParticleTypes.BUBBLE, getX(), y, getZ(), (int) (1.0f + (getBbWidth() * 20.0f)), getBbWidth(), 0.0d, getBbWidth(), 0.20000000298023224d);
            serverLevel.sendParticles(ParticleTypes.FISHING, getX(), y, getZ(), (int) (1.0f + (getBbWidth() * 20.0f)), getBbWidth(), 0.0d, getBbWidth(), 0.20000000298023224d);
            this.nibble = Mth.nextInt(this.random, 20, 40);
            getEntityData().set(DATA_BITING, true);
            return;
        }
        if (this.timeUntilLured > 0) {
            this.timeUntilLured -= i;
            float f3 = 0.15f;
            if (this.timeUntilLured < 20) {
                f3 = (float) (0.15f + ((20 - this.timeUntilLured) * 0.05d));
            } else if (this.timeUntilLured < 40) {
                f3 = (float) (0.15f + ((40 - this.timeUntilLured) * 0.02d));
            } else if (this.timeUntilLured < 60) {
                f3 = (float) (0.15f + ((60 - this.timeUntilLured) * 0.01d));
            }
            if (this.random.nextFloat() < f3) {
                float nextFloat = Mth.nextFloat(this.random, 0.0f, 360.0f) * 0.017453292f;
                float nextFloat2 = Mth.nextFloat(this.random, 25.0f, 60.0f);
                double x2 = getX() + (Mth.sin(nextFloat) * nextFloat2 * 0.1f);
                double floor2 = Mth.floor(getY()) + 1.0f;
                double z2 = getZ() + (Mth.cos(nextFloat) * nextFloat2 * 0.1f);
                if (serverLevel.getBlockState(new BlockPos(x2, floor2 - 1.0d, z2)).is(Blocks.WATER)) {
                    serverLevel.sendParticles(ParticleTypes.SPLASH, x2, floor2, z2, 2 + this.random.nextInt(2), 0.10000000149011612d, 0.0d, 0.10000000149011612d, 0.0d);
                }
            }
            if (this.timeUntilLured <= 0) {
                this.fishAngle = Mth.nextFloat(this.random, 0.0f, 360.0f);
                this.timeUntilHooked = Mth.nextInt(this.random, 20, 80);
                return;
            }
            return;
        }
        this.timeUntilLured = Mth.nextInt(this.random, 100, 600);
        this.timeUntilLured -= (this.lureSpeed * 20) * 5;
    }

    private boolean calculateOpenWater(BlockPos blockPos) {
        OpenWaterType openWaterType = OpenWaterType.INVALID;
        for (int i = -1; i <= 2; i++) {
            OpenWaterType openWaterTypeForArea = getOpenWaterTypeForArea(blockPos.offset(-2, i, -2), blockPos.offset(2, i, 2));
            switch (openWaterTypeForArea) {
                case INVALID:
                    return false;
                case ABOVE_WATER:
                    if (openWaterType == OpenWaterType.INVALID) {
                        return false;
                    }
                    break;
                case INSIDE_WATER:
                    if (openWaterType == OpenWaterType.ABOVE_WATER) {
                        return false;
                    }
                    break;
            }
            openWaterType = openWaterTypeForArea;
        }
        return true;
    }

    private OpenWaterType getOpenWaterTypeForArea(BlockPos blockPos, BlockPos blockPos2) {
        return (OpenWaterType) BlockPos.betweenClosedStream(blockPos, blockPos2).map(this::getOpenWaterTypeForBlock).reduce((openWaterType, openWaterType2) -> {
            return openWaterType == openWaterType2 ? openWaterType : OpenWaterType.INVALID;
        }).orElse(OpenWaterType.INVALID);
    }

    private OpenWaterType getOpenWaterTypeForBlock(BlockPos blockPos) {
        BlockState blockState = this.level.getBlockState(blockPos);
        if (blockState.isAir() || blockState.is(Blocks.LILY_PAD)) {
            return OpenWaterType.ABOVE_WATER;
        }
        FluidState fluidState = blockState.getFluidState();
        if (fluidState.is(FluidTags.WATER) && fluidState.isSource() && blockState.getCollisionShape(this.level, blockPos).isEmpty()) {
            return OpenWaterType.INSIDE_WATER;
        }
        return OpenWaterType.INVALID;
    }

    public boolean isOpenWaterFishing() {
        return this.openWater;
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override // net.minecraft.world.entity.projectile.Projectile, net.minecraft.world.entity.Entity
    public void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    public int retrieve(ItemStack itemStack) {
        Player playerOwner = getPlayerOwner();
        if (this.level.isClientSide || playerOwner == null) {
            return 0;
        }
        int i = 0;
        if (this.hookedIn != null) {
            bringInHookedEntity();
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer) playerOwner, itemStack, this, Collections.emptyList());
            this.level.broadcastEntityEvent(this, (byte) 31);
            i = this.hookedIn instanceof ItemEntity ? 3 : 5;
        } else if (this.nibble > 0) {
            List<ItemStack> randomItems = this.level.getServer().getLootTables().get(BuiltInLootTables.FISHING).getRandomItems(new LootContext.Builder((ServerLevel) this.level).withParameter(LootContextParams.ORIGIN, position()).withParameter(LootContextParams.TOOL, itemStack).withParameter(LootContextParams.THIS_ENTITY, this).withRandom(this.random).withLuck(this.luck + playerOwner.getLuck()).create(LootContextParamSets.FISHING));
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer) playerOwner, itemStack, this, randomItems);
            for (ItemStack itemStack2 : randomItems) {
                ItemEntity itemEntity = new ItemEntity(this.level, getX(), getY(), getZ(), itemStack2);
                double x = playerOwner.getX() - getX();
                double y = playerOwner.getY() - getY();
                double z = playerOwner.getZ() - getZ();
                itemEntity.setDeltaMovement(x * 0.1d, (y * 0.1d) + (Math.sqrt(Math.sqrt((x * x) + (y * y) + (z * z))) * 0.08d), z * 0.1d);
                this.level.addFreshEntity(itemEntity);
                playerOwner.level.addFreshEntity(new ExperienceOrb(playerOwner.level, playerOwner.getX(), playerOwner.getY() + 0.5d, playerOwner.getZ() + 0.5d, this.random.nextInt(6) + 1));
                if (itemStack2.getItem().is(ItemTags.FISHES)) {
                    playerOwner.awardStat(Stats.FISH_CAUGHT, 1);
                }
            }
            i = 1;
        }
        if (this.onGround) {
            i = 2;
        }
        remove();
        return i;
    }

    @Override // net.minecraft.world.entity.Entity
    public void handleEntityEvent(byte b) {
        if (b == 31 && this.level.isClientSide && (this.hookedIn instanceof Player) && ((Player) this.hookedIn).isLocalPlayer()) {
            bringInHookedEntity();
        }
        super.handleEntityEvent(b);
    }

    protected void bringInHookedEntity() {
        Entity owner = getOwner();
        if (owner == null) {
            return;
        }
        this.hookedIn.setDeltaMovement(this.hookedIn.getDeltaMovement().add(new Vec3(owner.getX() - getX(), owner.getY() - getY(), owner.getZ() - getZ()).scale(0.1d)));
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public void remove() {
        super.remove();
        Player playerOwner = getPlayerOwner();
        if (playerOwner != null) {
            playerOwner.fishing = null;
        }
    }

    @Nullable
    public Player getPlayerOwner() {
        Entity owner = getOwner();
        if (owner instanceof Player) {
            return (Player) owner;
        }
        return null;
    }

    @Nullable
    public Entity getHookedIn() {
        return this.hookedIn;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean canChangeDimensions() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        Entity owner = getOwner();
        return new ClientboundAddEntityPacket(this, owner == null ? getId() : owner.getId());
    }
}
