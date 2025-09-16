package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/WallTorchBlock.class */
public class WallTorchBlock extends TorchBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(5.5d, 3.0d, 11.0d, 10.5d, 13.0d, 16.0d), Direction.SOUTH, Block.box(5.5d, 3.0d, 0.0d, 10.5d, 13.0d, 5.0d), Direction.WEST, Block.box(11.0d, 3.0d, 5.5d, 16.0d, 13.0d, 10.5d), Direction.EAST, Block.box(0.0d, 3.0d, 5.5d, 5.0d, 13.0d, 10.5d)));

    protected WallTorchBlock(BlockBehaviour.Properties properties, ParticleOptions particleOptions) {
        super(properties, particleOptions);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override // net.minecraft.world.level.block.Block
    public String getDescriptionId() {
        return asItem().getDescriptionId();
    }

    @Override // net.minecraft.world.level.block.TorchBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return getShape(blockState);
    }

    public static VoxelShape getShape(BlockState blockState) {
        return AABBS.get(blockState.getValue(FACING));
    }

    @Override // net.minecraft.world.level.block.TorchBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        Direction direction = (Direction) blockState.getValue(FACING);
        BlockPos relative = blockPos.relative(direction.getOpposite());
        return levelReader.getBlockState(relative).isFaceSturdy(levelReader, relative, direction);
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState defaultBlockState = defaultBlockState();
        LevelReader level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        for (Direction direction : blockPlaceContext.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                defaultBlockState = (BlockState) defaultBlockState.setValue(FACING, direction.getOpposite());
                if (defaultBlockState.canSurvive(level, clickedPos)) {
                    return defaultBlockState;
                }
            }
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.TorchBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return blockState;
    }

    @Override // net.minecraft.world.level.block.TorchBlock, net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        double x = blockPos.getX() + 0.5d;
        double y = blockPos.getY() + 0.7d;
        double z = blockPos.getZ() + 0.5d;
        Direction opposite = ((Direction) blockState.getValue(FACING)).getOpposite();
        level.addParticle(ParticleTypes.SMOKE, x + (0.27d * opposite.getStepX()), y + 0.22d, z + (0.27d * opposite.getStepZ()), 0.0d, 0.0d, 0.0d);
        level.addParticle(this.flameParticle, x + (0.27d * opposite.getStepX()), y + 0.22d, z + (0.27d * opposite.getStepZ()), 0.0d, 0.0d, 0.0d);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState) blockState.setValue(FACING, rotation.rotate((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
