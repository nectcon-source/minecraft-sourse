package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.util.IntRange;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/RangedCrossbowAttackGoal.class */
public class RangedCrossbowAttackGoal<T extends Monster & RangedAttackMob & CrossbowAttackMob> extends Goal {
    public static final IntRange PATHFINDING_DELAY_RANGE = new IntRange(20, 40);
    private final T mob;
    private CrossbowState crossbowState = CrossbowState.UNCHARGED;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/RangedCrossbowAttackGoal$CrossbowState.class */
    enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK
    }

    public RangedCrossbowAttackGoal(T t, double d, float f) {
        this.mob = t;
        this.speedModifier = d;
        this.attackRadiusSqr = f * f;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return isValidTarget() && isHoldingCrossbow();
    }

    private boolean isHoldingCrossbow() {
        return this.mob.isHolding(Items.CROSSBOW);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return isValidTarget() && (canUse() || !this.mob.getNavigation().isDone()) && isHoldingCrossbow();
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setTarget(null);
        this.seeTime = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
            this.mob.setChargingCrossbow(false);
            CrossbowItem.setCharged(this.mob.getUseItem(), false);
        }
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        boolean canSee = this.mob.getSensing().canSee(target);
        if (canSee != (this.seeTime > 0)) {
            this.seeTime = 0;
        }
        if (canSee) {
            this.seeTime++;
        } else {
            this.seeTime--;
        }
        boolean z = (this.mob.distanceToSqr(target) > ((double) this.attackRadiusSqr) || this.seeTime < 5) && this.attackDelay == 0;
        if (z) {
            this.updatePathDelay--;
            if (this.updatePathDelay <= 0) {
                this.mob.getNavigation().moveTo(target, canRun() ? this.speedModifier : this.speedModifier * 0.5d);
                this.updatePathDelay = PATHFINDING_DELAY_RANGE.randomValue(this.mob.getRandom());
            }
        } else {
            this.updatePathDelay = 0;
            this.mob.getNavigation().stop();
        }
        this.mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
        if (this.crossbowState == CrossbowState.UNCHARGED) {
            if (!z) {
                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.CROSSBOW));
                this.crossbowState = CrossbowState.CHARGING;
                this.mob.setChargingCrossbow(true);
                return;
            }
            return;
        }
        if (this.crossbowState == CrossbowState.CHARGING) {
            if (!this.mob.isUsingItem()) {
                this.crossbowState = CrossbowState.UNCHARGED;
            }
            if (this.mob.getTicksUsingItem() >= CrossbowItem.getChargeDuration(this.mob.getUseItem())) {
                this.mob.releaseUsingItem();
                this.crossbowState = CrossbowState.CHARGED;
                this.attackDelay = 20 + this.mob.getRandom().nextInt(20);
                this.mob.setChargingCrossbow(false);
                return;
            }
            return;
        }
        if (this.crossbowState == CrossbowState.CHARGED) {
            this.attackDelay--;
            if (this.attackDelay == 0) {
                this.crossbowState = CrossbowState.READY_TO_ATTACK;
                return;
            }
            return;
        }
        if (this.crossbowState == CrossbowState.READY_TO_ATTACK && canSee) {
            this.mob.performRangedAttack(target, 1.0f);
            CrossbowItem.setCharged(this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.CROSSBOW)), false);
            this.crossbowState = CrossbowState.UNCHARGED;
        }
    }

    private boolean canRun() {
        return this.crossbowState == CrossbowState.UNCHARGED;
    }
}
