package net.minecraft.world.damagesource;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/damagesource/IndirectEntityDamageSource.class */
public class IndirectEntityDamageSource extends EntityDamageSource {
    private final Entity owner;

    public IndirectEntityDamageSource(String str, Entity entity, @Nullable Entity entity2) {
        super(str, entity);
        this.owner = entity2;
    }

    @Override // net.minecraft.world.damagesource.DamageSource
    @Nullable
    public Entity getDirectEntity() {
        return this.entity;
    }

    @Override // net.minecraft.world.damagesource.EntityDamageSource, net.minecraft.world.damagesource.DamageSource
    @Nullable
    public Entity getEntity() {
        return this.owner;
    }

    @Override // net.minecraft.world.damagesource.EntityDamageSource, net.minecraft.world.damagesource.DamageSource
    public Component getLocalizedDeathMessage(LivingEntity livingEntity) {
        Component displayName = this.owner == null ? this.entity.getDisplayName() : this.owner.getDisplayName();
        ItemStack mainHandItem = this.owner instanceof LivingEntity ? ((LivingEntity) this.owner).getMainHandItem() : ItemStack.EMPTY;
        String str = "death.attack." + this.msgId;
        String str2 = str + ".item";
        if (!mainHandItem.isEmpty() && mainHandItem.hasCustomHoverName()) {
            return new TranslatableComponent(str2, livingEntity.getDisplayName(), displayName, mainHandItem.getDisplayName());
        }
        return new TranslatableComponent(str, livingEntity.getDisplayName(), displayName);
    }
}
