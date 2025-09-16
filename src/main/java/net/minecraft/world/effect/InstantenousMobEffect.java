package net.minecraft.world.effect;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/effect/InstantenousMobEffect.class */
public class InstantenousMobEffect extends MobEffect {
    public InstantenousMobEffect(MobEffectCategory mobEffectCategory, int i) {
        super(mobEffectCategory, i);
    }

    @Override // net.minecraft.world.effect.MobEffect
    public boolean isInstantenous() {
        return true;
    }

    @Override // net.minecraft.world.effect.MobEffect
    public boolean isDurationEffectTick(int i, int i2) {
        return i >= 1;
    }
}
