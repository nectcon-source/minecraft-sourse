package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/RedstoneWallTorchBlock.class */
public class RedstoneWallTorchBlock extends RedstoneTorchBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    protected RedstoneWallTorchBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(LIT, true));
    }

    @Override // net.minecraft.world.level.block.Block
    public String getDescriptionId() {
        return asItem().getDescriptionId();
    }

    @Override // net.minecraft.world.level.block.TorchBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return WallTorchBlock.getShape(blockState);
    }

    @Override // net.minecraft.world.level.block.TorchBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return Blocks.WALL_TORCH.canSurvive(blockState, levelReader, blockPos);
    }

    @Override // net.minecraft.world.level.block.TorchBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        return Blocks.WALL_TORCH.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState stateForPlacement = Blocks.WALL_TORCH.getStateForPlacement(blockPlaceContext);
        if (stateForPlacement == null) {
            return null;
        }
        return (BlockState) defaultBlockState().setValue(FACING, stateForPlacement.getValue(FACING));
    }

    @Override // net.minecraft.world.level.block.RedstoneTorchBlock, net.minecraft.world.level.block.TorchBlock, net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (!((Boolean) blockState.getValue(LIT)).booleanValue()) {
            return;
        }
        Direction opposite = ((Direction) blockState.getValue(FACING)).getOpposite();
        level.addParticle(this.flameParticle, blockPos.getX() + 0.5d + ((random.nextDouble() - 0.5d) * 0.2d) + (0.27d * opposite.getStepX()), blockPos.getY() + 0.7d + ((random.nextDouble() - 0.5d) * 0.2d) + 0.22d, blockPos.getZ() + 0.5d + ((random.nextDouble() - 0.5d) * 0.2d) + (0.27d * opposite.getStepZ()), 0.0d, 0.0d, 0.0d);
    }

    @Override // net.minecraft.world.level.block.RedstoneTorchBlock
    protected boolean hasNeighborSignal(Level level, BlockPos blockPos, BlockState blockState) {
        Direction opposite = ((Direction) blockState.getValue(FACING)).getOpposite();
        return level.hasSignal(blockPos.relative(opposite), opposite);
    }

    @Override // net.minecraft.world.level.block.RedstoneTorchBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (((Boolean) blockState.getValue(LIT)).booleanValue() && blockState.getValue(FACING) != direction) {
            return 15;
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return Blocks.WALL_TORCH.rotate(blockState, rotation);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return Blocks.WALL_TORCH.mirror(blockState, mirror);
    }

    @Override // net.minecraft.world.level.block.RedstoneTorchBlock, net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }
}
