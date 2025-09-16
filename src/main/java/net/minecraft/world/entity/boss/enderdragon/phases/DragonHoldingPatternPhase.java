package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/enderdragon/phases/DragonHoldingPatternPhase.class */
public class DragonHoldingPatternPhase extends AbstractDragonPhaseInstance {
    private static final TargetingConditions NEW_TARGET_TARGETING = new TargetingConditions().range(64.0d);
    private Path currentPath;
    private Vec3 targetLocation;
    private boolean clockwise;

    public DragonHoldingPatternPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public EnderDragonPhase<DragonHoldingPatternPhase> getPhase() {
        return EnderDragonPhase.HOLDING_PATTERN;
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void doServerTick() {
        double distanceToSqr = this.targetLocation == null ? 0.0d : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (distanceToSqr < 100.0d || distanceToSqr > 22500.0d || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            findNewTarget();
        }
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void begin() {
        this.currentPath = null;
        this.targetLocation = null;
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    private void findNewTarget() {
        int i;
        int i2;
        if (this.currentPath != null && this.currentPath.isDone()) {
            BlockPos heightmapPos = this.dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(EndPodiumFeature.END_PODIUM_LOCATION));
            int crystalsAlive = this.dragon.getDragonFight() == null ? 0 : this.dragon.getDragonFight().getCrystalsAlive();
            if (this.dragon.getRandom().nextInt(crystalsAlive + 3) == 0) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING_APPROACH);
                return;
            }
            double d = 64.0d;
            Player nearestPlayer = this.dragon.level.getNearestPlayer(NEW_TARGET_TARGETING, heightmapPos.getX(), heightmapPos.getY(), heightmapPos.getZ());
            if (nearestPlayer != null) {
                d = heightmapPos.distSqr(nearestPlayer.position(), true) / 512.0d;
            }
            if (nearestPlayer != null && !nearestPlayer.abilities.invulnerable && (this.dragon.getRandom().nextInt(Mth.abs((int) d) + 2) == 0 || this.dragon.getRandom().nextInt(crystalsAlive + 2) == 0)) {
                strafePlayer(nearestPlayer);
                return;
            }
        }
        if (this.currentPath == null || this.currentPath.isDone()) {
            int findClosestNode = this.dragon.findClosestNode();
            int i3 = findClosestNode;
            if (this.dragon.getRandom().nextInt(8) == 0) {
                this.clockwise = !this.clockwise;
                i3 += 6;
            }
            if (this.clockwise) {
                i = i3 + 1;
            } else {
                i = i3 - 1;
            }
            if (this.dragon.getDragonFight() == null || this.dragon.getDragonFight().getCrystalsAlive() < 0) {
                i2 = ((i - 12) & 7) + 12;
            } else {
                i2 = i % 12;
                if (i2 < 0) {
                    i2 += 12;
                }
            }
            this.currentPath = this.dragon.findPath(findClosestNode, i2, null);
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }
        navigateToNextPathNode();
    }

    private void strafePlayer(Player player) {
        this.dragon.getPhaseManager().setPhase(EnderDragonPhase.STRAFE_PLAYER);
        ((DragonStrafePlayerPhase) this.dragon.getPhaseManager().getPhase(EnderDragonPhase.STRAFE_PLAYER)).setTarget(player);
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

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void onCrystalDestroyed(EndCrystal endCrystal, BlockPos blockPos, DamageSource damageSource, @Nullable Player player) {
        if (player != null && !player.abilities.invulnerable) {
            strafePlayer(player);
        }
    }
}
