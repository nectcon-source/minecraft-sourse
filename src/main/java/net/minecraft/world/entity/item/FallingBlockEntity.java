package net.minecraft.world.entity.item;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/item/FallingBlockEntity.class */
public class FallingBlockEntity extends Entity {
    private BlockState blockState;
    public int time;
    public boolean dropItem;
    private boolean cancelDrop;
    private boolean hurtEntities;
    private int fallDamageMax;
    private float fallDamageAmount;
    public CompoundTag blockData;
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    public FallingBlockEntity(EntityType<? extends FallingBlockEntity> entityType, Level level) {
        super(entityType, level);
        this.blockState = Blocks.SAND.defaultBlockState();
        this.dropItem = true;
        this.fallDamageMax = 40;
        this.fallDamageAmount = 2.0f;
    }

    public FallingBlockEntity(Level level, double d, double d2, double d3, BlockState blockState) {
        this(EntityType.FALLING_BLOCK, level);
        this.blockState = blockState;
        this.blocksBuilding = true;
        setPos(d, d2 + ((1.0f - getBbHeight()) / 2.0f), d3);
        setDeltaMovement(Vec3.ZERO);
        this.xo = d;
        this.yo = d2;
        this.zo = d3;
        setStartPos(blockPosition());
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isAttackable() {
        return false;
    }

    public void setStartPos(BlockPos blockPos) {
        this.entityData.set(DATA_START_POS, blockPos);
    }

    public BlockPos getStartPos() {
        return (BlockPos) this.entityData.get(DATA_START_POS);
    }

    @Override // net.minecraft.world.entity.Entity
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        this.entityData.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPickable() {
        return !this.removed;
    }

    @Override // net.minecraft.world.entity.Entity
    public void tick() {
        BlockEntity blockEntity;
        if (this.blockState.isAir()) {
            remove();
            return;
        }
        Block block = this.blockState.getBlock();
        int i = this.time;
        this.time = i + 1;
        if (i == 0) {
            BlockPos blockPosition = blockPosition();
            if (this.level.getBlockState(blockPosition).is(block)) {
                this.level.removeBlock(blockPosition, false);
            } else if (!this.level.isClientSide) {
                remove();
                return;
            }
        }
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0d, -0.04d, 0.0d));
        }
        move(MoverType.SELF, getDeltaMovement());
        if (!this.level.isClientSide) {
            BlockPos blockPosition2 = blockPosition();
            boolean z = this.blockState.getBlock() instanceof ConcretePowderBlock;
            boolean z2 = z && this.level.getFluidState(blockPosition2).is(FluidTags.WATER);
            double lengthSqr = getDeltaMovement().lengthSqr();
            if (z && lengthSqr > 1.0d) {
                BlockHitResult clip = this.level.clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
                if (clip.getType() != HitResult.Type.MISS && this.level.getFluidState(clip.getBlockPos()).is(FluidTags.WATER)) {
                    blockPosition2 = clip.getBlockPos();
                    z2 = true;
                }
            }
            if (this.onGround || z2) {
                BlockState blockState = this.level.getBlockState(blockPosition2);
                setDeltaMovement(getDeltaMovement().multiply(0.7d, -0.5d, 0.7d));
                if (!blockState.is(Blocks.MOVING_PISTON)) {
                    remove();
                    if (!this.cancelDrop) {
                        boolean canBeReplaced = blockState.canBeReplaced(new DirectionalPlaceContext(this.level, blockPosition2, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                        boolean z3 = this.blockState.canSurvive(this.level, blockPosition2) && !(FallingBlock.isFree(this.level.getBlockState(blockPosition2.below())) && (!z || !z2));
                        if (canBeReplaced && z3) {
                            if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level.getFluidState(blockPosition2).getType() == Fluids.WATER) {
                                this.blockState = (BlockState) this.blockState.setValue(BlockStateProperties.WATERLOGGED, true);
                            }
                            if (this.level.setBlock(blockPosition2, this.blockState, 3)) {
                                if (block instanceof FallingBlock) {
                                    ((FallingBlock) block).onLand(this.level, blockPosition2, this.blockState, blockState, this);
                                }
                                if (this.blockData != null && (block instanceof EntityBlock) && (blockEntity = this.level.getBlockEntity(blockPosition2)) != null) {
                                    CompoundTag save = blockEntity.save(new CompoundTag());
                                    for (String str : this.blockData.getAllKeys()) {
                                        Tag tag = this.blockData.get(str);
                                        if (!"x".equals(str) && !"y".equals(str) && !"z".equals(str)) {
                                            save.put(str, tag.copy());
                                        }
                                    }
                                    blockEntity.load(this.blockState, save);
                                    blockEntity.setChanged();
                                }
                            } else if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                spawnAtLocation(block);
                            }
                        } else if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            spawnAtLocation(block);
                        }
                    } else if (block instanceof FallingBlock) {
                        ((FallingBlock) block).onBroken(this.level, blockPosition2, this);
                    }
                }
            } else if (!this.level.isClientSide && ((this.time > 100 && (blockPosition2.getY() < 1 || blockPosition2.getY() > 256)) || this.time > 600)) {
                if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    spawnAtLocation(block);
                }
                remove();
            }
        }
        setDeltaMovement(getDeltaMovement().scale(0.98d));
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean causeFallDamage(float f, float f2) {
        int ceil;
        if (this.hurtEntities && (ceil = Mth.ceil(f - 1.0f)) > 0) {
            List<Entity> newArrayList = Lists.newArrayList(this.level.getEntities(this, getBoundingBox()));
            boolean is = this.blockState.is(BlockTags.ANVIL);
            DamageSource damageSource = is ? DamageSource.ANVIL : DamageSource.FALLING_BLOCK;
            Iterator<Entity> it = newArrayList.iterator();
            while (it.hasNext()) {
                it.next().hurt(damageSource, Math.min(Mth.floor(ceil * this.fallDamageAmount), this.fallDamageMax));
            }
            if (is && this.random.nextFloat() < 0.05000000074505806d + (ceil * 0.05d)) {
                BlockState damage = AnvilBlock.damage(this.blockState);
                if (damage == null) {
                    this.cancelDrop = true;
                    return false;
                }
                this.blockState = damage;
                return false;
            }
            return false;
        }
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.put("BlockState", NbtUtils.writeBlockState(this.blockState));
        compoundTag.putInt("Time", this.time);
        compoundTag.putBoolean("DropItem", this.dropItem);
        compoundTag.putBoolean("HurtEntities", this.hurtEntities);
        compoundTag.putFloat("FallHurtAmount", this.fallDamageAmount);
        compoundTag.putInt("FallHurtMax", this.fallDamageMax);
        if (this.blockData != null) {
            compoundTag.put("TileEntityData", this.blockData);
        }
    }

    @Override // net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.blockState = NbtUtils.readBlockState(compoundTag.getCompound("BlockState"));
        this.time = compoundTag.getInt("Time");
        if (compoundTag.contains("HurtEntities", 99)) {
            this.hurtEntities = compoundTag.getBoolean("HurtEntities");
            this.fallDamageAmount = compoundTag.getFloat("FallHurtAmount");
            this.fallDamageMax = compoundTag.getInt("FallHurtMax");
        } else if (this.blockState.is(BlockTags.ANVIL)) {
            this.hurtEntities = true;
        }
        if (compoundTag.contains("DropItem", 99)) {
            this.dropItem = compoundTag.getBoolean("DropItem");
        }
        if (compoundTag.contains("TileEntityData", 10)) {
            this.blockData = compoundTag.getCompound("TileEntityData");
        }
        if (this.blockState.isAir()) {
            this.blockState = Blocks.SAND.defaultBlockState();
        }
    }

    public Level getLevel() {
        return this.level;
    }

    public void setHurtsEntities(boolean z) {
        this.hurtEntities = z;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean displayFireAnimation() {
        return false;
    }

    @Override // net.minecraft.world.entity.Entity
    public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        super.fillCrashReportCategory(crashReportCategory);
        crashReportCategory.setDetail("Immitating BlockState", this.blockState.toString());
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, Block.getId(getBlockState()));
    }
}
