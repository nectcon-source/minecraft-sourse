package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/GameMasterBlockItem.class */
public class GameMasterBlockItem extends BlockItem {
    public GameMasterBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override // net.minecraft.world.item.BlockItem
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
        Player player = blockPlaceContext.getPlayer();
        if (player == null || player.canUseGameMasterBlocks()) {
            return super.getPlacementState(blockPlaceContext);
        }
        return null;
    }
}
