package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/AvoidEntityGoal.class */
public class AvoidEntityGoal<T extends LivingEntity> extends Goal {
    protected final PathfinderMob mob;
    private final double walkSpeedModifier;
    private final double sprintSpeedModifier;
    protected T toAvoid;
    protected final float maxDist;
    protected Path path;
    protected final PathNavigation pathNav;
    protected final Class<T> avoidClass;
    protected final Predicate<LivingEntity> avoidPredicate;
    protected final Predicate<LivingEntity> predicateOnAvoidEntity;
    private final TargetingConditions avoidEntityTargeting;

    /* JADX WARN: Illegal instructions before constructor call */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public AvoidEntityGoal(net.minecraft.world.entity.PathfinderMob var1, java.lang.Class<T> var2, float var3, double var4, double var6) {
        this(var1, var2, var0 -> true, var3, var4, var6, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
    }

    public AvoidEntityGoal(PathfinderMob pathfinderMob, Class<T> cls, Predicate<LivingEntity> predicate, float f, double d, double d2, Predicate<LivingEntity> predicate2) {
        this.mob = pathfinderMob;
        this.avoidClass = cls;
        this.avoidPredicate = predicate;
        this.maxDist = f;
        this.walkSpeedModifier = d;
        this.sprintSpeedModifier = d2;
        this.predicateOnAvoidEntity = predicate2;
        this.pathNav = pathfinderMob.getNavigation();
        setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.avoidEntityTargeting = new TargetingConditions().range(f).selector(predicate2.and(predicate));
    }

    public AvoidEntityGoal(PathfinderMob pathfinderMob, Class<T> cls, float f, double d, double d2, Predicate<LivingEntity> predicate) {
        this(pathfinderMob, cls, livingEntity -> {
            return true;
        }, f, d, d2, predicate);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        Vec3 posAvoid;
        this.toAvoid = (T) this.mob.level.getNearestLoadedEntity(this.avoidClass, this.avoidEntityTargeting, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ(), this.mob.getBoundingBox().inflate(this.maxDist, 3.0d, this.maxDist));
        if (this.toAvoid == null || (posAvoid = RandomPos.getPosAvoid(this.mob, 16, 7, this.toAvoid.position())) == null || this.toAvoid.distanceToSqr(posAvoid.x, posAvoid.y, posAvoid.z) < this.toAvoid.distanceToSqr(this.mob)) {
            return false;
        }
        this.path = this.pathNav.createPath(posAvoid.x, posAvoid.y, posAvoid.z, 0);
        return this.path != null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return !this.pathNav.isDone();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.pathNav.moveTo(this.path, this.walkSpeedModifier);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.toAvoid = null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        if (this.mob.distanceToSqr(this.toAvoid) < 49.0d) {
            this.mob.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
        } else {
            this.mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
        }
    }
}
