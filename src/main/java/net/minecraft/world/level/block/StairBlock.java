package net.minecraft.world.level.block;

import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/StairBlock.class */
public class StairBlock extends Block implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape TOP_AABB = SlabBlock.TOP_AABB;
    protected static final VoxelShape BOTTOM_AABB = SlabBlock.BOTTOM_AABB;
    protected static final VoxelShape OCTET_NNN = Block.box(0.0d, 0.0d, 0.0d, 8.0d, 8.0d, 8.0d);
    protected static final VoxelShape OCTET_NNP = Block.box(0.0d, 0.0d, 8.0d, 8.0d, 8.0d, 16.0d);
    protected static final VoxelShape OCTET_NPN = Block.box(0.0d, 8.0d, 0.0d, 8.0d, 16.0d, 8.0d);
    protected static final VoxelShape OCTET_NPP = Block.box(0.0d, 8.0d, 8.0d, 8.0d, 16.0d, 16.0d);
    protected static final VoxelShape OCTET_PNN = Block.box(8.0d, 0.0d, 0.0d, 16.0d, 8.0d, 8.0d);
    protected static final VoxelShape OCTET_PNP = Block.box(8.0d, 0.0d, 8.0d, 16.0d, 8.0d, 16.0d);
    protected static final VoxelShape OCTET_PPN = Block.box(8.0d, 8.0d, 0.0d, 16.0d, 16.0d, 8.0d);
    protected static final VoxelShape OCTET_PPP = Block.box(8.0d, 8.0d, 8.0d, 16.0d, 16.0d, 16.0d);
    protected static final VoxelShape[] TOP_SHAPES = makeShapes(TOP_AABB, OCTET_NNN, OCTET_PNN, OCTET_NNP, OCTET_PNP);
    protected static final VoxelShape[] BOTTOM_SHAPES = makeShapes(BOTTOM_AABB, OCTET_NPN, OCTET_PPN, OCTET_NPP, OCTET_PPP);
    private static final int[] SHAPE_BY_STATE = {12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};
    private final Block base;
    private final BlockState baseState;

    private static VoxelShape[] makeShapes(VoxelShape voxelShape, VoxelShape voxelShape2, VoxelShape voxelShape3, VoxelShape voxelShape4, VoxelShape voxelShape5) {
        return (VoxelShape[]) IntStream.range(0, 16).mapToObj(i -> {
            return makeStairShape(i, voxelShape, voxelShape2, voxelShape3, voxelShape4, voxelShape5);
        }).toArray(i2 -> {
            return new VoxelShape[i2];
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static VoxelShape makeStairShape(int i, VoxelShape voxelShape, VoxelShape voxelShape2, VoxelShape voxelShape3, VoxelShape voxelShape4, VoxelShape voxelShape5) {
        VoxelShape voxelShape6 = voxelShape;
        if ((i & 1) != 0) {
            voxelShape6 = Shapes.or(voxelShape6, voxelShape2);
        }
        if ((i & 2) != 0) {
            voxelShape6 = Shapes.or(voxelShape6, voxelShape3);
        }
        if ((i & 4) != 0) {
            voxelShape6 = Shapes.or(voxelShape6, voxelShape4);
        }
        if ((i & 8) != 0) {
            voxelShape6 = Shapes.or(voxelShape6, voxelShape5);
        }
        return voxelShape6;
    }

    protected StairBlock(BlockState blockState, BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(HALF, Half.BOTTOM)).setValue(SHAPE, StairsShape.STRAIGHT)).setValue(WATERLOGGED, false));
        this.base = blockState.getBlock();
        this.baseState = blockState;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return (blockState.getValue(HALF) == Half.TOP ? TOP_SHAPES : BOTTOM_SHAPES)[SHAPE_BY_STATE[getShapeIndex(blockState)]];
    }

    private int getShapeIndex(BlockState blockState) {
        return (((StairsShape) blockState.getValue(SHAPE)).ordinal() * 4) + ((Direction) blockState.getValue(FACING)).get2DDataValue();
    }

    @Override // net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        this.base.animateTick(blockState, level, blockPos, random);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        this.baseState.attack(level, blockPos, player);
    }

    @Override // net.minecraft.world.level.block.Block
    public void destroy(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        this.base.destroy(levelAccessor, blockPos, blockState);
    }

    @Override // net.minecraft.world.level.block.Block
    public float getExplosionResistance() {
        return this.base.getExplosionResistance();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState.is(blockState.getBlock())) {
            return;
        }
        this.baseState.neighborChanged(level, blockPos, Blocks.AIR, blockPos, false);
        this.base.onPlace(this.baseState, level, blockPos, blockState2, false);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        this.baseState.onRemove(level, blockPos, blockState2, z);
    }

    @Override // net.minecraft.world.level.block.Block
    public void stepOn(Level level, BlockPos blockPos, Entity entity) {
        this.base.stepOn(level, blockPos, entity);
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean isRandomlyTicking(BlockState blockState) {
        return this.base.isRandomlyTicking(blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        this.base.randomTick(blockState, serverLevel, blockPos, random);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        this.base.tick(blockState, serverLevel, blockPos, random);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return this.baseState.use(level, player, interactionHand, blockHitResult);
    }

    @Override // net.minecraft.world.level.block.Block
    public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
        this.base.wasExploded(level, blockPos, explosion);
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction clickedFace = blockPlaceContext.getClickedFace();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        BlockState blockState = (BlockState) ((BlockState) ((BlockState) defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection())).setValue(HALF, (clickedFace == Direction.DOWN || (clickedFace != Direction.UP && blockPlaceContext.getClickLocation().y - ((double) clickedPos.getY()) > 0.5d)) ? Half.TOP : Half.BOTTOM)).setValue(WATERLOGGED, Boolean.valueOf(blockPlaceContext.getLevel().getFluidState(clickedPos).getType() == Fluids.WATER));
        return (BlockState) blockState.setValue(SHAPE, getStairsShape(blockState, blockPlaceContext.getLevel(), clickedPos));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (direction.getAxis().isHorizontal()) {
            return (BlockState) blockState.setValue(SHAPE, getStairsShape(blockState, levelAccessor, blockPos));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    private static StairsShape getStairsShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        Direction direction = (Direction) blockState.getValue(FACING);
        BlockState blockState2 = blockGetter.getBlockState(blockPos.relative(direction));
        if (isStairs(blockState2) && blockState.getValue(HALF) == blockState2.getValue(HALF)) {
            Direction direction2 = (Direction) blockState2.getValue(FACING);
            if (direction2.getAxis() != ((Direction) blockState.getValue(FACING)).getAxis() && canTakeShape(blockState, blockGetter, blockPos, direction2.getOpposite())) {
                if (direction2 == direction.getCounterClockWise()) {
                    return StairsShape.OUTER_LEFT;
                }
                return StairsShape.OUTER_RIGHT;
            }
        }
        BlockState blockState3 = blockGetter.getBlockState(blockPos.relative(direction.getOpposite()));
        if (isStairs(blockState3) && blockState.getValue(HALF) == blockState3.getValue(HALF)) {
            Direction direction3 = (Direction) blockState3.getValue(FACING);
            if (direction3.getAxis() != ((Direction) blockState.getValue(FACING)).getAxis() && canTakeShape(blockState, blockGetter, blockPos, direction3)) {
                if (direction3 == direction.getCounterClockWise()) {
                    return StairsShape.INNER_LEFT;
                }
                return StairsShape.INNER_RIGHT;
            }
        }
        return StairsShape.STRAIGHT;
    }

    private static boolean canTakeShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockState blockState2 = blockGetter.getBlockState(blockPos.relative(direction));
        return (isStairs(blockState2) && blockState2.getValue(FACING) == blockState.getValue(FACING) && blockState2.getValue(HALF) == blockState.getValue(HALF)) ? false : true;
    }

    public static boolean isStairs(BlockState blockState) {
        return blockState.getBlock() instanceof StairBlock;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState) blockState.setValue(FACING, rotation.rotate((Direction) blockState.getValue(FACING)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        Direction direction = (Direction) blockState.getValue(FACING);
        StairsShape stairsShape = (StairsShape) blockState.getValue(SHAPE);
        switch (mirror) {
            case LEFT_RIGHT:
                if (direction.getAxis() == Direction.Axis.Z) {
                    switch (stairsShape) {
                        case INNER_LEFT:
                            return (BlockState) blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                        case INNER_RIGHT:
                            return (BlockState) blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                        case OUTER_LEFT:
                            return (BlockState) blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                        case OUTER_RIGHT:
                            return (BlockState) blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                        default:
                            return blockState.rotate(Rotation.CLOCKWISE_180);
                    }
                }
                break;
            case FRONT_BACK:
                if (direction.getAxis() == Direction.Axis.X) {
                    switch (stairsShape) {
                        case INNER_LEFT:
                            return (BlockState) blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                        case INNER_RIGHT:
                            return (BlockState) blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                        case OUTER_LEFT:
                            return (BlockState) blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                        case OUTER_RIGHT:
                            return (BlockState) blockState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                        case STRAIGHT:
                            return blockState.rotate(Rotation.CLOCKWISE_180);
                    }
                }
                break;
        }
        return super.mirror(blockState, mirror);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, SHAPE, WATERLOGGED);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public FluidState getFluidState(BlockState blockState) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
