package net.minecraft.world.item;

import net.minecraft.world.item.Item;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/TieredItem.class */
public class TieredItem extends Item {
    private final Tier tier;

    public TieredItem(Tier tier, Item.Properties properties) {
        super(properties.defaultDurability(tier.getUses()));
        this.tier = tier;
    }

    public Tier getTier() {
        return this.tier;
    }

    @Override // net.minecraft.world.item.Item
    public int getEnchantmentValue() {
        return this.tier.getEnchantmentValue();
    }

    @Override // net.minecraft.world.item.Item
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
        return this.tier.getRepairIngredient().test(itemStack2) || super.isValidRepairItem(itemStack, itemStack2);
    }
}
