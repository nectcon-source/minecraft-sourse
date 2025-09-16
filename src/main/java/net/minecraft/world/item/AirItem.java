package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/AirItem.class */
public class AirItem extends Item {
    private final Block block;

    public AirItem(Block block, Item.Properties properties) {
        super(properties);
        this.block = block;
    }

    @Override // net.minecraft.world.item.Item
    public String getDescriptionId() {
        return this.block.getDescriptionId();
    }

    @Override // net.minecraft.world.item.Item
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        this.block.appendHoverText(itemStack, level, list, tooltipFlag);
    }
}
