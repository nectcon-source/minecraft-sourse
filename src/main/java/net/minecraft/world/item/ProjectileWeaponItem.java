package net.minecraft.world.item;

import java.util.function.Predicate;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ProjectileWeaponItem.class */
public abstract class ProjectileWeaponItem extends Item {
    public static final Predicate<ItemStack> ARROW_ONLY = itemStack -> {
        return itemStack.getItem().is(ItemTags.ARROWS);
    };
    public static final Predicate<ItemStack> ARROW_OR_FIREWORK = ARROW_ONLY.or(itemStack -> {
        return itemStack.getItem() == Items.FIREWORK_ROCKET;
    });

    public abstract Predicate<ItemStack> getAllSupportedProjectiles();

    public abstract int getDefaultProjectileRange();

    public ProjectileWeaponItem(Item.Properties properties) {
        super(properties);
    }

    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return getAllSupportedProjectiles();
    }

    public static ItemStack getHeldProjectile(LivingEntity livingEntity, Predicate<ItemStack> predicate) {
        if (predicate.test(livingEntity.getItemInHand(InteractionHand.OFF_HAND))) {
            return livingEntity.getItemInHand(InteractionHand.OFF_HAND);
        }
        if (predicate.test(livingEntity.getItemInHand(InteractionHand.MAIN_HAND))) {
            return livingEntity.getItemInHand(InteractionHand.MAIN_HAND);
        }
        return ItemStack.EMPTY;
    }

    @Override // net.minecraft.world.item.Item
    public int getEnchantmentValue() {
        return 1;
    }
}
