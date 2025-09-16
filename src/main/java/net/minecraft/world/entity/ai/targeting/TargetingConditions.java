package net.minecraft.world.entity.ai.targeting;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/targeting/TargetingConditions.class */
public class TargetingConditions {
    public static final TargetingConditions DEFAULT = new TargetingConditions();
    private boolean allowInvulnerable;
    private boolean allowSameTeam;
    private boolean allowUnseeable;
    private boolean allowNonAttackable;
    private Predicate<LivingEntity> selector;
    private double range = -1.0d;
    private boolean testInvisible = true;

    public TargetingConditions range(double d) {
        this.range = d;
        return this;
    }

    public TargetingConditions allowInvulnerable() {
        this.allowInvulnerable = true;
        return this;
    }

    public TargetingConditions allowSameTeam() {
        this.allowSameTeam = true;
        return this;
    }

    public TargetingConditions allowUnseeable() {
        this.allowUnseeable = true;
        return this;
    }

    public TargetingConditions allowNonAttackable() {
        this.allowNonAttackable = true;
        return this;
    }

    public TargetingConditions ignoreInvisibilityTesting() {
        this.testInvisible = false;
        return this;
    }

    public TargetingConditions selector(@Nullable Predicate<LivingEntity> predicate) {
        this.selector = predicate;
        return this;
    }

    public boolean test(@Nullable LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (livingEntity == livingEntity2 || livingEntity2.isSpectator() || !livingEntity2.isAlive()) {
            return false;
        }
        if (!this.allowInvulnerable && livingEntity2.isInvulnerable()) {
            return false;
        }
        if (this.selector != null && !this.selector.test(livingEntity2)) {
            return false;
        }
        if (livingEntity != null) {
            if (!this.allowNonAttackable && (!livingEntity.canAttack(livingEntity2) || !livingEntity.canAttackType(livingEntity2.getType()))) {
                return false;
            }
            if (!this.allowSameTeam && livingEntity.isAlliedTo(livingEntity2)) {
                return false;
            }
            if (this.range > 0.0d) {
                double max = Math.max(this.range * (this.testInvisible ? livingEntity2.getVisibilityPercent(livingEntity) : 1.0d), 2.0d);
                if (livingEntity.distanceToSqr(livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ()) > max * max) {
                    return false;
                }
            }
            if (!this.allowUnseeable && (livingEntity instanceof Mob) && !((Mob) livingEntity).getSensing().canSee(livingEntity2)) {
                return false;
            }
            return true;
        }
        return true;
    }
}
