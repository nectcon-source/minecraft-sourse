package net.minecraft.world.item;

import net.minecraft.world.item.Item;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/BookItem.class */
public class BookItem extends Item {
    public BookItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public boolean isEnchantable(ItemStack itemStack) {
        return itemStack.getCount() == 1;
    }

    @Override // net.minecraft.world.item.Item
    public int getEnchantmentValue() {
        return 1;
    }
}
