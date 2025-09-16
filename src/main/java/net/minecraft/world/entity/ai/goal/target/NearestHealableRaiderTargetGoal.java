package net.minecraft.world.entity.ai.goal.target;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raider;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/goal/target/NearestHealableRaiderTargetGoal.class */
public class NearestHealableRaiderTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private int cooldown;

    public NearestHealableRaiderTargetGoal(Raider raider, Class<T> cls, boolean z, @Nullable Predicate<LivingEntity> predicate) {
        super(raider, cls, 500, z, false, predicate);
        this.cooldown = 0;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void decrementCooldown() {
        this.cooldown--;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public boolean canUse() {
        if (this.cooldown > 0 || !this.mob.getRandom().nextBoolean() || !((Raider) this.mob).hasActiveRaid()) {
            return false;
        }
        findTarget();
        return this.target != null;
    }

    @Override // net.minecraft.world.entity.p000ai.goal.target.NearestAttackableTargetGoal, net.minecraft.world.entity.p000ai.goal.target.TargetGoal, net.minecraft.world.entity.p000ai.goal.Goal
    public void start() {
        this.cooldown = 200;
        super.start();
    }
}
