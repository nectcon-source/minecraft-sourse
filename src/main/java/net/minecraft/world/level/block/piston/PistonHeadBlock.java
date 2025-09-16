package net.minecraft.world.level.block.piston;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/piston/PistonHeadBlock.class */
public class PistonHeadBlock extends DirectionalBlock {
    public static final EnumProperty<PistonType> TYPE = BlockStateProperties.PISTON_TYPE;
    public static final BooleanProperty SHORT = BlockStateProperties.SHORT;
    protected static final VoxelShape EAST_AABB = Block.box(12.0d, 0.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    protected static final VoxelShape WEST_AABB = Block.box(0.0d, 0.0d, 0.0d, 4.0d, 16.0d, 16.0d);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0d, 0.0d, 12.0d, 16.0d, 16.0d, 16.0d);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 16.0d, 4.0d);
    protected static final VoxelShape UP_AABB = Block.box(0.0d, 12.0d, 0.0d, 16.0d, 16.0d, 16.0d);
    protected static final VoxelShape DOWN_AABB = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 4.0d, 16.0d);
    protected static final VoxelShape UP_ARM_AABB = Block.box(6.0d, -4.0d, 6.0d, 10.0d, 12.0d, 10.0d);
    protected static final VoxelShape DOWN_ARM_AABB = Block.box(6.0d, 4.0d, 6.0d, 10.0d, 20.0d, 10.0d);
    protected static final VoxelShape SOUTH_ARM_AABB = Block.box(6.0d, 6.0d, -4.0d, 10.0d, 10.0d, 12.0d);
    protected static final VoxelShape NORTH_ARM_AABB = Block.box(6.0d, 6.0d, 4.0d, 10.0d, 10.0d, 20.0d);
    protected static final VoxelShape EAST_ARM_AABB = Block.box(-4.0d, 6.0d, 6.0d, 12.0d, 10.0d, 10.0d);
    protected static final VoxelShape WEST_ARM_AABB = Block.box(4.0d, 6.0d, 6.0d, 20.0d, 10.0d, 10.0d);
    protected static final VoxelShape SHORT_UP_ARM_AABB = Block.box(6.0d, 0.0d, 6.0d, 10.0d, 12.0d, 10.0d);
    protected static final VoxelShape SHORT_DOWN_ARM_AABB = Block.box(6.0d, 4.0d, 6.0d, 10.0d, 16.0d, 10.0d);
    protected static final VoxelShape SHORT_SOUTH_ARM_AABB = Block.box(6.0d, 6.0d, 0.0d, 10.0d, 10.0d, 12.0d);
    protected static final VoxelShape SHORT_NORTH_ARM_AABB = Block.box(6.0d, 6.0d, 4.0d, 10.0d, 10.0d, 16.0d);
    protected static final VoxelShape SHORT_EAST_ARM_AABB = Block.box(0.0d, 6.0d, 6.0d, 12.0d, 10.0d, 10.0d);
    protected static final VoxelShape SHORT_WEST_ARM_AABB = Block.box(4.0d, 6.0d, 6.0d, 16.0d, 10.0d, 10.0d);
    private static final VoxelShape[] SHAPES_SHORT = makeShapes(true);
    private static final VoxelShape[] SHAPES_LONG = makeShapes(false);

    private static VoxelShape[] makeShapes(boolean z) {
        return (VoxelShape[]) Arrays.stream(Direction.values()).map(direction -> {
            return calculateShape(direction, z);
        }).toArray(i -> {
            return new VoxelShape[i];
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static VoxelShape calculateShape(Direction direction, boolean z) {
        switch (direction) {
            case DOWN:
            default:
                return Shapes.or(DOWN_AABB, z ? SHORT_DOWN_ARM_AABB : DOWN_ARM_AABB);
            case UP:
                return Shapes.or(UP_AABB, z ? SHORT_UP_ARM_AABB : UP_ARM_AABB);
            case NORTH:
                return Shapes.or(NORTH_AABB, z ? SHORT_NORTH_ARM_AABB : NORTH_ARM_AABB);
            case SOUTH:
                return Shapes.or(SOUTH_AABB, z ? SHORT_SOUTH_ARM_AABB : SOUTH_ARM_AABB);
            case WEST:
                return Shapes.or(WEST_AABB, z ? SHORT_WEST_ARM_AABB : WEST_ARM_AABB);
            case EAST:
                return Shapes.or(EAST_AABB, z ? SHORT_EAST_ARM_AABB : EAST_ARM_AABB);
        }
    }

    public PistonHeadBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(TYPE, PistonType.DEFAULT)).setValue(SHORT, false));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return (((Boolean) blockState.getValue(SHORT)).booleanValue() ? SHAPES_SHORT : SHAPES_LONG)[((Direction) blockState.getValue(FACING)).ordinal()];
    }

    private boolean isFittingBase(BlockState blockState, BlockState blockState2) {
        return blockState2.is(blockState.getValue(TYPE) == PistonType.DEFAULT ? Blocks.PISTON : Blocks.STICKY_PISTON) && ((Boolean) blockState2.getValue(PistonBaseBlock.EXTENDED)).booleanValue() && blockState2.getValue(FACING) == blockState.getValue(FACING);
    }

    @Override // net.minecraft.world.level.block.Block
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide && player.abilities.instabuild) {
            BlockPos relative = blockPos.relative(((Direction) blockState.getValue(FACING)).getOpposite());
            if (isFittingBase(blockState, level.getBlockState(relative))) {
                level.destroyBlock(relative, false);
            }
        }
        super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        super.onRemove(blockState, level, blockPos, blockState2, z);
        BlockPos relative = blockPos.relative(((Direction) blockState.getValue(FACING)).getOpposite());
        if (isFittingBase(blockState, level.getBlockState(relative))) {
            level.destroyBlock(relative, true);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState2 = levelReader.getBlockState(blockPos.relative(((Direction) blockState.getValue(FACING)).getOpposite()));
        return isFittingBase(blockState, blockState2) || (blockState2.is(Blocks.MOVING_PISTON) && blockState2.getValue(FACING) == blockState.getValue(FACING));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        if (blockState.canSurvive(level, blockPos)) {
            BlockPos relative = blockPos.relative(((Direction) blockState.getValue(FACING)).getOpposite());
            level.getBlockState(relative).neighborChanged(level, relative, block, blockPos2, false);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return new ItemStack(blockState.getValue(TYPE) == PistonType.STICKY ? Blocks.STICKY_PISTON : Blocks.PISTON);
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
        builder.add(FACING, TYPE, SHORT);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
