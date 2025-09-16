package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SoulFireBlock.class */
public class SoulFireBlock extends BaseFireBlock {
    public SoulFireBlock(BlockBehaviour.Properties properties) {
        super(properties, 2.0f);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (canSurvive(blockState, levelAccessor, blockPos)) {
            return defaultBlockState();
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return canSurviveOnBlock(levelReader.getBlockState(blockPos.below()).getBlock());
    }

    public static boolean canSurviveOnBlock(Block block) {
        return block.is(BlockTags.SOUL_FIRE_BASE_BLOCKS);
    }

    @Override // net.minecraft.world.level.block.BaseFireBlock
    protected boolean canBurn(BlockState blockState) {
        return true;
    }
}
