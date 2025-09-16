package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.AbstractArrow;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/enderdragon/phases/AbstractDragonSittingPhase.class */
public abstract class AbstractDragonSittingPhase extends AbstractDragonPhaseInstance {
    public AbstractDragonSittingPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public boolean isSitting() {
        return true;
    }

    @Override // net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance, net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance
    public float onHurt(DamageSource damageSource, float f) {
        if (damageSource.getDirectEntity() instanceof AbstractArrow) {
            damageSource.getDirectEntity().setSecondsOnFire(1);
            return 0.0f;
        }
        return super.onHurt(damageSource, f);
    }
}
