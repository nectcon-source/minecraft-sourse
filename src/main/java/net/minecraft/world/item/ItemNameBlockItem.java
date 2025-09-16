package net.minecraft.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ItemNameBlockItem.class */
public class ItemNameBlockItem extends BlockItem {
    public ItemNameBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override // net.minecraft.world.item.BlockItem, net.minecraft.world.item.Item
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }
}
