package net.minecraft.world.level.block.piston;

import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/piston/PistonMovingBlockEntity.class */
public class PistonMovingBlockEntity extends BlockEntity implements TickableBlockEntity {
    private BlockState movedState;
    private Direction direction;
    private boolean extending;
    private boolean isSourcePiston;
    private static final ThreadLocal<Direction> NOCLIP = ThreadLocal.withInitial(() -> {
        return null;
    });
    private float progress;
    private float progressO;
    private long lastTicked;
    private int deathTicks;

    public PistonMovingBlockEntity() {
        super(BlockEntityType.PISTON);
    }

    public PistonMovingBlockEntity(BlockState blockState, Direction direction, boolean z, boolean z2) {
        this();
        this.movedState = blockState;
        this.direction = direction;
        this.extending = z;
        this.isSourcePiston = z2;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    public boolean isExtending() {
        return this.extending;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public boolean isSourcePiston() {
        return this.isSourcePiston;
    }

    public float getProgress(float f) {
        if (f > 1.0f) {
            f = 1.0f;
        }
        return Mth.lerp(f, this.progressO, this.progress);
    }

    public float getXOff(float f) {
        return this.direction.getStepX() * getExtendedProgress(getProgress(f));
    }

    public float getYOff(float f) {
        return this.direction.getStepY() * getExtendedProgress(getProgress(f));
    }

    public float getZOff(float f) {
        return this.direction.getStepZ() * getExtendedProgress(getProgress(f));
    }

    private float getExtendedProgress(float f) {
        return this.extending ? f - 1.0f : 1.0f - f;
    }

    private BlockState getCollisionRelatedBlockState() {
        if (!isExtending() && isSourcePiston() && (this.movedState.getBlock() instanceof PistonBaseBlock)) {
            return (BlockState) ((BlockState) ((BlockState) Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.SHORT, Boolean.valueOf(this.progress > 0.25f))).setValue(PistonHeadBlock.TYPE, this.movedState.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT)).setValue(PistonHeadBlock.FACING, this.movedState.getValue(PistonBaseBlock.FACING));
        }
        return this.movedState;
    }

    /* JADX WARN: Removed duplicated region for block: B:36:0x0173 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:48:0x006f A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void moveCollidedEntities(float var1) {
        Direction var2 = this.getMovementDirection();
        double var3x = (double)(var1 - this.progress);
        VoxelShape var5xx = this.getCollisionRelatedBlockState().getCollisionShape(this.level, this.getBlockPos());
        if (!var5xx.isEmpty()) {
            AABB var6xxx = this.moveByPositionAndProgress(var5xx.bounds());
            List<Entity> var7xxxx = this.level.getEntities(null, PistonMath.getMovementArea(var6xxx, var2, var3x).minmax(var6xxx));
            if (!var7xxxx.isEmpty()) {
                List<AABB> var8xxxxx = var5xx.toAabbs();
                boolean var9xxxxxx = this.movedState.is(Blocks.SLIME_BLOCK);
                Iterator var10 = var7xxxx.iterator();

                while(true) {
                    Entity var11;
                    while(true) {
                        if (!var10.hasNext()) {
                            return;
                        }

                  var11 = (Entity)var10.next();
                        if (var11.getPistonPushReaction() != PushReaction.IGNORE) {
                            if (!var9xxxxxx) {
                                break;
                            }

                            if (!(var11 instanceof ServerPlayer)) {
                                Vec3 var12xxxxxxx = var11.getDeltaMovement();
                                double var13xxxxxxxx = var12xxxxxxx.x;
                                double var15xxxxxxxxx = var12xxxxxxx.y;
                                double var17xxxxxxxxxx = var12xxxxxxx.z;
                                switch(var2.getAxis()) {
                                    case X:
                                        var13xxxxxxxx = (double)var2.getStepX();
                                        break;
                                    case Y:
                                        var15xxxxxxxxx = (double)var2.getStepY();
                                        break;
                                    case Z:
                                        var17xxxxxxxxxx = (double)var2.getStepZ();
                                }

                        var11.setDeltaMovement(var13xxxxxxxx, var15xxxxxxxxx, var17xxxxxxxxxx);
                                break;
                            }
                        }
                    }

                    double var19xxxxxxx = 0.0;

                    for(AABB var21 : var8xxxxx) {
                        AABB var16xxxxxxxx = PistonMath.getMovementArea(this.moveByPositionAndProgress(var21), var2, var3x);
                        AABB var22xxxxxxxxx = var11.getBoundingBox();
                        if (var16xxxxxxxx.intersects(var22xxxxxxxxx)) {
                     var19xxxxxxx = Math.max(var19xxxxxxx, getMovement(var16xxxxxxxx, var2, var22xxxxxxxxx));
                            if (var19xxxxxxx >= var3x) {
                                break;
                            }
                        }
                    }

                    if (!(var19xxxxxxx <= 0.0)) {
                  var19xxxxxxx = Math.min(var19xxxxxxx, var3x) + 0.01;
                        moveEntityByPiston(var2, var11, var19xxxxxxx, var2);
                        if (!this.extending && this.isSourcePiston) {
                            this.fixEntityWithinPistonBase(var11, var2, var3x);
                        }
                    }
                }
            }
        }
    }

    private static void moveEntityByPiston(Direction direction, Entity entity, double d, Direction direction2) {
        NOCLIP.set(direction);
        entity.move(MoverType.PISTON, new Vec3(d * direction2.getStepX(), d * direction2.getStepY(), d * direction2.getStepZ()));
        NOCLIP.set(null);
    }

    private void moveStuckEntities(float f) {
        if (!isStickyForEntities()) {
            return;
        }
        Direction movementDirection = getMovementDirection();
        if (!movementDirection.getAxis().isHorizontal()) {
            return;
        }
        AABB moveByPositionAndProgress = moveByPositionAndProgress(new AABB(0.0d, this.movedState.getCollisionShape(this.level, this.worldPosition).max(Direction.Axis.Y), 0.0d, 1.0d, 1.5000000999999998d, 1.0d));
        double d = f - this.progress;
        Iterator<Entity> it = this.level.getEntities((Entity) null, moveByPositionAndProgress, entity -> {
            return matchesStickyCritera(moveByPositionAndProgress, entity);
        }).iterator();
        while (it.hasNext()) {
            moveEntityByPiston(movementDirection, it.next(), d, movementDirection);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean matchesStickyCritera(AABB aabb, Entity entity) {
        return entity.getPistonPushReaction() == PushReaction.NORMAL && entity.isOnGround() && entity.getX() >= aabb.minX && entity.getX() <= aabb.maxX && entity.getZ() >= aabb.minZ && entity.getZ() <= aabb.maxZ;
    }

    private boolean isStickyForEntities() {
        return this.movedState.is(Blocks.HONEY_BLOCK);
    }

    public Direction getMovementDirection() {
        return this.extending ? this.direction : this.direction.getOpposite();
    }

    private static double getMovement(AABB aabb, Direction direction, AABB aabb2) {
        switch (direction) {
            case EAST:
                return aabb.maxX - aabb2.minX;
            case WEST:
                return aabb2.maxX - aabb.minX;
            case UP:
            default:
                return aabb.maxY - aabb2.minY;
            case DOWN:
                return aabb2.maxY - aabb.minY;
            case SOUTH:
                return aabb.maxZ - aabb2.minZ;
            case NORTH:
                return aabb2.maxZ - aabb.minZ;
        }
    }

    private AABB moveByPositionAndProgress(AABB aabb) {
        double extendedProgress = getExtendedProgress(this.progress);
        return aabb.move(this.worldPosition.getX() + (extendedProgress * this.direction.getStepX()), this.worldPosition.getY() + (extendedProgress * this.direction.getStepY()), this.worldPosition.getZ() + (extendedProgress * this.direction.getStepZ()));
    }

    private void fixEntityWithinPistonBase(Entity entity, Direction direction, double d) {
        AABB boundingBox = entity.getBoundingBox();
        AABB move = Shapes.block().bounds().move(this.worldPosition);
        if (boundingBox.intersects(move)) {
            Direction opposite = direction.getOpposite();
            double movement = getMovement(move, opposite, boundingBox) + 0.01d;
            if (Math.abs(movement - (getMovement(move, opposite, boundingBox.intersect(move)) + 0.01d)) < 0.01d) {
                moveEntityByPiston(direction, entity, Math.min(movement, d) + 0.01d, opposite);
            }
        }
    }

    public BlockState getMovedState() {
        return this.movedState;
    }

    public void finalTick() {
        BlockState updateFromNeighbourShapes;
        if (this.level != null) {
            if (this.progressO < 1.0f || this.level.isClientSide) {
                this.progress = 1.0f;
                this.progressO = this.progress;
                this.level.removeBlockEntity(this.worldPosition);
                setRemoved();
                if (this.level.getBlockState(this.worldPosition).is(Blocks.MOVING_PISTON)) {
                    if (this.isSourcePiston) {
                        updateFromNeighbourShapes = Blocks.AIR.defaultBlockState();
                    } else {
                        updateFromNeighbourShapes = Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
                    }
                    this.level.setBlock(this.worldPosition, updateFromNeighbourShapes, 3);
                    this.level.neighborChanged(this.worldPosition, updateFromNeighbourShapes.getBlock(), this.worldPosition);
                }
            }
        }
    }

    @Override // net.minecraft.world.level.block.entity.TickableBlockEntity
    public void tick() {
        this.lastTicked = this.level.getGameTime();
        this.progressO = this.progress;
        if (this.progressO >= 1.0f) {
            if (this.level.isClientSide && this.deathTicks < 5) {
                this.deathTicks++;
                return;
            }
            this.level.removeBlockEntity(this.worldPosition);
            setRemoved();
            if (this.movedState != null && this.level.getBlockState(this.worldPosition).is(Blocks.MOVING_PISTON)) {
                BlockState updateFromNeighbourShapes = Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
                if (updateFromNeighbourShapes.isAir()) {
                    this.level.setBlock(this.worldPosition, this.movedState, 84);
                    Block.updateOrDestroy(this.movedState, updateFromNeighbourShapes, this.level, this.worldPosition, 3);
                    return;
                }
                if (updateFromNeighbourShapes.hasProperty(BlockStateProperties.WATERLOGGED) && ((Boolean) updateFromNeighbourShapes.getValue(BlockStateProperties.WATERLOGGED)).booleanValue()) {
                    updateFromNeighbourShapes = (BlockState) updateFromNeighbourShapes.setValue(BlockStateProperties.WATERLOGGED, false);
                }
                this.level.setBlock(this.worldPosition, updateFromNeighbourShapes, 67);
                this.level.neighborChanged(this.worldPosition, updateFromNeighbourShapes.getBlock(), this.worldPosition);
                return;
            }
            return;
        }
        float f = this.progress + 0.5f;
        moveCollidedEntities(f);
        moveStuckEntities(f);
        this.progress = f;
        if (this.progress >= 1.0f) {
            this.progress = 1.0f;
        }
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.movedState = NbtUtils.readBlockState(compoundTag.getCompound("blockState"));
        this.direction = Direction.from3DDataValue(compoundTag.getInt("facing"));
        this.progress = compoundTag.getFloat("progress");
        this.progressO = this.progress;
        this.extending = compoundTag.getBoolean("extending");
        this.isSourcePiston = compoundTag.getBoolean("source");
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        compoundTag.put("blockState", NbtUtils.writeBlockState(this.movedState));
        compoundTag.putInt("facing", this.direction.get3DDataValue());
        compoundTag.putFloat("progress", this.progressO);
        compoundTag.putBoolean("extending", this.extending);
        compoundTag.putBoolean("source", this.isSourcePiston);
        return compoundTag;
    }

    public VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos) {
        VoxelShape empty;
        BlockState blockState;
        if (!this.extending && this.isSourcePiston) {
            empty = ((BlockState) this.movedState.setValue(PistonBaseBlock.EXTENDED, true)).getCollisionShape(blockGetter, blockPos);
        } else {
            empty = Shapes.empty();
        }
        Direction direction = NOCLIP.get();
        if (this.progress < 1.0d && direction == getMovementDirection()) {
            return empty;
        }
        if (isSourcePiston()) {
            blockState = (BlockState) ((BlockState) Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, this.direction)).setValue(PistonHeadBlock.SHORT, Boolean.valueOf(this.extending != (((1.0f - this.progress) > 0.25f ? 1 : ((1.0f - this.progress) == 0.25f ? 0 : -1)) < 0)));
        } else {
            blockState = this.movedState;
        }
        float extendedProgress = getExtendedProgress(this.progress);
        return Shapes.or(empty, blockState.getCollisionShape(blockGetter, blockPos).move(this.direction.getStepX() * extendedProgress, this.direction.getStepY() * extendedProgress, this.direction.getStepZ() * extendedProgress));
    }

    public long getLastTicked() {
        return this.lastTicked;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public double getViewDistance() {
        return 68.0d;
    }
}
