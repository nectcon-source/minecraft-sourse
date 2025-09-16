package net.minecraft.world.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ItemFrameItem.class */
public class ItemFrameItem extends HangingEntityItem {
    public ItemFrameItem(Item.Properties properties) {
        super(EntityType.ITEM_FRAME, properties);
    }

    @Override // net.minecraft.world.item.HangingEntityItem
    protected boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos blockPos) {
        return !Level.isOutsideBuildHeight(blockPos) && player.mayUseItemAt(blockPos, direction, itemStack);
    }
}
