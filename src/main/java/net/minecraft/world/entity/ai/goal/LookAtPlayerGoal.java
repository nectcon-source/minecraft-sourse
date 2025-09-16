package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/LookAtPlayerGoal.class */
public class LookAtPlayerGoal extends Goal {
    protected final Mob mob;
    protected Entity lookAt;
    protected final float lookDistance;
    private int lookTime;
    protected final float probability;
    protected final Class<? extends LivingEntity> lookAtType;
    protected final TargetingConditions lookAtContext;

    public LookAtPlayerGoal(Mob mob, Class<? extends LivingEntity> cls, float f) {
        this(mob, cls, f, 0.02f);
    }

    public LookAtPlayerGoal(Mob mob, Class<? extends LivingEntity> cls, float f, float f2) {
        this.mob = mob;
        this.lookAtType = cls;
        this.lookDistance = f;
        this.probability = f2;
        setFlags(EnumSet.of(Goal.Flag.LOOK));
        if (cls == Player.class) {
            this.lookAtContext = new TargetingConditions().range(f).allowSameTeam().allowInvulnerable().allowNonAttackable().selector(livingEntity -> {
                return EntitySelector.notRiding(mob).test(livingEntity);
            });
        } else {
            this.lookAtContext = new TargetingConditions().range(f).allowSameTeam().allowInvulnerable().allowNonAttackable();
        }
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
        }
        if (this.mob.getTarget() != null) {
            this.lookAt = this.mob.getTarget();
        }
        if (this.lookAtType == Player.class) {
            this.lookAt = this.mob.level.getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        } else {
            this.lookAt = this.mob.level.getNearestLoadedEntity(this.lookAtType, this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.mob.getBoundingBox().inflate(this.lookDistance, 3.0d, this.lookDistance));
        }
        return this.lookAt != null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.lookAt.isAlive() && this.mob.distanceToSqr(this.lookAt) <= ((double) (this.lookDistance * this.lookDistance)) && this.lookTime > 0;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.lookTime = 40 + this.mob.getRandom().nextInt(40);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.lookAt = null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        this.mob.getLookControl().setLookAt(this.lookAt.getX(), this.lookAt.getEyeY(), this.lookAt.getZ());
        this.lookTime--;
    }
}
