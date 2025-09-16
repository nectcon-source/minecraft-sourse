package net.minecraft.world.entity.ai.goal.target;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raider;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/target/NearestAttackableWitchTargetGoal.class */
public class NearestAttackableWitchTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private boolean canAttack;

    public NearestAttackableWitchTargetGoal(Raider raider, Class<T> cls, int i, boolean z, boolean z2, @Nullable Predicate<LivingEntity> predicate) {
        super(raider, cls, i, z, z2, predicate);
        this.canAttack = true;
    }

    public void setCanAttack(boolean z) {
        this.canAttack = z;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        return this.canAttack && super.canUse();
    }
}
