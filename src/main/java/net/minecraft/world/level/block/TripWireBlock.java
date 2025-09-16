package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/TripWireBlock.class */
public class TripWireBlock extends Block {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    public static final BooleanProperty DISARMED = BlockStateProperties.DISARMED;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = CrossCollisionBlock.PROPERTY_BY_DIRECTION;
    protected static final VoxelShape AABB = Block.box(0.0d, 1.0d, 0.0d, 16.0d, 2.5d, 16.0d);
    protected static final VoxelShape NOT_ATTACHED_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 8.0d, 16.0d);
    private final TripWireHookBlock hook;

    public TripWireBlock(TripWireHookBlock tripWireHookBlock, BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(POWERED, false)).setValue(ATTACHED, false)).setValue(DISARMED, false)).setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false));
        this.hook = tripWireHookBlock;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return ((Boolean) blockState.getValue(ATTACHED)).booleanValue() ? AABB : NOT_ATTACHED_AABB;
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockGetter level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        return (BlockState) ((BlockState) ((BlockState) ((BlockState) defaultBlockState().setValue(NORTH, Boolean.valueOf(shouldConnectTo(level.getBlockState(clickedPos.north()), Direction.NORTH)))).setValue(EAST, Boolean.valueOf(shouldConnectTo(level.getBlockState(clickedPos.east()), Direction.EAST)))).setValue(SOUTH, Boolean.valueOf(shouldConnectTo(level.getBlockState(clickedPos.south()), Direction.SOUTH)))).setValue(WEST, Boolean.valueOf(shouldConnectTo(level.getBlockState(clickedPos.west()), Direction.WEST)));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction.getAxis().isHorizontal()) {
            return (BlockState) blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), Boolean.valueOf(shouldConnectTo(blockState2, direction)));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        updateSource(level, blockPos, blockState);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (z || blockState.is(blockState2.getBlock())) {
            return;
        }
        updateSource(level, blockPos, (BlockState) blockState.setValue(POWERED, true));
    }

    @Override // net.minecraft.world.level.block.Block
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide && !player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() == Items.SHEARS) {
            level.setBlock(blockPos, (BlockState) blockState.setValue(DISARMED, true), 4);
        }
        super.playerWillDestroy(level, blockPos, blockState, player);
    }

    private void updateSource(Level level, BlockPos blockPos, BlockState blockState) {
        for (Direction direction : new Direction[]{Direction.SOUTH, Direction.WEST}) {
            int i = 1;
            while (true) {
                if (i < 42) {
                    BlockPos relative = blockPos.relative(direction, i);
                    BlockState blockState2 = level.getBlockState(relative);
                    if (blockState2.is(this.hook)) {
                        if (blockState2.getValue(TripWireHookBlock.FACING) == direction.getOpposite()) {
                            this.hook.calculateState(level, relative, blockState2, false, true, i, blockState);
                        }
                    } else if (!blockState2.is(this)) {
                        break;
                    } else {
                        i++;
                    }
                }
            }
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (level.isClientSide || ((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            return;
        }
        checkPressed(level, blockPos);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!((Boolean) serverLevel.getBlockState(blockPos).getValue(POWERED)).booleanValue()) {
            return;
        }
        checkPressed(serverLevel, blockPos);
    }

    private void checkPressed(Level level, BlockPos blockPos) {
        BlockState blockState = level.getBlockState(blockPos);
        boolean booleanValue = ((Boolean) blockState.getValue(POWERED)).booleanValue();
        boolean z = false;
        List<? extends Entity> entities = level.getEntities(null, blockState.getShape(level, blockPos).bounds().move(blockPos));
        if (!entities.isEmpty()) {
            Iterator<? extends Entity> it = entities.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                } else if (!it.next().isIgnoringBlockTriggers()) {
                    z = true;
                    break;
                }
            }
        }
        if (z != booleanValue) {
            BlockState blockState2 = (BlockState) blockState.setValue(POWERED, Boolean.valueOf(z));
            level.setBlock(blockPos, blockState2, 3);
            updateSource(level, blockPos, blockState2);
        }
        if (z) {
            level.getBlockTicks().scheduleTick(new BlockPos(blockPos), this, 10);
        }
    }

    public boolean shouldConnectTo(BlockState blockState, Direction direction) {
        Block block = blockState.getBlock();
        return block == this.hook ? blockState.getValue(TripWireHookBlock.FACING) == direction.getOpposite() : block == this;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180:
                return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(EAST, blockState.getValue(WEST))).setValue(SOUTH, blockState.getValue(NORTH))).setValue(WEST, blockState.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH, blockState.getValue(EAST))).setValue(EAST, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(NORTH));
            case CLOCKWISE_90:
                return (BlockState) ((BlockState) ((BlockState) ((BlockState) blockState.setValue(NORTH, blockState.getValue(WEST))).setValue(EAST, blockState.getValue(NORTH))).setValue(SOUTH, blockState.getValue(EAST))).setValue(WEST, blockState.getValue(SOUTH));
            default:
                return blockState;
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT:
                return (BlockState) ((BlockState) blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(NORTH));
            case FRONT_BACK:
                return (BlockState) ((BlockState) blockState.setValue(EAST, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(EAST));
            default:
                return super.mirror(blockState, mirror);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH);
    }
}
