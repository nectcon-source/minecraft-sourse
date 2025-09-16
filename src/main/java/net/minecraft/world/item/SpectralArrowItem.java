package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/SpectralArrowItem.class */
public class SpectralArrowItem extends ArrowItem {
    public SpectralArrowItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.ArrowItem
    public AbstractArrow createArrow(Level level, ItemStack itemStack, LivingEntity livingEntity) {
        return new SpectralArrow(level, livingEntity);
    }
}
