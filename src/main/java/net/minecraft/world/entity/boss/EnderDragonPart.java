package net.minecraft.world.entity.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/boss/EnderDragonPart.class */
public class EnderDragonPart extends Entity {
    public final EnderDragon parentMob;
    public final String name;
    private final EntityDimensions size;

    public EnderDragonPart(EnderDragon enderDragon, String str, float f, float f2) {
        super(enderDragon.getType(), enderDragon.level);
        this.size = EntityDimensions.scalable(f, f2);
        refreshDimensions();
        this.parentMob = enderDragon;
        this.name = str;
    }

    @Override // net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
    }

    @Override // net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override // net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean isPickable() {
        return true;
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean hurt(DamageSource damageSource, float f) {
        if (isInvulnerableTo(damageSource)) {
            return false;
        }
        return this.parentMob.hurt(this, damageSource, f);
    }

    @Override // net.minecraft.world.entity.Entity
    /* renamed from: is */
    public boolean is(Entity entity) {
        return this == entity || this.parentMob == entity;
    }

    @Override // net.minecraft.world.entity.Entity
    public Packet<?> getAddEntityPacket() {
        throw new UnsupportedOperationException();
    }

    @Override // net.minecraft.world.entity.Entity
    public EntityDimensions getDimensions(Pose pose) {
        return this.size;
    }
}
