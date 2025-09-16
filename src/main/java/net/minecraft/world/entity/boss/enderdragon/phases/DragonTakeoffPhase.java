package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/enderdragon/phases/DragonTakeoffPhase.class */
public class DragonTakeoffPhase extends AbstractDragonPhaseInstance {
    private boolean firstTick;
    private Path currentPath;
    private Vec3 targetLocation;

    public DragonTakeoffPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void doServerTick() {
        if (this.firstTick || this.currentPath == null) {
            this.firstTick = false;
            findNewTarget();
        } else if (!this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION).closerThan(this.dragon.position(), 10.0d)) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
        }
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void begin() {
        this.firstTick = true;
        this.currentPath = null;
        this.targetLocation = null;
    }

    private void findNewTarget() {
        int i;
        int findClosestNode = this.dragon.findClosestNode();
        Vec3 headLookVector = this.dragon.getHeadLookVector(1.0f);
        int findClosestNode2 = this.dragon.findClosestNode((-headLookVector.x) * 40.0d, 105.0d, (-headLookVector.z) * 40.0d);
        if (this.dragon.getDragonFight() == null || this.dragon.getDragonFight().getCrystalsAlive() <= 0) {
            i = ((findClosestNode2 - 12) & 7) + 12;
        } else {
            i = findClosestNode2 % 12;
            if (i < 0) {
                i += 12;
            }
        }
        this.currentPath = this.dragon.findPath(findClosestNode, i, null);
        navigateToNextPathNode();
    }

    private void navigateToNextPathNode() {
        double y;
        if (this.currentPath != null) {
            this.currentPath.advance();
            if (!this.currentPath.isDone()) {
                Vec3i nextNodePos = this.currentPath.getNextNodePos();
                this.currentPath.advance();
                do {
                    y = nextNodePos.getY() + (this.dragon.getRandom().nextFloat() * 20.0f);
                } while (y < nextNodePos.getY());
                this.targetLocation = new Vec3(nextNodePos.getX(), y, nextNodePos.getZ());
            }
        }
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public EnderDragonPhase<DragonTakeoffPhase> getPhase() {
        return EnderDragonPhase.TAKEOFF;
    }
}
