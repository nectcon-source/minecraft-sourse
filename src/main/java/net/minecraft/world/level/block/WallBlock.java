package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/WallBlock.class */
public class WallBlock extends Block implements SimpleWaterloggedBlock {
    private final Map<BlockState, VoxelShape> shapeByIndex;
    private final Map<BlockState, VoxelShape> collisionShapeByIndex;

    /* renamed from: UP */
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final EnumProperty<WallSide> EAST_WALL = BlockStateProperties.EAST_WALL;
    public static final EnumProperty<WallSide> NORTH_WALL = BlockStateProperties.NORTH_WALL;
    public static final EnumProperty<WallSide> SOUTH_WALL = BlockStateProperties.SOUTH_WALL;
    public static final EnumProperty<WallSide> WEST_WALL = BlockStateProperties.WEST_WALL;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape POST_TEST = Block.box(7.0d, 0.0d, 7.0d, 9.0d, 16.0d, 9.0d);
    private static final VoxelShape NORTH_TEST = Block.box(7.0d, 0.0d, 0.0d, 9.0d, 16.0d, 9.0d);
    private static final VoxelShape SOUTH_TEST = Block.box(7.0d, 0.0d, 7.0d, 9.0d, 16.0d, 16.0d);
    private static final VoxelShape WEST_TEST = Block.box(0.0d, 0.0d, 7.0d, 9.0d, 16.0d, 9.0d);
    private static final VoxelShape EAST_TEST = Block.box(7.0d, 0.0d, 7.0d, 16.0d, 16.0d, 9.0d);

    public WallBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(UP, true)).setValue(NORTH_WALL, WallSide.NONE)).setValue(EAST_WALL, WallSide.NONE)).setValue(SOUTH_WALL, WallSide.NONE)).setValue(WEST_WALL, WallSide.NONE)).setValue(WATERLOGGED, false));
        this.shapeByIndex = makeShapes(4.0f, 3.0f, 16.0f, 0.0f, 14.0f, 16.0f);
        this.collisionShapeByIndex = makeShapes(4.0f, 3.0f, 24.0f, 0.0f, 24.0f, 24.0f);
    }

    private static VoxelShape applyWallShape(VoxelShape voxelShape, WallSide wallSide, VoxelShape voxelShape2, VoxelShape voxelShape3) {
        if (wallSide == WallSide.TALL) {
            return Shapes.or(voxelShape, voxelShape3);
        }
        if (wallSide == WallSide.LOW) {
            return Shapes.or(voxelShape, voxelShape2);
        }
        return voxelShape;
    }

    private Map<BlockState, VoxelShape> makeShapes(float f, float f2, float f3, float f4, float f5, float f6) {
        float f7 = 8.0f - f;
        float f8 = 8.0f + f;
        float f9 = 8.0f - f2;
        float f10 = 8.0f + f2;
        VoxelShape box = Block.box(f7, 0.0d, f7, f8, f3, f8);
        VoxelShape box2 = Block.box(f9, f4, 0.0d, f10, f5, f10);
        VoxelShape box3 = Block.box(f9, f4, f9, f10, f5, 16.0d);
        VoxelShape box4 = Block.box(0.0d, f4, f9, f10, f5, f10);
        VoxelShape box5 = Block.box(f9, f4, f9, 16.0d, f5, f10);
        VoxelShape box6 = Block.box(f9, f4, 0.0d, f10, f6, f10);
        VoxelShape box7 = Block.box(f9, f4, f9, f10, f6, 16.0d);
        VoxelShape box8 = Block.box(0.0d, f4, f9, f10, f6, f10);
        VoxelShape box9 = Block.box(f9, f4, f9, 16.0d, f6, f10);
        ImmutableMap.Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();
        for (Boolean bool : UP.getPossibleValues()) {
            for (WallSide wallSide : EAST_WALL.getPossibleValues()) {
                for (WallSide wallSide2 : NORTH_WALL.getPossibleValues()) {
                    for (WallSide wallSide3 : WEST_WALL.getPossibleValues()) {
                        for (WallSide wallSide4 : SOUTH_WALL.getPossibleValues()) {
                            VoxelShape applyWallShape = applyWallShape(applyWallShape(applyWallShape(applyWallShape(Shapes.empty(), wallSide, box5, box9), wallSide3, box4, box8), wallSide2, box2, box6), wallSide4, box3, box7);
                            if (bool.booleanValue()) {
                                applyWallShape = Shapes.or(applyWallShape, box);
                            }
                            BlockState blockState = (BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) defaultBlockState().setValue(UP, bool)).setValue(EAST_WALL, wallSide)).setValue(WEST_WALL, wallSide3)).setValue(NORTH_WALL, wallSide2)).setValue(SOUTH_WALL, wallSide4);
                            builder.put(blockState.setValue(WATERLOGGED, false), applyWallShape);
                            builder.put(blockState.setValue(WATERLOGGED, true), applyWallShape);
                        }
                    }
                }
            }
        }
        return builder.build();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shapeByIndex.get(blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.collisionShapeByIndex.get(blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }

    private boolean connectsTo(BlockState blockState, boolean z, Direction direction) {
        Block block = blockState.getBlock();
        return blockState.is(BlockTags.WALLS) || (!isExceptionForConnection(block) && z) || (block instanceof IronBarsBlock) || ((block instanceof FenceGateBlock) && FenceGateBlock.connectsToDirection(blockState, direction));
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        LevelReader level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        BlockPos north = clickedPos.north();
        BlockPos east = clickedPos.east();
        BlockPos south = clickedPos.south();
        BlockPos west = clickedPos.west();
        BlockPos above = clickedPos.above();
        BlockState blockState = level.getBlockState(north);
        BlockState blockState2 = level.getBlockState(east);
        BlockState blockState3 = level.getBlockState(south);
        BlockState blockState4 = level.getBlockState(west);
        return updateShape(level, (BlockState) defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidState.getType() == Fluids.WATER)), above, level.getBlockState(above), connectsTo(blockState, blockState.isFaceSturdy(level, north, Direction.SOUTH), Direction.SOUTH), connectsTo(blockState2, blockState2.isFaceSturdy(level, east, Direction.WEST), Direction.WEST), connectsTo(blockState3, blockState3.isFaceSturdy(level, south, Direction.NORTH), Direction.NORTH), connectsTo(blockState4, blockState4.isFaceSturdy(level, west, Direction.EAST), Direction.EAST));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            levelAccessor.getLiquidTicks().scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        if (direction == Direction.DOWN) {
            return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
        }
        if (direction == Direction.UP) {
            return topUpdate(levelAccessor, blockState, blockPos2, blockState2);
        }
        return sideUpdate(levelAccessor, blockPos, blockState, blockPos2, blockState2, direction);
    }

    private static boolean isConnected(BlockState blockState, Property<WallSide> property) {
        return blockState.getValue(property) != WallSide.NONE;
    }

    private static boolean isCovered(VoxelShape voxelShape, VoxelShape voxelShape2) {
        return !Shapes.joinIsNotEmpty(voxelShape2, voxelShape, BooleanOp.ONLY_FIRST);
    }

    private BlockState topUpdate(LevelReader levelReader, BlockState blockState, BlockPos blockPos, BlockState blockState2) {
        return updateShape(levelReader, blockState, blockPos, blockState2, isConnected(blockState, NORTH_WALL), isConnected(blockState, EAST_WALL), isConnected(blockState, SOUTH_WALL), isConnected(blockState, WEST_WALL));
    }

    private BlockState sideUpdate(LevelReader levelReader, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2, Direction direction) {
        Direction opposite = direction.getOpposite();
        boolean connectsTo = direction == Direction.NORTH ? connectsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos2, opposite), opposite) : isConnected(blockState, NORTH_WALL);
        boolean connectsTo2 = direction == Direction.EAST ? connectsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos2, opposite), opposite) : isConnected(blockState, EAST_WALL);
        boolean connectsTo3 = direction == Direction.SOUTH ? connectsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos2, opposite), opposite) : isConnected(blockState, SOUTH_WALL);
        boolean connectsTo4 = direction == Direction.WEST ? connectsTo(blockState2, blockState2.isFaceSturdy(levelReader, blockPos2, opposite), opposite) : isConnected(blockState, WEST_WALL);
        BlockPos above = blockPos.above();
        return updateShape(levelReader, blockState, above, levelReader.getBlockState(above), connectsTo, connectsTo2, connectsTo3, connectsTo4);
    }

    private BlockState updateShape(LevelReader levelReader, BlockState blockState, BlockPos blockPos, BlockState blockState2, boolean z, boolean z2, boolean z3, boolean z4) {
        VoxelShape faceShape = blockState2.getCollisionShape(levelReader, blockPos).getFaceShape(Direction.DOWN);
        BlockState updateSides = updateSides(blockState, z, z2, z3, z4, faceShape);
        return (BlockState) updateSides.setValue(UP, Boolean.valueOf(shouldRaisePost(updateSides, blockState2, faceShape)));
    }

    private boolean shouldRaisePost(BlockState blockState, BlockState blockState2, VoxelShape voxelShape) {
        if ((blockState2.getBlock() instanceof WallBlock) && ((Boolean) blockState2.getValue(UP)).booleanValue()) {
            return true;
        }
        WallSide wallSide = (WallSide) blockState.getValue(NORTH_WALL);
        WallSide wallSide2 = (WallSide) blockState.getValue(SOUTH_WALL);
        WallSide wallSide3 = (WallSide) blockState.getValue(EAST_WALL);
        WallSide wallSide4 = (WallSide) blockState.getValue(WEST_WALL);
        boolean z = wallSide2 == WallSide.NONE;
        boolean z2 = wallSide4 == WallSide.NONE;
        boolean z3 = wallSide3 == WallSide.NONE;
        boolean z4 = wallSide == WallSide.NONE;
        if (((!z4 || !z || !z2 || !z3) && z4 == z && z2 == z3) ? false : true) {
            return true;
        }
        if ((wallSide == WallSide.TALL && wallSide2 == WallSide.TALL) || (wallSide3 == WallSide.TALL && wallSide4 == WallSide.TALL)) {
            return false;
        }
        return blockState2.getBlock().is(BlockTags.WALL_POST_OVERRIDE) || isCovered(voxelShape, POST_TEST);
    }

    private BlockState updateSides(BlockState blockState, boolean z, boolean z2, boolean z3, boolean z4, VoxelShape voxelShape) {
        return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH_WALL, makeWallState(z, voxelShape, NORTH_TEST))).setValue(EAST_WALL, makeWallState(z2, voxelShape, EAST_TEST))).setValue(SOUTH_WALL, makeWallState(z3, voxelShape, SOUTH_TEST))).setValue(WEST_WALL, makeWallState(z4, voxelShape, WEST_TEST));
    }

    private WallSide makeWallState(boolean z, VoxelShape voxelShape, VoxelShape voxelShape2) {
        if (z) {
            if (isCovered(voxelShape, voxelShape2)) {
                return WallSide.TALL;
            }
            return WallSide.LOW;
        }
        return WallSide.NONE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public FluidState getFluidState(BlockState blockState) {
        if (((Boolean) blockState.getValue(WATERLOGGED)).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return !((Boolean) blockState.getValue(WATERLOGGED)).booleanValue();
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, NORTH_WALL, EAST_WALL, WEST_WALL, SOUTH_WALL, WATERLOGGED);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180:
                return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH_WALL, blockState.getValue(SOUTH_WALL))).setValue(EAST_WALL, blockState.getValue(WEST_WALL))).setValue(SOUTH_WALL, blockState.getValue(NORTH_WALL))).setValue(WEST_WALL, blockState.getValue(EAST_WALL));
            case COUNTERCLOCKWISE_90:
                return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH_WALL, blockState.getValue(EAST_WALL))).setValue(EAST_WALL, blockState.getValue(SOUTH_WALL))).setValue(SOUTH_WALL, blockState.getValue(WEST_WALL))).setValue(WEST_WALL, blockState.getValue(NORTH_WALL));
            case CLOCKWISE_90:
                return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH_WALL, blockState.getValue(WEST_WALL))).setValue(EAST_WALL, blockState.getValue(NORTH_WALL))).setValue(SOUTH_WALL, blockState.getValue(EAST_WALL))).setValue(WEST_WALL, blockState.getValue(SOUTH_WALL));
            default:
                return blockState;
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT:
                return (BlockState) ((BlockState) blockState.setValue(NORTH_WALL, blockState.getValue(SOUTH_WALL))).setValue(SOUTH_WALL, blockState.getValue(NORTH_WALL));
            case FRONT_BACK:
                return (BlockState) ((BlockState) blockState.setValue(EAST_WALL, blockState.getValue(WEST_WALL))).setValue(WEST_WALL, blockState.getValue(EAST_WALL));
            default:
                return super.mirror(blockState, mirror);
        }
    }
}
