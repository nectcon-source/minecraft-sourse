package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/damagesource/CombatEntry.class */
public class CombatEntry {
    private final DamageSource source;
    private final int time;
    private final float damage;
    private final float health;
    private final String location;
    private final float fallDistance;

    public CombatEntry(DamageSource damageSource, int i, float f, float f2, String str, float f3) {
        this.source = damageSource;
        this.time = i;
        this.damage = f2;
        this.health = f;
        this.location = str;
        this.fallDistance = f3;
    }

    public DamageSource getSource() {
        return this.source;
    }

    public float getDamage() {
        return this.damage;
    }

    public boolean isCombatRelated() {
        return this.source.getEntity() instanceof LivingEntity;
    }

    @Nullable
    public String getLocation() {
        return this.location;
    }

    @Nullable
    public Component getAttackerName() {
        if (getSource().getEntity() == null) {
            return null;
        }
        return getSource().getEntity().getDisplayName();
    }

    public float getFallDistance() {
        if (this.source == DamageSource.OUT_OF_WORLD) {
            return Float.MAX_VALUE;
        }
        return this.fallDistance;
    }
}
