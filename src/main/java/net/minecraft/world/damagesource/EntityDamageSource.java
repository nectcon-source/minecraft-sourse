package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/damagesource/EntityDamageSource.class */
public class EntityDamageSource extends DamageSource {

    @Nullable
    protected final Entity entity;
    private boolean isThorns;

    public EntityDamageSource(String str, @Nullable Entity entity) {
        super(str);
        this.entity = entity;
    }

    public EntityDamageSource setThorns() {
        this.isThorns = true;
        return this;
    }

    public boolean isThorns() {
        return this.isThorns;
    }

    @Override // net.minecraft.world.damagesource.DamageSource
    @Nullable
    public Entity getEntity() {
        return this.entity;
    }

    @Override // net.minecraft.world.damagesource.DamageSource
    public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
        ItemStack mainHandItem = this.entity instanceof LivingEntity ? ((LivingEntity) this.entity).getMainHandItem() : ItemStack.EMPTY;
        String str = "death.attack." + this.msgId;
        if (!mainHandItem.isEmpty() && mainHandItem.hasCustomHoverName()) {
            return new TranslatableComponent(str + ".item", livingEntity.getDisplayName(), this.entity.getDisplayName(), mainHandItem.getDisplayName());
        }
        return new TranslatableComponent(str, livingEntity.getDisplayName(), this.entity.getDisplayName());
    }

    @Override // net.minecraft.world.damagesource.DamageSource
    public boolean scalesWithDifficulty() {
        return (this.entity == null || !(this.entity instanceof LivingEntity) || (this.entity instanceof Player)) ? false : true;
    }

    @Override // net.minecraft.world.damagesource.DamageSource
    @Nullable
    public Vec3 getSourcePosition() {
        if (this.entity != null) {
            return this.entity.position();
        }
        return null;
    }

    @Override // net.minecraft.world.damagesource.DamageSource
    public String toString() {
        return "EntityDamageSource (" + this.entity + ")";
    }
}
