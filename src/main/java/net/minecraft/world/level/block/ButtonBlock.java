package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/ButtonBlock.class */
public abstract class ButtonBlock extends FaceAttachedHorizontalDirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    protected static final VoxelShape CEILING_AABB_X = Block.box(6.0d, 14.0d, 5.0d, 10.0d, 16.0d, 11.0d);
    protected static final VoxelShape CEILING_AABB_Z = Block.box(5.0d, 14.0d, 6.0d, 11.0d, 16.0d, 10.0d);
    protected static final VoxelShape FLOOR_AABB_X = Block.box(6.0d, 0.0d, 5.0d, 10.0d, 2.0d, 11.0d);
    protected static final VoxelShape FLOOR_AABB_Z = Block.box(5.0d, 0.0d, 6.0d, 11.0d, 2.0d, 10.0d);
    protected static final VoxelShape NORTH_AABB = Block.box(5.0d, 6.0d, 14.0d, 11.0d, 10.0d, 16.0d);
    protected static final VoxelShape SOUTH_AABB = Block.box(5.0d, 6.0d, 0.0d, 11.0d, 10.0d, 2.0d);
    protected static final VoxelShape WEST_AABB = Block.box(14.0d, 6.0d, 5.0d, 16.0d, 10.0d, 11.0d);
    protected static final VoxelShape EAST_AABB = Block.box(0.0d, 6.0d, 5.0d, 2.0d, 10.0d, 11.0d);
    protected static final VoxelShape PRESSED_CEILING_AABB_X = Block.box(6.0d, 15.0d, 5.0d, 10.0d, 16.0d, 11.0d);
    protected static final VoxelShape PRESSED_CEILING_AABB_Z = Block.box(5.0d, 15.0d, 6.0d, 11.0d, 16.0d, 10.0d);
    protected static final VoxelShape PRESSED_FLOOR_AABB_X = Block.box(6.0d, 0.0d, 5.0d, 10.0d, 1.0d, 11.0d);
    protected static final VoxelShape PRESSED_FLOOR_AABB_Z = Block.box(5.0d, 0.0d, 6.0d, 11.0d, 1.0d, 10.0d);
    protected static final VoxelShape PRESSED_NORTH_AABB = Block.box(5.0d, 6.0d, 15.0d, 11.0d, 10.0d, 16.0d);
    protected static final VoxelShape PRESSED_SOUTH_AABB = Block.box(5.0d, 6.0d, 0.0d, 11.0d, 10.0d, 1.0d);
    protected static final VoxelShape PRESSED_WEST_AABB = Block.box(15.0d, 6.0d, 5.0d, 16.0d, 10.0d, 11.0d);
    protected static final VoxelShape PRESSED_EAST_AABB = Block.box(0.0d, 6.0d, 5.0d, 1.0d, 10.0d, 11.0d);
    private final boolean sensitive;

    protected abstract SoundEvent getSound(boolean z);

    protected ButtonBlock(boolean z, BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(FACE, AttachFace.WALL));
        this.sensitive = z;
    }

    private int getPressDuration() {
        return this.sensitive ? 30 : 20;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        Direction direction = (Direction) blockState.getValue(FACING);
        boolean booleanValue = ((Boolean) blockState.getValue(POWERED)).booleanValue();
        switch ((AttachFace) blockState.getValue(FACE)) {
            case FLOOR:
                return direction.getAxis() == Direction.Axis.X ? booleanValue ? PRESSED_FLOOR_AABB_X : FLOOR_AABB_X : booleanValue ? PRESSED_FLOOR_AABB_Z : FLOOR_AABB_Z;
            case WALL:
                switch (direction) {
                    case EAST:
                        return booleanValue ? PRESSED_EAST_AABB : EAST_AABB;
                    case WEST:
                        return booleanValue ? PRESSED_WEST_AABB : WEST_AABB;
                    case SOUTH:
                        return booleanValue ? PRESSED_SOUTH_AABB : SOUTH_AABB;
                    case NORTH:
                    default:
                        return booleanValue ? PRESSED_NORTH_AABB : NORTH_AABB;
                }
            case CEILING:
            default:
                return direction.getAxis() == Direction.Axis.X ? booleanValue ? PRESSED_CEILING_AABB_X : CEILING_AABB_X : booleanValue ? PRESSED_CEILING_AABB_Z : CEILING_AABB_Z;
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            return InteractionResult.CONSUME;
        }
        press(blockState, level, blockPos);
        playSound(player, level, blockPos, true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public void press(BlockState blockState, Level level, BlockPos blockPos) {
        level.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, true), 3);
        updateNeighbours(blockState, level, blockPos);
        level.getBlockTicks().scheduleTick(blockPos, this, getPressDuration());
    }

    protected void playSound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos, boolean z) {
        levelAccessor.playSound(z ? player : null, blockPos, getSound(z), SoundSource.BLOCKS, 0.3f, z ? 0.6f : 0.5f);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (z || blockState.is(blockState2.getBlock())) {
            return;
        }
        if (((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            updateNeighbours(blockState, level, blockPos);
        }
        super.onRemove(blockState, level, blockPos, blockState2, z);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return ((Boolean) blockState.getValue(POWERED)).booleanValue() ? 15 : 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (((Boolean) blockState.getValue(POWERED)).booleanValue() && getConnectedDirection(blockState) == direction) {
            return 15;
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            return;
        }
        if (this.sensitive) {
            checkPressed(blockState, serverLevel, blockPos);
            return;
        }
        serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, false), 3);
        updateNeighbours(blockState, serverLevel, blockPos);
        playSound(null, serverLevel, blockPos, false);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (level.isClientSide || !this.sensitive || ((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            return;
        }
        checkPressed(blockState, level, blockPos);
    }

    private void checkPressed(BlockState blockState, Level level, BlockPos blockPos) {
        boolean z = !level.getEntitiesOfClass(AbstractArrow.class, blockState.getShape(level, blockPos).bounds().move(blockPos)).isEmpty();
        if (z != ((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            level.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, Boolean.valueOf(z)), 3);
            updateNeighbours(blockState, level, blockPos);
            playSound(null, level, blockPos, z);
        }
        if (z) {
            level.getBlockTicks().scheduleTick(new BlockPos(blockPos), this, getPressDuration());
        }
    }

    private void updateNeighbours(BlockState blockState, Level level, BlockPos blockPos) {
        level.updateNeighborsAt(blockPos, this);
        level.updateNeighborsAt(blockPos.relative(getConnectedDirection(blockState).getOpposite()), this);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, FACE);
    }
}
