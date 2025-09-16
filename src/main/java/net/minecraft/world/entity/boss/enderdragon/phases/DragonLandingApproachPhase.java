package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/enderdragon/phases/DragonLandingApproachPhase.class */
public class DragonLandingApproachPhase extends AbstractDragonPhaseInstance {
    private static final TargetingConditions NEAR_EGG_TARGETING = new TargetingConditions().range(128.0d);
    private Path currentPath;
    private Vec3 targetLocation;

    public DragonLandingApproachPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public EnderDragonPhase<DragonLandingApproachPhase> getPhase() {
        return EnderDragonPhase.LANDING_APPROACH;
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void begin() {
        this.currentPath = null;
        this.targetLocation = null;
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void doServerTick() {
        double distanceToSqr = this.targetLocation == null ? 0.0d : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (distanceToSqr < 100.0d || distanceToSqr > 22500.0d || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            findNewTarget();
        }
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    private void findNewTarget() {
        int findClosestNode;
        if (this.currentPath == null || this.currentPath.isDone()) {
            int findClosestNode2 = this.dragon.findClosestNode();
            BlockPos heightmapPos = this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
            Player nearestPlayer = this.dragon.level.getNearestPlayer(NEAR_EGG_TARGETING, heightmapPos.getX(), heightmapPos.getY(), heightmapPos.getZ());
            if (nearestPlayer != null) {
                Vec3 normalize = new Vec3(nearestPlayer.getX(), 0.0d, nearestPlayer.getZ()).normalize();
                findClosestNode = this.dragon.findClosestNode((-normalize.x) * 40.0d, 105.0d, (-normalize.z) * 40.0d);
            } else {
                findClosestNode = this.dragon.findClosestNode(40.0d, heightmapPos.getY(), 0.0d);
            }
            this.currentPath = this.dragon.findPath(findClosestNode2, findClosestNode, new Node(heightmapPos.getX(), heightmapPos.getY(), heightmapPos.getZ()));
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }
        navigateToNextPathNode();
        if (this.currentPath != null && this.currentPath.isDone()) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING);
        }
    }

    private void navigateToNextPathNode() {
        double y;
        if (this.currentPath != null && !this.currentPath.isDone()) {
            Vec3i nextNodePos = this.currentPath.getNextNodePos();
            this.currentPath.advance();
            double x = nextNodePos.getX();
            double z = nextNodePos.getZ();
            do {
                y = nextNodePos.getY() + (this.dragon.getRandom().nextFloat() * 20.0f);
            } while (y < nextNodePos.getY());
            this.targetLocation = new Vec3(x, y, z);
        }
    }
}
