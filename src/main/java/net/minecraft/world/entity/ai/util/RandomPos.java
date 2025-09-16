package net.minecraft.world.entity.ai.util;

import java.util.Random;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/util/RandomPos.class */
public class RandomPos {
    @Nullable
    public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int i2) {
        pathfinderMob.getClass();
        return generateRandomPos(pathfinderMob, i, i2, 0, null, true, 1.5707963705062866d, pathfinderMob::getWalkTargetValue, false, 0, 0, true);
    }

    @Nullable
    public static Vec3 getAirPos(PathfinderMob pathfinderMob, int i, int i2, int i3, @Nullable Vec3 vec3, double d) {
        pathfinderMob.getClass();
        return generateRandomPos(pathfinderMob, i, i2, i3, vec3, true, d, pathfinderMob::getWalkTargetValue, true, 0, 0, false);
    }

    @Nullable
    public static Vec3 getLandPos(PathfinderMob pathfinderMob, int i, int i2) {
        pathfinderMob.getClass();
        return getLandPos(pathfinderMob, i, i2, pathfinderMob::getWalkTargetValue);
    }

    @Nullable
    public static Vec3 getLandPos(PathfinderMob pathfinderMob, int i, int i2, ToDoubleFunction<BlockPos> toDoubleFunction) {
        return generateRandomPos(pathfinderMob, i, i2, 0, null, false, 0.0d, toDoubleFunction, true, 0, 0, true);
    }

    @Nullable
    public static Vec3 getAboveLandPos(PathfinderMob pathfinderMob, int i, int i2, Vec3 vec3, float f, int i3, int i4) {
        pathfinderMob.getClass();
        return generateRandomPos(pathfinderMob, i, i2, 0, vec3, false, f, pathfinderMob::getWalkTargetValue, true, i3, i4, true);
    }

    @Nullable
    public static Vec3 getLandPosTowards(PathfinderMob pathfinderMob, int i, int i2, Vec3 vec3) {
        Vec3 subtract = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
        pathfinderMob.getClass();
        return generateRandomPos(pathfinderMob, i, i2, 0, subtract, false, 1.5707963705062866d, pathfinderMob::getWalkTargetValue, true, 0, 0, true);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int i2, Vec3 vec3) {
        Vec3 subtract = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
        pathfinderMob.getClass();
        return generateRandomPos(pathfinderMob, i, i2, 0, subtract, true, 1.5707963705062866d, pathfinderMob::getWalkTargetValue, false, 0, 0, true);
    }

    @Nullable
    public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int i2, Vec3 vec3, double d) {
        Vec3 subtract = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
        pathfinderMob.getClass();
        return generateRandomPos(pathfinderMob, i, i2, 0, subtract, true, d, pathfinderMob::getWalkTargetValue, false, 0, 0, true);
    }

    @Nullable
    public static Vec3 getAirPosTowards(PathfinderMob pathfinderMob, int i, int i2, int i3, Vec3 vec3, double d) {
        Vec3 subtract = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
        pathfinderMob.getClass();
        return generateRandomPos(pathfinderMob, i, i2, i3, subtract, false, d, pathfinderMob::getWalkTargetValue, true, 0, 0, false);
    }

    @Nullable
    public static Vec3 getPosAvoid(PathfinderMob pathfinderMob, int i, int i2, Vec3 vec3) {
        Vec3 subtract = pathfinderMob.position().subtract(vec3);
        pathfinderMob.getClass();
        return generateRandomPos(pathfinderMob, i, i2, 0, subtract, true, 1.5707963705062866d, pathfinderMob::getWalkTargetValue, false, 0, 0, true);
    }

    @Nullable
    public static Vec3 getLandPosAvoid(PathfinderMob pathfinderMob, int i, int i2, Vec3 vec3) {
        Vec3 subtract = pathfinderMob.position().subtract(vec3);
        pathfinderMob.getClass();
        return generateRandomPos(pathfinderMob, i, i2, 0, subtract, false, 1.5707963705062866d, pathfinderMob::getWalkTargetValue, true, 0, 0, true);
    }

    @Nullable
    private static Vec3 generateRandomPos(PathfinderMob pathfinderMob, int i, int i2, int i3, @Nullable Vec3 vec3, boolean z, double d, ToDoubleFunction<BlockPos> toDoubleFunction, boolean z2, int i4, int i5, boolean z3) {
        boolean z4;
        PathNavigation navigation = pathfinderMob.getNavigation();
        Random random = pathfinderMob.getRandom();
        if (pathfinderMob.hasRestriction()) {
            z4 = pathfinderMob.getRestrictCenter().closerThan(pathfinderMob.position(), pathfinderMob.getRestrictRadius() + i + 1.0d);
        } else {
            z4 = false;
        }
        boolean z5 = false;
        double d2 = Double.NEGATIVE_INFINITY;
        BlockPos blockPosition = pathfinderMob.blockPosition();
        for (int i6 = 0; i6 < 10; i6++) {
            BlockPos randomDelta = getRandomDelta(random, i, i2, i3, vec3, d);
            if (randomDelta != null) {
                int x = randomDelta.getX();
                int y = randomDelta.getY();
                int z6 = randomDelta.getZ();
                if (pathfinderMob.hasRestriction() && i > 1) {
                    BlockPos restrictCenter = pathfinderMob.getRestrictCenter();
                    if (pathfinderMob.getX() > restrictCenter.getX()) {
                        x -= random.nextInt(i / 2);
                    } else {
                        x += random.nextInt(i / 2);
                    }
                    z6 = pathfinderMob.getZ() > ((double) restrictCenter.getZ()) ? z6 - random.nextInt(i / 2) : z6 + random.nextInt(i / 2);
                }
                BlockPos blockPos = new BlockPos(x + pathfinderMob.getX(), y + pathfinderMob.getY(), z6 + pathfinderMob.getZ());
                if (blockPos.getY() >= 0 && blockPos.getY() <= pathfinderMob.level.getMaxBuildHeight() && ((!z4 || pathfinderMob.isWithinRestriction(blockPos)) && (!z3 || navigation.isStableDestination(blockPos)))) {
                    if (z2) {
                        blockPos = moveUpToAboveSolid(blockPos, random.nextInt(i4 + 1) + i5, pathfinderMob.level.getMaxBuildHeight(), blockPos2 -> {
                            return pathfinderMob.level.getBlockState(blockPos2).getMaterial().isSolid();
                        });
                    }
                    if ((z || !pathfinderMob.level.getFluidState(blockPos).is(FluidTags.WATER)) && pathfinderMob.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(pathfinderMob.level, blockPos.mutable())) == 0.0f) {
                        double applyAsDouble = toDoubleFunction.applyAsDouble(blockPos);
                        if (applyAsDouble > d2) {
                            d2 = applyAsDouble;
                            blockPosition = blockPos;
                            z5 = true;
                        }
                    }
                }
            }
        }
        if (z5) {
            return Vec3.atBottomCenterOf(blockPosition);
        }
        return null;
    }

    @Nullable
    private static BlockPos getRandomDelta(Random random, int i, int i2, int i3, @Nullable Vec3 vec3, double d) {
        if (vec3 == null || d >= 3.141592653589793d) {
            return new BlockPos(random.nextInt((2 * i) + 1) - i, (random.nextInt((2 * i2) + 1) - i2) + i3, random.nextInt((2 * i) + 1) - i);
        }
        double atan2 = (Mth.atan2(vec3.z, vec3.x) - 1.5707963705062866d) + (((2.0f * random.nextFloat()) - 1.0f) * d);
        double sqrt = Math.sqrt(random.nextDouble()) * Mth.SQRT_OF_TWO * i;
        double sin = (-sqrt) * Math.sin(atan2);
        double cos = sqrt * Math.cos(atan2);
        if (Math.abs(sin) > i || Math.abs(cos) > i) {
            return null;
        }
        return new BlockPos(sin, (random.nextInt((2 * i2) + 1) - i2) + i3, cos);
    }

    static BlockPos moveUpToAboveSolid(BlockPos blockPos, int i, int i2, Predicate<BlockPos> predicate) {
        BlockPos blockPos2;
        BlockPos blockPos3;
        if (i < 0) {
            throw new IllegalArgumentException("aboveSolidAmount was " + i + ", expected >= 0");
        }
        if (predicate.test(blockPos)) {
            BlockPos above = blockPos.above();
            while (true) {
                blockPos2 = above;
                if (blockPos2.getY() >= i2 || !predicate.test(blockPos2)) {
                    break;
                }
                above = blockPos2.above();
            }
            BlockPos blockPos4 = blockPos2;
            while (true) {
                blockPos3 = blockPos4;
                if (blockPos3.getY() >= i2 || blockPos3.getY() - blockPos2.getY() >= i) {
                    break;
                }
                BlockPos above2 = blockPos3.above();
                if (predicate.test(above2)) {
                    break;
                }
                blockPos4 = above2;
            }
            return blockPos3;
        }
        return blockPos;
    }
}
