package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/target/HurtByTargetGoal.class */
public class HurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = new TargetingConditions().allowUnseeable().ignoreInvisibilityTesting();
    private boolean alertSameType;
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    private Class<?>[] toIgnoreAlert;

    public HurtByTargetGoal(PathfinderMob pathfinderMob, Class<?>... clsArr) {
        super(pathfinderMob, true);
        this.toIgnoreDamage = clsArr;
        setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        int lastHurtByMobTimestamp = this.mob.getLastHurtByMobTimestamp();
        LivingEntity lastHurtByMob = this.mob.getLastHurtByMob();
        if (lastHurtByMobTimestamp == this.timestamp || lastHurtByMob == null) {
            return false;
        }
        if (lastHurtByMob.getType() == EntityType.PLAYER && this.mob.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            return false;
        }
        for (Class<?> cls : this.toIgnoreDamage) {
            if (cls.isAssignableFrom(lastHurtByMob.getClass())) {
                return false;
            }
        }
        return canAttack(lastHurtByMob, HURT_BY_TARGETING);
    }

    public HurtByTargetGoal setAlertOthers(Class<?>... clsArr) {
        this.alertSameType = true;
        this.toIgnoreAlert = clsArr;
        return this;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 300;
        if (this.alertSameType) {
            alertOthers();
        }
        super.start();
    }

    protected void alertOthers() {
        double followDistance = getFollowDistance();
        for (Mob mob : this.mob.level.getLoadedEntitiesOfClass(this.mob.getClass(), AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(followDistance, 10.0d, followDistance))) {
            if (this.mob != mob && mob.getTarget() == null && (!(this.mob instanceof TamableAnimal) || ((TamableAnimal) this.mob).getOwner() == ((TamableAnimal) mob).getOwner())) {
                if (!mob.isAlliedTo(this.mob.getLastHurtByMob())) {
                    if (this.toIgnoreAlert != null) {
                        boolean z = false;
                        Class<?>[] clsArr = this.toIgnoreAlert;
                        int length = clsArr.length;
                        int i = 0;
                        while (true) {
                            if (i >= length) {
                                break;
                            }
                            if (mob.getClass() != clsArr[i]) {
                                i++;
                            } else {
                                z = true;
                                break;
                            }
                        }
                        if (z) {
                        }
                    }
                    alertOther(mob, this.mob.getLastHurtByMob());
                }
            }
        }
    }

    protected void alertOther(Mob mob, LivingEntity livingEntity) {
        mob.setTarget(livingEntity);
    }
}
