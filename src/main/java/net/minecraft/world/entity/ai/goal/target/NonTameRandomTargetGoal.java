package net.minecraft.world.entity.ai.goal.target;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/target/NonTameRandomTargetGoal.class */
public class NonTameRandomTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final TamableAnimal tamableMob;

    public NonTameRandomTargetGoal(TamableAnimal tamableAnimal, Class<T> cls, boolean z, @Nullable Predicate<LivingEntity> predicate) {
        super(tamableAnimal, cls, 10, z, false, predicate);
        this.tamableMob = tamableAnimal;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return !this.tamableMob.isTame() && super.canUse();
    }

    @Override // net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canContinueToUse() {
        if (this.targetConditions != null) {
            return this.targetConditions.test(this.mob, this.target);
        }
        return super.canContinueToUse();
    }
}
