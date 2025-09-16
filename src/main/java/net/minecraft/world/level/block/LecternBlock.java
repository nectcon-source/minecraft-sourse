package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/LecternBlock.class */
public class LecternBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;
    public static final VoxelShape SHAPE_BASE = Block.box(0.0d, 0.0d, 0.0d, 16.0d, 2.0d, 16.0d);
    public static final VoxelShape SHAPE_POST = Block.box(4.0d, 2.0d, 4.0d, 12.0d, 14.0d, 12.0d);
    public static final VoxelShape SHAPE_COMMON = Shapes.or(SHAPE_BASE, SHAPE_POST);
    public static final VoxelShape SHAPE_TOP_PLATE = Block.box(0.0d, 15.0d, 0.0d, 16.0d, 15.0d, 16.0d);
    public static final VoxelShape SHAPE_COLLISION = Shapes.or(SHAPE_COMMON, SHAPE_TOP_PLATE);
    public static final VoxelShape SHAPE_WEST = Shapes.or(Block.box(1.0d, 10.0d, 0.0d, 5.333333d, 14.0d, 16.0d), Block.box(5.333333d, 12.0d, 0.0d, 9.666667d, 16.0d, 16.0d), Block.box(9.666667d, 14.0d, 0.0d, 14.0d, 18.0d, 16.0d), SHAPE_COMMON);
    public static final VoxelShape SHAPE_NORTH = Shapes.or(Block.box(0.0d, 10.0d, 1.0d, 16.0d, 14.0d, 5.333333d), Block.box(0.0d, 12.0d, 5.333333d, 16.0d, 16.0d, 9.666667d), Block.box(0.0d, 14.0d, 9.666667d, 16.0d, 18.0d, 14.0d), SHAPE_COMMON);
    public static final VoxelShape SHAPE_EAST = Shapes.or(Block.box(15.0d, 10.0d, 0.0d, 10.666667d, 14.0d, 16.0d), Block.box(10.666667d, 12.0d, 0.0d, 6.333333d, 16.0d, 16.0d), Block.box(6.333333d, 14.0d, 0.0d, 2.0d, 18.0d, 16.0d), SHAPE_COMMON);
    public static final VoxelShape SHAPE_SOUTH = Shapes.or(Block.box(0.0d, 10.0d, 15.0d, 16.0d, 14.0d, 10.666667d), Block.box(0.0d, 12.0d, 10.666667d, 16.0d, 16.0d, 6.333333d), Block.box(0.0d, 14.0d, 6.333333d, 16.0d, 18.0d, 2.0d), SHAPE_COMMON);

    protected LecternBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(POWERED, false)).setValue(HAS_BOOK, false));
    }

    @Override // net.minecraft.world.level.block.BaseEntityBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return SHAPE_COMMON;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level level = blockPlaceContext.getLevel();
        CompoundTag tag = blockPlaceContext.getItemInHand().getTag();
        Player player = blockPlaceContext.getPlayer();
        boolean z = false;
        if (!level.isClientSide && player != null && tag != null && player.canUseGameMasterBlocks() && tag.contains("BlockEntityTag") && tag.getCompound("BlockEntityTag").contains("Book")) {
            z = true;
        }
        return (BlockState) ((BlockState) defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite())).setValue(HAS_BOOK, Boolean.valueOf(z));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_COLLISION;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch ((Direction) blockState.getValue(FACING)) {
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            default:
                return SHAPE_COMMON;
        }
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
        builder.add(FACING, POWERED, HAS_BOOK);
    }

    @Override // net.minecraft.world.level.block.EntityBlock
    @Nullable
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new LecternBlockEntity();
    }

    public static boolean tryPlaceBook(Level level, BlockPos blockPos, BlockState blockState, ItemStack itemStack) {
        if (!((Boolean) blockState.getValue(HAS_BOOK)).booleanValue()) {
            if (!level.isClientSide) {
                placeBook(level, blockPos, blockState, itemStack);
                return true;
            }
            return true;
        }
        return false;
    }

    private static void placeBook(Level level, BlockPos blockPos, BlockState blockState, ItemStack itemStack) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof LecternBlockEntity) {
            ((LecternBlockEntity) blockEntity).setBook(itemStack.split(1));
            resetBookState(level, blockPos, blockState, true);
            level.playSound((Player) null, blockPos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public static void resetBookState(Level level, BlockPos blockPos, BlockState blockState, boolean z) {
        level.setBlock(blockPos, (BlockState) ((BlockState) blockState.setValue(POWERED, false)).setValue(HAS_BOOK, Boolean.valueOf(z)), 3);
        updateBelow(level, blockPos, blockState);
    }

    public static void signalPageChange(Level level, BlockPos blockPos, BlockState blockState) {
        changePowered(level, blockPos, blockState, true);
        level.getBlockTicks().scheduleTick(blockPos, blockState.getBlock(), 2);
        level.levelEvent(1043, blockPos, 0);
    }

    private static void changePowered(Level level, BlockPos blockPos, BlockState blockState, boolean z) {
        level.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, Boolean.valueOf(z)), 3);
        updateBelow(level, blockPos, blockState);
    }

    private static void updateBelow(Level level, BlockPos blockPos, BlockState blockState) {
        level.updateNeighborsAt(blockPos.below(), blockState.getBlock());
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        changePowered(serverLevel, blockPos, blockState, false);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        if (((Boolean) blockState.getValue(HAS_BOOK)).booleanValue()) {
            popBook(blockState, level, blockPos);
        }
        if (((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            level.updateNeighborsAt(blockPos.below(), this);
        }
        super.onRemove(blockState, level, blockPos, blockState2, z);
    }

    private void popBook(BlockState blockState, Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof LecternBlockEntity) {
            LecternBlockEntity lecternBlockEntity = (LecternBlockEntity) blockEntity;
            Direction direction = (Direction) blockState.getValue(FACING);
            ItemEntity itemEntity = new ItemEntity(level, blockPos.getX() + 0.5d + (0.25f * direction.getStepX()), blockPos.getY() + 1, blockPos.getZ() + 0.5d + (0.25f * direction.getStepZ()), lecternBlockEntity.getBook().copy());
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
            lecternBlockEntity.clearContent();
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return ((Boolean) blockState.getValue(POWERED)).booleanValue() ? 15 : 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return (direction == Direction.UP && ((Boolean) blockState.getValue(POWERED)).booleanValue()) ? 15 : 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        if (((Boolean) blockState.getValue(HAS_BOOK)).booleanValue()) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof LecternBlockEntity) {
                return ((LecternBlockEntity) blockEntity).getRedstoneSignal();
            }
            return 0;
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (((Boolean) blockState.getValue(HAS_BOOK)).booleanValue()) {
            if (!level.isClientSide) {
                openScreen(level, blockPos, player);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (itemInHand.isEmpty() || itemInHand.getItem().is(ItemTags.LECTERN_BOOKS)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.CONSUME;
    }

    @Override // net.minecraft.world.level.block.BaseEntityBlock, net.minecraft.world.level.block.state.BlockBehaviour
    @Nullable
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        if (!((Boolean) blockState.getValue(HAS_BOOK)).booleanValue()) {
            return null;
        }
        return super.getMenuProvider(blockState, level, blockPos);
    }

    private void openScreen(Level level, BlockPos blockPos, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof LecternBlockEntity) {
            player.openMenu((LecternBlockEntity) blockEntity);
            player.awardStat(Stats.INTERACT_WITH_LECTERN);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
