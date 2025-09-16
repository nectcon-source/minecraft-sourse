package net.minecraft.world.level.portal;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/portal/PortalShape.class */
public class PortalShape {
    private static final BlockBehaviour.StatePredicate FRAME = (blockState, blockGetter, blockPos) -> {
        return blockState.is(Blocks.OBSIDIAN);
    };
    private final LevelAccessor level;
    private final Direction.Axis axis;
    private final Direction rightDir;
    private int numPortalBlocks;

    @Nullable
    private BlockPos bottomLeft;
    private int height;
    private int width;

    public static Optional<PortalShape> findEmptyPortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Direction.Axis axis) {
        return findPortalShape(levelAccessor, blockPos, portalShape -> {
            return portalShape.isValid() && portalShape.numPortalBlocks == 0;
        }, axis);
    }

    public static Optional<PortalShape> findPortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Predicate<PortalShape> predicate, Direction.Axis axis) {
        Optional<PortalShape> filter = Optional.of(new PortalShape(levelAccessor, blockPos, axis)).filter(predicate);
        if (filter.isPresent()) {
            return filter;
        }
        return Optional.of(new PortalShape(levelAccessor, blockPos, axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X)).filter(predicate);
    }

    public PortalShape(LevelAccessor levelAccessor, BlockPos blockPos, Direction.Axis axis) {
        this.level = levelAccessor;
        this.axis = axis;
        this.rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = calculateBottomLeft(blockPos);
        if (this.bottomLeft == null) {
            this.bottomLeft = blockPos;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = calculateWidth();
            if (this.width > 0) {
                this.height = calculateHeight();
            }
        }
    }

    @Nullable
    private BlockPos calculateBottomLeft(BlockPos blockPos) {
        int max = Math.max(0, blockPos.getY() - 21);
        while (blockPos.getY() > max && isEmpty(this.level.getBlockState(blockPos.below()))) {
            blockPos = blockPos.below();
        }
        Direction opposite = this.rightDir.getOpposite();
        int distanceUntilEdgeAboveFrame = getDistanceUntilEdgeAboveFrame(blockPos, opposite) - 1;
        if (distanceUntilEdgeAboveFrame < 0) {
            return null;
        }
        return blockPos.relative(opposite, distanceUntilEdgeAboveFrame);
    }

    private int calculateWidth() {
        int distanceUntilEdgeAboveFrame = getDistanceUntilEdgeAboveFrame(this.bottomLeft, this.rightDir);
        if (distanceUntilEdgeAboveFrame < 2 || distanceUntilEdgeAboveFrame > 21) {
            return 0;
        }
        return distanceUntilEdgeAboveFrame;
    }

    private int getDistanceUntilEdgeAboveFrame(BlockPos blockPos, Direction direction) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= 21; i++) {
            mutableBlockPos.set(blockPos).move(direction, i);
            BlockState blockState = this.level.getBlockState(mutableBlockPos);
            if (!isEmpty(blockState)) {
                if (FRAME.test(blockState, this.level, mutableBlockPos)) {
                    return i;
                }
                return 0;
            }
            if (!FRAME.test(this.level.getBlockState(mutableBlockPos.move(Direction.DOWN)), this.level, mutableBlockPos)) {
                return 0;
            }
        }
        return 0;
    }

    private int calculateHeight() {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int distanceUntilTop = getDistanceUntilTop(mutableBlockPos);
        if (distanceUntilTop < 3 || distanceUntilTop > 21 || !hasTopFrame(mutableBlockPos, distanceUntilTop)) {
            return 0;
        }
        return distanceUntilTop;
    }

    private boolean hasTopFrame(BlockPos.MutableBlockPos mutableBlockPos, int i) {
        for (int i2 = 0; i2 < this.width; i2++) {
            BlockPos.MutableBlockPos move = mutableBlockPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, i2);
            if (!FRAME.test(this.level.getBlockState(move), this.level, move)) {
                return false;
            }
        }
        return true;
    }

    private int getDistanceUntilTop(BlockPos.MutableBlockPos mutableBlockPos) {
        for (int i = 0; i < 21; i++) {
            mutableBlockPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, -1);
            if (!FRAME.test(this.level.getBlockState(mutableBlockPos), this.level, mutableBlockPos)) {
                return i;
            }
            mutableBlockPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, this.width);
            if (!FRAME.test(this.level.getBlockState(mutableBlockPos), this.level, mutableBlockPos)) {
                return i;
            }
            for (int i2 = 0; i2 < this.width; i2++) {
                mutableBlockPos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, i2);
                BlockState blockState = this.level.getBlockState(mutableBlockPos);
                if (!isEmpty(blockState)) {
                    return i;
                }
                if (blockState.is(Blocks.NETHER_PORTAL)) {
                    this.numPortalBlocks++;
                }
            }
        }
        return 21;
    }

    private static boolean isEmpty(BlockState blockState) {
        return blockState.isAir() || blockState.is(BlockTags.FIRE) || blockState.is(Blocks.NETHER_PORTAL);
    }

    public boolean isValid() {
        return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
    }

    public void createPortalBlocks() {
        BlockState blockState = (BlockState) Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis);
        BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1)).forEach(blockPos -> {
            this.level.setBlock(blockPos, blockState, 18);
        });
    }

    public boolean isComplete() {
        return isValid() && this.numPortalBlocks == this.width * this.height;
    }

    public static Vec3 getRelativePosition(BlockUtil.FoundRectangle foundRectangle, Direction.Axis axis, Vec3 vec3, EntityDimensions entityDimensions) {
        double var4 = (double)foundRectangle.axis1Size - (double)entityDimensions.width;
        double var6 = (double)foundRectangle.axis2Size - (double)entityDimensions.height;
        BlockPos var8 = foundRectangle.minCorner;
        double var9;
        if (var4 > (double)0.0F) {
            float var11 = (float)var8.get(axis) + entityDimensions.width / 2.0F;
            var9 = Mth.clamp(Mth.inverseLerp(vec3.get(axis) - (double)var11, (double)0.0F, var4), (double)0.0F, (double)1.0F);
        } else {
            var9 = (double)0.5F;
        }

        double var16;
        if (var6 > (double)0.0F) {
            Direction.Axis var13 = Direction.Axis.Y;
            var16 = Mth.clamp(Mth.inverseLerp(vec3.get(var13) - (double)var8.get(var13), (double)0.0F, var6), (double)0.0F, (double)1.0F);
        } else {
            var16 = (double)0.0F;
        }

        Direction.Axis var17 = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        double var14 = vec3.get(var17) - ((double)var8.get(var17) + (double)0.5F);
        return new Vec3(var9, var16, var14);
    }

    public static PortalInfo createPortalInfo(ServerLevel serverLevel, BlockUtil.FoundRectangle foundRectangle, Direction.Axis axis, Vec3 vec3, EntityDimensions entityDimensions, Vec3 vec32, float f, float f2) {
        BlockPos var8 = foundRectangle.minCorner;
        BlockState var9 = serverLevel.getBlockState(var8);
        Direction.Axis var10 = (Direction.Axis)var9.getValue(BlockStateProperties.HORIZONTAL_AXIS);
        double var11 = (double)foundRectangle.axis1Size;
        double var13 = (double)foundRectangle.axis2Size;
        int var15 = axis == var10 ? 0 : 90;
        Vec3 var16 = axis == var10 ? vec32 : new Vec3(vec32.z, vec32.y, -vec32.x);
        double var17 = (double)entityDimensions.width / (double)2.0F + (var11 - (double)entityDimensions.width) * vec3.x();
        double var19 = (var13 - (double)entityDimensions.height) * vec3.y();
        double var21 = (double)0.5F + vec3.z();
        boolean var23 = var10 == Direction.Axis.X;
        Vec3 var24 = new Vec3((double)var8.getX() + (var23 ? var17 : var21), (double)var8.getY() + var19, (double)var8.getZ() + (var23 ? var21 : var17));
        return new PortalInfo(var24, var16, f + (float)var15, f2);
    }
}
