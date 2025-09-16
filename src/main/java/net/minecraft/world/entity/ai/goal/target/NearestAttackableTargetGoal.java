package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/target/NearestAttackableTargetGoal.class */
public class NearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
    protected final Class<T> targetType;
    protected final int randomInterval;
    protected LivingEntity target;
    protected TargetingConditions targetConditions;

    public NearestAttackableTargetGoal(Mob mob, Class<T> cls, boolean z) {
        this(mob, cls, z, false);
    }

    public NearestAttackableTargetGoal(Mob mob, Class<T> cls, boolean z, boolean z2) {
        this(mob, cls, 10, z, z2, null);
    }

    public NearestAttackableTargetGoal(Mob mob, Class<T> cls, int i, boolean z, boolean z2, @Nullable Predicate<LivingEntity> predicate) {
        super(mob, z, z2);
        this.targetType = cls;
        this.randomInterval = i;
        setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.targetConditions = new TargetingConditions().range(getFollowDistance()).selector(predicate);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        }
        findTarget();
        return this.target != null;
    }

    protected AABB getTargetSearchArea(double d) {
        return this.mob.getBoundingBox().inflate(d, 4.0d, d);
    }

    protected void findTarget() {
        if (this.targetType == Player.class || this.targetType == ServerPlayer.class) {
            this.target = this.mob.level.getNearestPlayer(this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        } else {
            this.target = this.mob.level.getNearestLoadedEntity(this.targetType, this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), getTargetSearchArea(getFollowDistance()));
        }
    }

    @Override // net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable LivingEntity livingEntity) {
        this.target = livingEntity;
    }
}
