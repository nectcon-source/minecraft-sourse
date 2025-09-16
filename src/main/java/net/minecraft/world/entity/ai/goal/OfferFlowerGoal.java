package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/OfferFlowerGoal.class */
public class OfferFlowerGoal extends Goal {
    private static final TargetingConditions OFFER_TARGER_CONTEXT = new TargetingConditions().range(6.0d).allowSameTeam().allowInvulnerable();
    private final IronGolem golem;
    private Villager villager;
    private int tick;

    public OfferFlowerGoal(IronGolem ironGolem) {
        this.golem = ironGolem;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (!this.golem.level.isDay() || this.golem.getRandom().nextInt(8000) != 0) {
            return false;
        }
        this.villager = (Villager) this.golem.level.getNearestEntity(Villager.class, OFFER_TARGER_CONTEXT, this.golem, this.golem.getX(), this.golem.getY(), this.golem.getZ(), this.golem.getBoundingBox().inflate(6.0d, 2.0d, 6.0d));
        return this.villager != null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        return this.tick > 0;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.tick = 400;
        this.golem.offerFlower(true);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void stop() {
        this.golem.offerFlower(false);
        this.villager = null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public void tick() {
        this.golem.getLookControl().setLookAt(this.villager, 30.0f, 30.0f);
        this.tick--;
    }
}
