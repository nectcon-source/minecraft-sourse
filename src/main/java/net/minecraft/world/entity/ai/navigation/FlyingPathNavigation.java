package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/navigation/FlyingPathNavigation.class */
public class FlyingPathNavigation extends PathNavigation {
    public FlyingPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected PathFinder createPathFinder(int i) {
        this.nodeEvaluator = new FlyNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        return new PathFinder(this.nodeEvaluator, i);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected boolean canUpdatePath() {
        return (canFloat() && isInLiquid()) || !this.mob.isPassenger();
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected Vec3 getTempMobPos() {
        return this.mob.position();
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    public Path createPath(Entity entity, int i) {
        return createPath(entity.blockPosition(), i);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    public void tick() {
        this.tick++;
        if (this.hasDelayedRecomputation) {
            recomputePath();
        }
        if (isDone()) {
            return;
        }
        if (canUpdatePath()) {
            followThePath();
        } else if (this.path != null && !this.path.isDone()) {
            Vec3 nextEntityPos = this.path.getNextEntityPos(this.mob);
            if (Mth.floor(this.mob.getX()) == Mth.floor(nextEntityPos.x) && Mth.floor(this.mob.getY()) == Mth.floor(nextEntityPos.y) && Mth.floor(this.mob.getZ()) == Mth.floor(nextEntityPos.z)) {
                this.path.advance();
            }
        }
        DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
        if (isDone()) {
            return;
        }
        Vec3 nextEntityPos2 = this.path.getNextEntityPos(this.mob);
        this.mob.getMoveControl().setWantedPosition(nextEntityPos2.x, nextEntityPos2.y, nextEntityPos2.z, this.speedModifier);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec32, int i, int i2, int i3) {
        int floor = Mth.floor(vec3.x);
        int floor2 = Mth.floor(vec3.y);
        int floor3 = Mth.floor(vec3.z);
        double d = vec32.x - vec3.x;
        double d2 = vec32.y - vec3.y;
        double d3 = vec32.z - vec3.z;
        double d4 = (d * d) + (d2 * d2) + (d3 * d3);
        if (d4 < 1.0E-8d) {
            return false;
        }
        double sqrt = 1.0d / Math.sqrt(d4);
        double d5 = d * sqrt;
        double d6 = d2 * sqrt;
        double d7 = d3 * sqrt;
        double abs = 1.0d / Math.abs(d5);
        double abs2 = 1.0d / Math.abs(d6);
        double abs3 = 1.0d / Math.abs(d7);
        double d8 = floor - vec3.x;
        double d9 = floor2 - vec3.y;
        double d10 = floor3 - vec3.z;
        if (d5 >= 0.0d) {
            d8 += 1.0d;
        }
        if (d6 >= 0.0d) {
            d9 += 1.0d;
        }
        if (d7 >= 0.0d) {
            d10 += 1.0d;
        }
        double d11 = d8 / d5;
        double d12 = d9 / d6;
        double d13 = d10 / d7;
        int i4 = d5 < 0.0d ? -1 : 1;
        int i5 = d6 < 0.0d ? -1 : 1;
        int i6 = d7 < 0.0d ? -1 : 1;
        int floor4 = Mth.floor(vec32.x);
        int floor5 = Mth.floor(vec32.y);
        int floor6 = Mth.floor(vec32.z);
        int i7 = floor4 - floor;
        int i8 = floor5 - floor2;
        int i9 = floor6 - floor3;
        while (true) {
            if (i7 * i4 <= 0 && i8 * i5 <= 0 && i9 * i6 <= 0) {
                return true;
            }
            if (d11 < d13 && d11 <= d12) {
                d11 += abs;
                floor += i4;
                i7 = floor4 - floor;
            } else if (d12 < d11 && d12 <= d13) {
                d12 += abs2;
                floor2 += i5;
                i8 = floor5 - floor2;
            } else {
                d13 += abs3;
                floor3 += i6;
                i9 = floor6 - floor3;
            }
        }
    }

    public void setCanOpenDoors(boolean z) {
        this.nodeEvaluator.setCanOpenDoors(z);
    }

    public void setCanPassDoors(boolean z) {
        this.nodeEvaluator.setCanPassDoors(z);
    }

    @Override // net.minecraft.world.entity.p000ai.navigation.PathNavigation
    public boolean isStableDestination(BlockPos blockPos) {
        return this.level.getBlockState(blockPos).entityCanStandOn(this.level, blockPos, this.mob);
    }
}
