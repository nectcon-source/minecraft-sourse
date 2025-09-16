package net.minecraft.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/DoubleHighBlockItem.class */
public class DoubleHighBlockItem extends BlockItem {
    public DoubleHighBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override // net.minecraft.world.item.BlockItem
    protected boolean placeBlock(BlockPlaceContext blockPlaceContext, BlockState blockState) {
        blockPlaceContext.getLevel().setBlock(blockPlaceContext.getClickedPos().above(), Blocks.AIR.defaultBlockState(), 27);
        return super.placeBlock(blockPlaceContext, blockState);
    }
}
