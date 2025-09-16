package net.minecraft.world.level.block;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/HugeMushroomBlock.class */
public class HugeMushroomBlock extends Block {
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;

    /* renamed from: UP */
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;

    public HugeMushroomBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(NORTH, true)).setValue(EAST, true)).setValue(SOUTH, true)).setValue(WEST, true)).setValue(UP, true)).setValue(DOWN, true));
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockGetter level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        return (BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) defaultBlockState().setValue(DOWN, Boolean.valueOf(this != level.getBlockState(clickedPos.below()).getBlock()))).setValue(UP, Boolean.valueOf(this != level.getBlockState(clickedPos.above()).getBlock()))).setValue(NORTH, Boolean.valueOf(this != level.getBlockState(clickedPos.north()).getBlock()))).setValue(EAST, Boolean.valueOf(this != level.getBlockState(clickedPos.east()).getBlock()))).setValue(SOUTH, Boolean.valueOf(this != level.getBlockState(clickedPos.south()).getBlock()))).setValue(WEST, Boolean.valueOf(this != level.getBlockState(clickedPos.west()).getBlock()));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (blockState2.is(this)) {
            return (BlockState) blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), false);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.NORTH)), blockState.getValue(NORTH))).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.SOUTH)), blockState.getValue(SOUTH))).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.EAST)), blockState.getValue(EAST))).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.WEST)), blockState.getValue(WEST))).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.UP)), blockState.getValue(UP))).setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.DOWN)), blockState.getValue(DOWN));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return (BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.NORTH)), blockState.getValue(NORTH))).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.SOUTH)), blockState.getValue(SOUTH))).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.EAST)), blockState.getValue(EAST))).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.WEST)), blockState.getValue(WEST))).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.UP)), blockState.getValue(UP))).setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.DOWN)), blockState.getValue(DOWN));
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
    }
}
