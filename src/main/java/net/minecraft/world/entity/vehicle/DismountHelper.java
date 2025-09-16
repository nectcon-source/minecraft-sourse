package net.minecraft.world.entity.vehicle;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/DismountHelper.class */
public class DismountHelper {
    /* JADX WARN: Type inference failed for: r0v7, types: [int[], int[][]] */
    public static int[][] offsetsForDirection(Direction direction) {
        Direction var1 = direction.getClockWise();
        Direction var2 = var1.getOpposite();
        Direction var3 = direction.getOpposite();
        return new int[][]{{var1.getStepX(), var1.getStepZ()}, {var2.getStepX(), var2.getStepZ()}, {var3.getStepX() + var1.getStepX(), var3.getStepZ() + var1.getStepZ()}, {var3.getStepX() + var2.getStepX(), var3.getStepZ() + var2.getStepZ()}, {direction.getStepX() + var1.getStepX(), direction.getStepZ() + var1.getStepZ()}, {direction.getStepX() + var2.getStepX(), direction.getStepZ() + var2.getStepZ()}, {var3.getStepX(), var3.getStepZ()}, {direction.getStepX(), direction.getStepZ()}};
    }

    public static boolean isBlockFloorValid(double d) {
        return !Double.isInfinite(d) && d < 1.0d;
    }

    public static boolean canDismountTo(CollisionGetter collisionGetter, LivingEntity livingEntity, AABB aabb) {
        return collisionGetter.getBlockCollisions(livingEntity, aabb).allMatch((v0) -> {
            return v0.isEmpty();
        });
    }

    @Nullable
    public static Vec3 findDismountLocation(CollisionGetter collisionGetter, double d, double d2, double d3, LivingEntity livingEntity, Pose pose) {
        if (isBlockFloorValid(d2)) {
            Vec3 vec3 = new Vec3(d, d2, d3);
            if (canDismountTo(collisionGetter, livingEntity, livingEntity.getLocalBoundsForPose(pose).move(vec3))) {
                return vec3;
            }
            return null;
        }
        return null;
    }

    public static VoxelShape nonClimbableShape(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        if (blockState.is(BlockTags.CLIMBABLE) || ((blockState.getBlock() instanceof TrapDoorBlock) && ((Boolean) blockState.getValue(TrapDoorBlock.OPEN)).booleanValue())) {
            return Shapes.empty();
        }
        return blockState.getCollisionShape(blockGetter, blockPos);
    }

    public static double findCeilingFrom(BlockPos blockPos, int i, Function<BlockPos, VoxelShape> function) {
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        int i2 = 0;
        while (i2 < i) {
            VoxelShape apply = function.apply(mutable);
            if (!apply.isEmpty()) {
                return blockPos.getY() + i2 + apply.min(Direction.Axis.Y);
            }
            i2++;
            mutable.move(Direction.UP);
        }
        return Double.POSITIVE_INFINITY;
    }

    @Nullable
    public static Vec3 findSafeDismountLocation(EntityType<?> entityType, CollisionGetter collisionGetter, BlockPos blockPos, boolean z) {
        if (z && entityType.isBlockDangerous(collisionGetter.getBlockState(blockPos))) {
            return null;
        }
        double blockFloorHeight = collisionGetter.getBlockFloorHeight(nonClimbableShape(collisionGetter, blockPos), () -> {
            return nonClimbableShape(collisionGetter, blockPos.below());
        });
        if (!isBlockFloorValid(blockFloorHeight)) {
            return null;
        }
        if (z && blockFloorHeight <= 0.0d && entityType.isBlockDangerous(collisionGetter.getBlockState(blockPos.below()))) {
            return null;
        }
        Vec3 upFromBottomCenterOf = Vec3.upFromBottomCenterOf(blockPos, blockFloorHeight);
        if (collisionGetter.getBlockCollisions(null, entityType.getDimensions().makeBoundingBox(upFromBottomCenterOf)).allMatch((v0) -> {
            return v0.isEmpty();
        })) {
            return upFromBottomCenterOf;
        }
        return null;
    }
}
