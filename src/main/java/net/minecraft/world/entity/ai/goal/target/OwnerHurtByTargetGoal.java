package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/target/OwnerHurtByTargetGoal.class */
public class OwnerHurtByTargetGoal extends TargetGoal {
    private final TamableAnimal tameAnimal;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public OwnerHurtByTargetGoal(TamableAnimal tamableAnimal) {
        super(tamableAnimal, false);
        this.tameAnimal = tamableAnimal;
        setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override // net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        LivingEntity owner;
        if (!this.tameAnimal.isTame() || this.tameAnimal.isOrderedToSit() || (owner = this.tameAnimal.getOwner()) == null) {
            return false;
        }
        this.ownerLastHurtBy = owner.getLastHurtByMob();
        return owner.getLastHurtByMobTimestamp() != this.timestamp && canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT) && this.tameAnimal.wantsToAttack(this.ownerLastHurtBy, owner);
    }

    @Override // net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity owner = this.tameAnimal.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
