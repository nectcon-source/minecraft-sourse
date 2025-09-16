package net.minecraft.world.level;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/BlockGetter.class */
public interface BlockGetter {
    @Nullable
    BlockEntity getBlockEntity(BlockPos blockPos);

    BlockState getBlockState(BlockPos blockPos);

    FluidState getFluidState(BlockPos blockPos);

    default int getLightEmission(BlockPos blockPos) {
        return getBlockState(blockPos).getLightEmission();
    }

    default int getMaxLightLevel() {
        return 15;
    }

    default int getMaxBuildHeight() {
        return 256;
    }

    default Stream<BlockState> getBlockStates(AABB aabb) {
        return BlockPos.betweenClosedStream(aabb).map(this::getBlockState);
    }

    default BlockHitResult clip(ClipContext clipContext) {
        return  traverseBlocks(clipContext, (clipContext2, blockPos) -> {
            BlockState blockState = getBlockState(blockPos);
            FluidState fluidState = getFluidState(blockPos);
            Vec3 from = clipContext2.getFrom();
            Vec3 to = clipContext2.getTo();
            BlockHitResult clipWithInteractionOverride = clipWithInteractionOverride(from, to, blockPos, clipContext2.getBlockShape(blockState, this, blockPos), blockState);
            BlockHitResult clip = clipContext2.getFluidShape(fluidState, this, blockPos).clip(from, to, blockPos);
            return (clipWithInteractionOverride == null ? Double.MAX_VALUE : clipContext2.getFrom().distanceToSqr(clipWithInteractionOverride.getLocation())) <= (clip == null ? Double.MAX_VALUE : clipContext2.getFrom().distanceToSqr(clip.getLocation())) ? clipWithInteractionOverride : clip;
        }, clipContext3 -> {
            Vec3 subtract = clipContext3.getFrom().subtract(clipContext3.getTo());
            return BlockHitResult.miss(clipContext3.getTo(), Direction.getNearest(subtract.x, subtract.y, subtract.z), new BlockPos(clipContext3.getTo()));
        });
    }

    @Nullable
    default BlockHitResult clipWithInteractionOverride(Vec3 vec3, Vec3 vec32, BlockPos blockPos, VoxelShape voxelShape, BlockState blockState) {
        BlockHitResult clip;
        BlockHitResult clip2 = voxelShape.clip(vec3, vec32, blockPos);
        if (clip2 != null && (clip = blockState.getInteractionShape(this, blockPos).clip(vec3, vec32, blockPos)) != null && clip.getLocation().subtract(vec3).lengthSqr() < clip2.getLocation().subtract(vec3).lengthSqr()) {
            return clip2.withDirection(clip.getDirection());
        }
        return clip2;
    }

    default double getBlockFloorHeight(VoxelShape voxelShape, Supplier<VoxelShape> supplier) {
        if (!voxelShape.isEmpty()) {
            return voxelShape.max(Direction.Axis.Y);
        }
        double max = supplier.get().max(Direction.Axis.Y);
        if (max >= 1.0d) {
            return max - 1.0d;
        }
        return Double.NEGATIVE_INFINITY;
    }

    default double getBlockFloorHeight(BlockPos blockPos) {
        return getBlockFloorHeight(getBlockState(blockPos).getCollisionShape(this, blockPos), () -> {
            BlockPos below = blockPos.below();
            return getBlockState(below).getCollisionShape(this, below);
        });
    }

    static <T> T traverseBlocks(ClipContext clipContext, BiFunction<ClipContext, BlockPos, T> biFunction, Function<ClipContext, T> function) {
        T apply;
        Vec3 from = clipContext.getFrom();
        Vec3 to = clipContext.getTo();
        if (from.equals(to)) {
            return function.apply(clipContext);
        }
        double lerp = Mth.lerp(-1.0E-7d, to.x, from.x);
        double lerp2 = Mth.lerp(-1.0E-7d, to.y, from.y);
        double lerp3 = Mth.lerp(-1.0E-7d, to.z, from.z);
        double lerp4 = Mth.lerp(-1.0E-7d, from.x, to.x);
        double lerp5 = Mth.lerp(-1.0E-7d, from.y, to.y);
        double lerp6 = Mth.lerp(-1.0E-7d, from.z, to.z);
        int floor = Mth.floor(lerp4);
        int floor2 = Mth.floor(lerp5);
        int floor3 = Mth.floor(lerp6);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(floor, floor2, floor3);
        T apply2 = biFunction.apply(clipContext, mutableBlockPos);
        if (apply2 != null) {
            return apply2;
        }
        double d = lerp - lerp4;
        double d2 = lerp2 - lerp5;
        double d3 = lerp3 - lerp6;
        int sign = Mth.sign(d);
        int sign2 = Mth.sign(d2);
        int sign3 = Mth.sign(d3);
        double d4 = sign == 0 ? Double.MAX_VALUE : sign / d;
        double d5 = sign2 == 0 ? Double.MAX_VALUE : sign2 / d2;
        double d6 = sign3 == 0 ? Double.MAX_VALUE : sign3 / d3;
        double frac = d4 * (sign > 0 ? 1.0d - Mth.frac(lerp4) : Mth.frac(lerp4));
        double frac2 = d5 * (sign2 > 0 ? 1.0d - Mth.frac(lerp5) : Mth.frac(lerp5));
        double frac3 = d6 * (sign3 > 0 ? 1.0d - Mth.frac(lerp6) : Mth.frac(lerp6));
        do {
            if (frac <= 1.0d || frac2 <= 1.0d || frac3 <= 1.0d) {
                if (frac < frac2) {
                    if (frac < frac3) {
                        floor += sign;
                        frac += d4;
                    } else {
                        floor3 += sign3;
                        frac3 += d6;
                    }
                } else if (frac2 < frac3) {
                    floor2 += sign2;
                    frac2 += d5;
                } else {
                    floor3 += sign3;
                    frac3 += d6;
                }
                apply = biFunction.apply(clipContext, mutableBlockPos.set(floor, floor2, floor3));
            } else {
                return function.apply(clipContext);
            }
        } while (apply == null);
        return apply;
    }
}
