package net.minecraft.world.entity.ai.goal;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/MoveThroughVillageGoal.class */
public class MoveThroughVillageGoal extends Goal {
    protected final PathfinderMob mob;
    private final double speedModifier;
    private Path path;
    private BlockPos poiPos;
    private final boolean onlyAtNight;
    private final List<BlockPos> visited = Lists.newArrayList();
    private final int distanceToPoi;
    private final BooleanSupplier canDealWithDoors;

    public MoveThroughVillageGoal(PathfinderMob pathfinderMob, double d, boolean z, int i, BooleanSupplier booleanSupplier) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.onlyAtNight = z;
        this.distanceToPoi = i;
        this.canDealWithDoors = booleanSupplier;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
        if (!GoalUtils.hasGroundPathNavigation(pathfinderMob)) {
            throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
        }
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        Vec3 landPos;
        if (!GoalUtils.hasGroundPathNavigation(this.mob)) {
            return false;
        }
        updateVisited();
        if (this.onlyAtNight && this.mob.level.isDay()) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel) this.mob.level;
        BlockPos blockPosition = this.mob.blockPosition();
        if (!serverLevel.isCloseToVillage(blockPosition, 6) || (landPos = RandomPos.getLandPos(this.mob, 15, 7, blockPos -> {
            if (!serverLevel.isVillage(blockPos)) {
                return Double.NEGATIVE_INFINITY;
            }
            Optional<BlockPos> find = serverLevel.getPoiManager().find(PoiType.ALL, this::hasNotVisited, blockPos, 10, PoiManager.Occupancy.IS_OCCUPIED);
            if (!find.isPresent()) {
                return Double.NEGATIVE_INFINITY;
            }
            return -find.get().distSqr(blockPosition);
        })) == null) {
            return false;
        }
        Optional<BlockPos> find = serverLevel.getPoiManager().find(PoiType.ALL, this::hasNotVisited, new BlockPos(landPos), 10, PoiManager.Occupancy.IS_OCCUPIED);
        if (!find.isPresent()) {
            return false;
        }
        this.poiPos = find.get().immutable();
        GroundPathNavigation groundPathNavigation = (GroundPathNavigation) this.mob.getNavigation();
        boolean canOpenDoors = groundPathNavigation.canOpenDoors();
        groundPathNavigation.setCanOpenDoors(this.canDealWithDoors.getAsBoolean());
        this.path = groundPathNavigation.createPath(this.poiPos, 0);
        groundPathNavigation.setCanOpenDoors(canOpenDoors);
        if (this.path == null) {
            Vec3 posTowards = RandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(this.poiPos));
            if (posTowards == null) {
                return false;
            }
            groundPathNavigation.setCanOpenDoors(this.canDealWithDoors.getAsBoolean());
            this.path = this.mob.getNavigation().createPath(posTowards.x, posTowards.y, posTowards.z, 0);
            groundPathNavigation.setCanOpenDoors(canOpenDoors);
            if (this.path == null) {
                return false;
            }
        }
        int i = 0;
        while (true) {
            if (i >= this.path.getNodeCount()) {
                break;
            }
            Node node = this.path.getNode(i);
            if (!DoorBlock.isWoodenDoor(this.mob.level, new BlockPos(node.x, node.y + 1, node.z))) {
                i++;
            } else {
                this.path = this.mob.getNavigation().createPath(node.x, node.y, node.z, 0);
                break;
            }
        }
        return this.path != null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return (this.mob.getNavigation().isDone() || this.poiPos.closerThan(this.mob.position(), (double) (this.mob.getBbWidth() + ((float) this.distanceToPoi)))) ? false : true;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        if (this.mob.getNavigation().isDone() || this.poiPos.closerThan(this.mob.position(), this.distanceToPoi)) {
            this.visited.add(this.poiPos);
        }
    }

    private boolean hasNotVisited(BlockPos blockPos) {
        Iterator<BlockPos> it = this.visited.iterator();
        while (it.hasNext()) {
            if (Objects.equals(blockPos, it.next())) {
                return false;
            }
        }
        return true;
    }

    private void updateVisited() {
        if (this.visited.size() > 15) {
            this.visited.remove(0);
        }
    }
}
