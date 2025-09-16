package net.minecraft.world.item;

import net.minecraft.world.item.Item;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/EnchantedGoldenAppleItem.class */
public class EnchantedGoldenAppleItem extends Item {
    public EnchantedGoldenAppleItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }
}
