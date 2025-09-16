package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/navigation/GroundPathNavigation.class */
public class GroundPathNavigation extends PathNavigation {
    private boolean avoidSun;

    public GroundPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected PathFinder createPathFinder(int i) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, i);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected boolean canUpdatePath() {
        return this.mob.isOnGround() || isInLiquid() || this.mob.isPassenger();
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), getSurfaceY(), this.mob.getZ());
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    public Path createPath(BlockPos blockPos, int i) {
        BlockPos blockPos2;
        BlockPos blockPos3;
        if (this.level.getBlockState(blockPos).isAir()) {
            BlockPos below = blockPos.below();
            while (true) {
                blockPos3 = below;
                if (blockPos3.getY() <= 0 || !this.level.getBlockState(blockPos3).isAir()) {
                    break;
                }
                below = blockPos3.below();
            }
            if (blockPos3.getY() > 0) {
                return super.createPath(blockPos3.above(), i);
            }
            while (blockPos3.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockPos3).isAir()) {
                blockPos3 = blockPos3.above();
            }
            blockPos = blockPos3;
        }
        if (this.level.getBlockState(blockPos).getMaterial().isSolid()) {
            BlockPos above = blockPos.above();
            while (true) {
                blockPos2 = above;
                if (blockPos2.getY() >= this.level.getMaxBuildHeight() || !this.level.getBlockState(blockPos2).getMaterial().isSolid()) {
                    break;
                }
                above = blockPos2.above();
            }
            return super.createPath(blockPos2, i);
        }
        return super.createPath(blockPos, i);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    public Path createPath(Entity entity, int i) {
        return createPath(entity.blockPosition(), i);
    }

    private int getSurfaceY() {
        if (!this.mob.isInWater() || !canFloat()) {
            return Mth.floor(this.mob.getY() + 0.5d);
        }
        int floor = Mth.floor(this.mob.getY());
        Block block = this.level.getBlockState(new BlockPos(this.mob.getX(), floor, this.mob.getZ())).getBlock();
        int i = 0;
        while (block == Blocks.WATER) {
            floor++;
            block = this.level.getBlockState(new BlockPos(this.mob.getX(), floor, this.mob.getZ())).getBlock();
            i++;
            if (i > 16) {
                return Mth.floor(this.mob.getY());
            }
        }
        return floor;
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected void trimPath() {
        super.trimPath();
        if (!this.avoidSun || this.level.canSeeSky(new BlockPos(this.mob.getX(), this.mob.getY() + 0.5d, this.mob.getZ()))) {
            return;
        }
        for (int i = 0; i < this.path.getNodeCount(); i++) {
            Node node = this.path.getNode(i);
            if (this.level.canSeeSky(new BlockPos(node.x, node.y, node.z))) {
                this.path.truncateNodes(i);
                return;
            }
        }
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec32, int i, int i2, int i3) {
        int floor = Mth.floor(vec3.x);
        int floor2 = Mth.floor(vec3.z);
        double d = vec32.x - vec3.x;
        double d2 = vec32.z - vec3.z;
        double d3 = (d * d) + (d2 * d2);
        if (d3 < 1.0E-8d) {
            return false;
        }
        double sqrt = 1.0d / Math.sqrt(d3);
        double d4 = d * sqrt;
        double d5 = d2 * sqrt;
        int i4 = i + 2;
        int i5 = i3 + 2;
        if (!canWalkOn(floor, Mth.floor(vec3.y), floor2, i4, i2, i5, vec3, d4, d5)) {
            return false;
        }
        int i6 = i4 - 2;
        int i7 = i5 - 2;
        double abs = 1.0d / Math.abs(d4);
        double abs2 = 1.0d / Math.abs(d5);
        double d6 = floor - vec3.x;
        double d7 = floor2 - vec3.z;
        if (d4 >= 0.0d) {
            d6 += 1.0d;
        }
        if (d5 >= 0.0d) {
            d7 += 1.0d;
        }
        double d8 = d6 / d4;
        double d9 = d7 / d5;
        int i8 = d4 < 0.0d ? -1 : 1;
        int i9 = d5 < 0.0d ? -1 : 1;
        int floor3 = Mth.floor(vec32.x);
        int floor4 = Mth.floor(vec32.z);
        int i10 = floor3 - floor;
        int i11 = floor4 - floor2;
        do {
            if (i10 * i8 <= 0 && i11 * i9 <= 0) {
                return true;
            }
            if (d8 < d9) {
                d8 += abs;
                floor += i8;
                i10 = floor3 - floor;
            } else {
                d9 += abs2;
                floor2 += i9;
                i11 = floor4 - floor2;
            }
        } while (canWalkOn(floor, Mth.floor(vec3.y), floor2, i6, i2, i7, vec3, d4, d5));
        return false;
    }

    private boolean canWalkOn(int i, int i2, int i3, int i4, int i5, int i6, Vec3 vec3, double d, double d2) {
        int i7 = i - (i4 / 2);
        int i8 = i3 - (i6 / 2);
        if (!canWalkAbove(i7, i2, i8, i4, i5, i6, vec3, d, d2)) {
            return false;
        }
        for (int i9 = i7; i9 < i7 + i4; i9++) {
            for (int i10 = i8; i10 < i8 + i6; i10++) {
                if ((((i9 + 0.5d) - vec3.x) * d) + (((i10 + 0.5d) - vec3.z) * d2) >= 0.0d) {
                    if (!hasValidPathType(this.nodeEvaluator.getBlockPathType(this.level, i9, i2 - 1, i10, this.mob, i4, i5, i6, true, true))) {
                        return false;
                    }
                    BlockPathTypes blockPathType = this.nodeEvaluator.getBlockPathType(this.level, i9, i2, i10, this.mob, i4, i5, i6, true, true);
                    float pathfindingMalus = this.mob.getPathfindingMalus(blockPathType);
                    if (pathfindingMalus < 0.0f || pathfindingMalus >= 8.0f || blockPathType == BlockPathTypes.DAMAGE_FIRE || blockPathType == BlockPathTypes.DANGER_FIRE || blockPathType == BlockPathTypes.DAMAGE_OTHER) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected boolean hasValidPathType(BlockPathTypes blockPathTypes) {
        if (blockPathTypes == BlockPathTypes.WATER || blockPathTypes == BlockPathTypes.LAVA || blockPathTypes == BlockPathTypes.OPEN) {
            return false;
        }
        return true;
    }

    private boolean canWalkAbove(int i, int i2, int i3, int i4, int i5, int i6, Vec3 vec3, double d, double d2) {
        for (BlockPos blockPos : BlockPos.betweenClosed(new BlockPos(i, i2, i3), new BlockPos((i + i4) - 1, (i2 + i5) - 1, (i3 + i6) - 1))) {
            if ((((blockPos.getX() + 0.5d) - vec3.x) * d) + (((blockPos.getZ() + 0.5d) - vec3.z) * d2) >= 0.0d && !this.level.getBlockState(blockPos).isPathfindable(this.level, blockPos, PathComputationType.LAND)) {
                return false;
            }
        }
        return true;
    }

    public void setCanOpenDoors(boolean z) {
        this.nodeEvaluator.setCanOpenDoors(z);
    }

    public boolean canOpenDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public void setAvoidSun(boolean z) {
        this.avoidSun = z;
    }
}
