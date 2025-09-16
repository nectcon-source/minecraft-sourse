package net.minecraft.world.entity.ai.goal.target;

import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.scores.Team;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/target/TargetGoal.class */
public abstract class TargetGoal extends Goal {
    protected final Mob mob;
    protected final boolean mustSee;
    private final boolean mustReach;
    private int reachCache;
    private int reachCacheTime;
    private int unseenTicks;
    protected LivingEntity targetMob;
    protected int unseenMemoryTicks;

    public TargetGoal(Mob mob, boolean z) {
        this(mob, z, false);
    }

    public TargetGoal(Mob mob, boolean z, boolean z2) {
        this.unseenMemoryTicks = 60;
        this.mob = mob;
        this.mustSee = z;
        this.mustReach = z2;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            target = this.targetMob;
        }
        if (target == null || !target.isAlive()) {
            return false;
        }
        Team team = this.mob.getTeam();
        Team team2 = target.getTeam();
        if (team != null && team2 == team) {
            return false;
        }
        double followDistance = getFollowDistance();
        if (this.mob.distanceToSqr(target) > followDistance * followDistance) {
            return false;
        }
        if (this.mustSee) {
            if (this.mob.getSensing().canSee(target)) {
                this.unseenTicks = 0;
            } else {
                int i = this.unseenTicks + 1;
                this.unseenTicks = i;
                if (i > this.unseenMemoryTicks) {
                    return false;
                }
            }
        }
        if ((target instanceof Player) && ((Player) target).abilities.invulnerable) {
            return false;
        }
        this.mob.setTarget(target);
        return true;
    }

    protected double getFollowDistance() {
        return this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.reachCache = 0;
        this.reachCacheTime = 0;
        this.unseenTicks = 0;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.mob.setTarget(null);
        this.targetMob = null;
    }

    protected boolean canAttack(@Nullable LivingEntity livingEntity, TargetingConditions targetingConditions) {
        if (livingEntity == null || !targetingConditions.test(this.mob, livingEntity) || !this.mob.isWithinRestriction(livingEntity.blockPosition())) {
            return false;
        }
        if (this.mustReach) {
            int i = this.reachCacheTime - 1;
            this.reachCacheTime = i;
            if (i <= 0) {
                this.reachCache = 0;
            }
            if (this.reachCache == 0) {
                this.reachCache = canReach(livingEntity) ? 1 : 2;
            }
            if (this.reachCache == 2) {
                return false;
            }
            return true;
        }
        return true;
    }

    private boolean canReach(LivingEntity livingEntity) {
        Node endNode;
        this.reachCacheTime = 10 + this.mob.getRandom().nextInt(5);
        Path createPath = this.mob.getNavigation().createPath(livingEntity, 0);
        if (createPath == null || (endNode = createPath.getEndNode()) == null) {
            return false;
        }
        int floor = endNode.x - Mth.floor(livingEntity.getX());
        int floor2 = endNode.z - Mth.floor(livingEntity.getZ());
        return ((double) ((floor * floor) + (floor2 * floor2))) <= 2.25d;
    }

    public TargetGoal setUnseenMemoryTicks(int i) {
        this.unseenMemoryTicks = i;
        return this;
    }
}
