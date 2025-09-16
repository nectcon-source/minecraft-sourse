package net.minecraft.world.item;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/StandingAndWallBlockItem.class */
public class StandingAndWallBlockItem extends BlockItem {
    protected final Block wallBlock;

    public StandingAndWallBlockItem(Block block, Block block2, Item.Properties properties) {
        super(block, properties);
        this.wallBlock = block2;
    }

    @Override // net.minecraft.world.item.BlockItem
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
        BlockState stateForPlacement = this.wallBlock.getStateForPlacement(blockPlaceContext);
        BlockState blockState = null;
        LevelReader level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        Direction[] nearestLookingDirections = blockPlaceContext.getNearestLookingDirections();
        int length = nearestLookingDirections.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            Direction direction = nearestLookingDirections[i];
            if (direction != Direction.UP) {
                BlockState stateForPlacement2 = direction == Direction.DOWN ? getBlock().getStateForPlacement(blockPlaceContext) : stateForPlacement;
                if (stateForPlacement2 != null && stateForPlacement2.canSurvive(level, clickedPos)) {
                    blockState = stateForPlacement2;
                    break;
                }
            }
            i++;
        }
        if (blockState == null || !level.isUnobstructed(blockState, clickedPos, CollisionContext.empty())) {
            return null;
        }
        return blockState;
    }

    @Override // net.minecraft.world.item.BlockItem
    public void registerBlocks(Map<Block, Item> map, Item item) {
        super.registerBlocks(map, item);
        map.put(this.wallBlock, item);
    }
}
