package net.minecraft.world.level.block;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/GlazedTerracottaBlock.class */
public class GlazedTerracottaBlock extends HorizontalDirectionalBlock {
    public GlazedTerracottaBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState) defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.PUSH_ONLY;
    }
}
