package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/enderdragon/phases/DragonStrafePlayerPhase.class */
public class DragonStrafePlayerPhase extends AbstractDragonPhaseInstance {
    private static final Logger LOGGER = LogManager.getLogger();
    private int fireballCharge;
    private Path currentPath;
    private Vec3 targetLocation;
    private LivingEntity attackTarget;
    private boolean holdingPatternClockwise;

    public DragonStrafePlayerPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public void doServerTick() {
        if (this.attackTarget == null) {
            LOGGER.warn("Skipping player strafe phase because no player was found");
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            return;
        }
        if (this.currentPath != null && this.currentPath.isDone()) {
            double x = this.attackTarget.getX();
            double z = this.attackTarget.getZ();
            double x2 = x - this.dragon.getX();
            double z2 = z - this.dragon.getZ();
            this.targetLocation = new Vec3(x, this.attackTarget.getY() + Math.min((0.4000000059604645d + (Mth.sqrt((x2 * x2) + (z2 * z2)) / 80.0d)) - 1.0d, 10.0d), z);
        }
        double distanceToSqr = this.targetLocation == null ? 0.0d : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (distanceToSqr < 100.0d || distanceToSqr > 22500.0d) {
            findNewTarget();
        }
        if (this.attackTarget.distanceToSqr(this.dragon) < 4096.0d) {
            if (this.dragon.canSee(this.attackTarget)) {
                this.fireballCharge++;
                float acos = ((float) (Math.acos((float) new Vec3(Mth.sin(this.dragon.yRot * 0.017453292f), 0.0d, -Mth.cos(this.dragon.yRot * 0.017453292f)).normalize().dot(new Vec3(this.attackTarget.getX() - this.dragon.getX(), 0.0d, this.attackTarget.getZ() - this.dragon.getZ()).normalize())) * 57.2957763671875d)) + 0.5f;
                if (this.fireballCharge >= 5 && acos >= 0.0f && acos < 10.0f) {
                    Vec3 viewVector = this.dragon.getViewVector(1.0f);
                    double x3 = this.dragon.head.getX() - (viewVector.x * 1.0d);
                    double y = this.dragon.head.getY(0.5d) + 0.5d;
                    double z3 = this.dragon.head.getZ() - (viewVector.z * 1.0d);
                    double x4 = this.attackTarget.getX() - x3;
                    double y2 = this.attackTarget.getY(0.5d) - y;
                    double z4 = this.attackTarget.getZ() - z3;
                    if (!this.dragon.isSilent()) {
                        this.dragon.level.levelEvent(null, 1017, this.dragon.blockPosition(), 0);
                    }
                    DragonFireball dragonFireball = new DragonFireball(this.dragon.level, this.dragon, x4, y2, z4);
                    dragonFireball.moveTo(x3, y, z3, 0.0f, 0.0f);
                    this.dragon.level.addFreshEntity(dragonFireball);
                    this.fireballCharge = 0;
                    if (this.currentPath != null) {
                        while (!this.currentPath.isDone()) {
                            this.currentPath.advance();
                        }
                    }
                    this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
                    return;
                }
                return;
            }
            if (this.fireballCharge > 0) {
                this.fireballCharge--;
                return;
            }
            return;
        }
        if (this.fireballCharge > 0) {
            this.fireballCharge--;
        }
    }

    private void findNewTarget() {
        int i;
        int i2;
        if (this.currentPath == null || this.currentPath.isDone()) {
            int findClosestNode = this.dragon.findClosestNode();
            int i3 = findClosestNode;
            if (this.dragon.getRandom().nextInt(8) == 0) {
                this.holdingPatternClockwise = !this.holdingPatternClockwise;
                i3 += 6;
            }
            if (this.holdingPatternClockwise) {
                i = i3 + 1;
            } else {
                i = i3 - 1;
            }
            if (this.dragon.getDragonFight() == null || this.dragon.getDragonFight().getCrystalsAlive() <= 0) {
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
    public void begin() {
        this.fireballCharge = 0;
        this.targetLocation = null;
        this.currentPath = null;
        this.attackTarget = null;
    }

    public void setTarget(LivingEntity livingEntity) {
        this.attackTarget = livingEntity;
        int findClosestNode = this.dragon.findClosestNode();
        int findClosestNode2 = this.dragon.findClosestNode(this.attackTarget.getX(), this.attackTarget.getY(), this.attackTarget.getZ());
        int floor = Mth.floor(this.attackTarget.getX());
        int floor2 = Mth.floor(this.attackTarget.getZ());
        double x = floor - this.dragon.getX();
        double z = floor2 - this.dragon.getZ();
        this.currentPath = this.dragon.findPath(findClosestNode, findClosestNode2, new Node(floor, Mth.floor(this.attackTarget.getY() + Math.min((0.4000000059604645d + (Mth.sqrt((x * x) + (z * z)) / 80.0d)) - 1.0d, 10.0d)), floor2));
        if (this.currentPath != null) {
            this.currentPath.advance();
            navigateToNextPathNode();
        }
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public EnderDragonPhase<DragonStrafePlayerPhase> getPhase() {
        return EnderDragonPhase.STRAFE_PLAYER;
    }
}
