package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/CoralBlock.class */
public class CoralBlock extends Block {
    private final Block deadBlock;

    public CoralBlock(Block block, BlockBehaviour.Properties properties) {
        super(properties);
        this.deadBlock = block;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!scanForWater(serverLevel, blockPos)) {
            serverLevel.setBlock(blockPos, this.deadBlock.defaultBlockState(), 2);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!scanForWater(levelAccessor, blockPos)) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 60 + levelAccessor.getRandom().nextInt(40));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    protected boolean scanForWater(BlockGetter blockGetter, BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (blockGetter.getFluidState(blockPos.relative(direction)).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        if (!scanForWater(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
            blockPlaceContext.getLevel().getBlockTicks().scheduleTick(blockPlaceContext.getClickedPos(), this, 60 + blockPlaceContext.getLevel().getRandom().nextInt(40));
        }
        return defaultBlockState();
    }
}
