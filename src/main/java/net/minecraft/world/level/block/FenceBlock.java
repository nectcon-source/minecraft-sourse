package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/FenceBlock.class */
public class FenceBlock extends CrossCollisionBlock {
    private final VoxelShape[] occlusionByIndex;

    public FenceBlock(BlockBehaviour.Properties properties) {
        super(2.0f, 2.0f, 16.0f, 16.0f, 24.0f, properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false)).setValue(WATERLOGGED, false));
        this.occlusionByIndex = makeShapes(2.0f, 1.0f, 16.0f, 6.0f, 15.0f);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return this.occlusionByIndex[getAABBIndex(blockState)];
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return getShape(blockState, blockGetter, blockPos, collisionContext);
    }

    @Override // net.minecraft.world.level.block.CrossCollisionBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }

    public boolean connectsTo(BlockState blockState, boolean z, Direction direction) {
        Block block = blockState.getBlock();
        return (!isExceptionForConnection(block) && z) || isSameFence(block) || ((block instanceof FenceGateBlock) && FenceGateBlock.connectsToDirection(blockState, direction));
    }

    private boolean isSameFence(Block block) {
        return block.is(BlockTags.FENCES) && block.is(BlockTags.WOODEN_FENCES) == defaultBlockState().is(BlockTags.WOODEN_FENCES);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            if (player.getItemInHand(interactionHand).getItem() == Items.LEAD) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return LeadItem.bindPlayerMobs(player, level, blockPos);
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockGetter level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        BlockPos north = clickedPos.north();
        BlockPos east = clickedPos.east();
        BlockPos south = clickedPos.south();
        BlockPos west = clickedPos.west();
        BlockState blockState = level.getBlockState(north);
        BlockState blockState2 = level.getBlockState(east);
        BlockState blockState3 = level.getBlockState(south);
        BlockState blockState4 = level.getBlockState(west);
        return (BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) super.getStateForPlacement(blockPlaceContext).setValue(NORTH, Boolean.valueOf(connectsTo(blockState, blockState.isFaceSturdy(level, north, Direction.SOUTH), Direction.SOUTH)))).setValue(EAST, Boolean.valueOf(connectsTo(blockState2, blockState2.isFaceSturdy(level, east, Direction.WEST), Direction.WEST)))).setValue(SOUTH, Boolean.valueOf(connectsTo(blockState3, blockState3.isFaceSturdy(level, south, Direction.NORTH), Direction.NORTH)))).setValue(WEST, Boolean.valueOf(connectsTo(blockState4, blockState4.isFaceSturdy(level, west, Direction.EAST), Direction.EAST)))).setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (direction.getAxis().getPlane() == Direction.Plane.HORIZONTAL) {
            return (BlockState) blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(connectsTo(blockState2, blockState2.isFaceSturdy(levelAccessor, blockPos2, direction.getOpposite()), direction.getOpposite())));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }
}
