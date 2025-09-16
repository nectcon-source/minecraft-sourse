package net.minecraft.world.level.block;

import com.google.common.base.MoreObjects;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/TripWireHookBlock.class */
public class TripWireHookBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    protected static final VoxelShape NORTH_AABB = Block.box(5.0d, 0.0d, 10.0d, 11.0d, 10.0d, 16.0d);
    protected static final VoxelShape SOUTH_AABB = Block.box(5.0d, 0.0d, 0.0d, 11.0d, 10.0d, 6.0d);
    protected static final VoxelShape WEST_AABB = Block.box(10.0d, 0.0d, 5.0d, 16.0d, 10.0d, 11.0d);
    protected static final VoxelShape EAST_AABB = Block.box(0.0d, 0.0d, 5.0d, 6.0d, 10.0d, 11.0d);

    public TripWireHookBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH)).setValue(POWERED, false)).setValue(ATTACHED, false));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch ((Direction) blockState.getValue(FACING)) {
            case EAST:
            default:
                return EAST_AABB;
            case WEST:
                return WEST_AABB;
            case SOUTH:
                return SOUTH_AABB;
            case NORTH:
                return NORTH_AABB;
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        Direction direction = (Direction) blockState.getValue(FACING);
        BlockPos relative = blockPos.relative(direction.getOpposite());
        return direction.getAxis().isHorizontal() && levelReader.getBlockState(relative).isFaceSturdy(levelReader, relative, direction);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction.getOpposite() == blockState.getValue(FACING) && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.Block
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = (BlockState) ((BlockState) defaultBlockState().setValue(POWERED, false)).setValue(ATTACHED, false);
        LevelReader level = blockPlaceContext.getLevel();
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        for (Direction direction : blockPlaceContext.getNearestLookingDirections()) {
            if (direction.getAxis().isHorizontal()) {
                blockState = (BlockState) blockState.setValue(FACING, direction.getOpposite());
                if (blockState.canSurvive(level, clickedPos)) {
                    return blockState;
                }
            }
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.Block
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        calculateState(level, blockPos, blockState, false, false, -1, null);
    }

    public void calculateState(Level level, BlockPos blockPos, BlockState blockState, boolean z, boolean z2, int i, @Nullable BlockState blockState2) {
        Direction direction = (Direction) blockState.getValue(FACING);
        boolean booleanValue = ((Boolean) blockState.getValue(ATTACHED)).booleanValue();
        boolean booleanValue2 = ((Boolean) blockState.getValue(POWERED)).booleanValue();
        boolean z3 = !z;
        boolean z4 = false;
        int i2 = 0;
        BlockState[] blockStateArr = new BlockState[42];
        int i3 = 1;
        while (true) {
            if (i3 >= 42) {
                break;
            }
            BlockState blockState3 = level.getBlockState(blockPos.relative(direction, i3));
            if (blockState3.is(Blocks.TRIPWIRE_HOOK)) {
                if (blockState3.getValue(FACING) == direction.getOpposite()) {
                    i2 = i3;
                }
            } else {
                if (blockState3.is(Blocks.TRIPWIRE) || i3 == i) {
                    if (i3 == i) {
                        blockState3 = (BlockState) MoreObjects.firstNonNull(blockState2, blockState3);
                    }
                    boolean z5 = !((Boolean) blockState3.getValue(TripWireBlock.DISARMED)).booleanValue();
                    z4 |= z5 && ((Boolean) blockState3.getValue(TripWireBlock.POWERED)).booleanValue();
                    blockStateArr[i3] = blockState3;
                    if (i3 == i) {
                        level.getBlockTicks().scheduleTick(blockPos, this, 10);
                        z3 &= z5;
                    }
                } else {
                    blockStateArr[i3] = null;
                    z3 = false;
                }
                i3++;
            }
        }
        boolean z6 = z3 & (i2 > 1);
        boolean z7 = z4 & z6;
        BlockState blockState4 = (BlockState) ((BlockState) defaultBlockState().setValue(ATTACHED, Boolean.valueOf(z6))).setValue(POWERED, Boolean.valueOf(z7));
        if (i2 > 0) {
            BlockPos relative = blockPos.relative(direction, i2);
            Direction opposite = direction.getOpposite();
            level.setBlock(relative, (BlockState) blockState4.setValue(FACING, opposite), 3);
            notifyNeighbors(level, relative, opposite);
            playSound(level, relative, z6, z7, booleanValue, booleanValue2);
        }
        playSound(level, blockPos, z6, z7, booleanValue, booleanValue2);
        if (!z) {
            level.setBlock(blockPos, (BlockState) blockState4.setValue(FACING, direction), 3);
            if (z2) {
                notifyNeighbors(level, blockPos, direction);
            }
        }
        if (booleanValue != z6) {
            for (int i4 = 1; i4 < i2; i4++) {
                BlockPos relative2 = blockPos.relative(direction, i4);
                BlockState blockState5 = blockStateArr[i4];
                if (blockState5 != null) {
                    level.setBlock(relative2, (BlockState) blockState5.setValue(ATTACHED, Boolean.valueOf(z6)), 3);
                    if (!level.getBlockState(relative2).isAir()) {
                    }
                }
            }
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        calculateState(serverLevel, blockPos, blockState, false, true, -1, null);
    }

    private void playSound(Level level, BlockPos blockPos, boolean z, boolean z2, boolean z3, boolean z4) {
        if (z2 && !z4) {
            level.playSound((Player) null, blockPos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.4f, 0.6f);
            return;
        }
        if (!z2 && z4) {
            level.playSound((Player) null, blockPos, SoundEvents.TRIPWIRE_CLICK_OFF, SoundSource.BLOCKS, 0.4f, 0.5f);
            return;
        }
        if (z && !z3) {
            level.playSound((Player) null, blockPos, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 0.4f, 0.7f);
        } else if (!z && z3) {
            level.playSound((Player) null, blockPos, SoundEvents.TRIPWIRE_DETACH, SoundSource.BLOCKS, 0.4f, 1.2f / ((level.random.nextFloat() * 0.2f) + 0.9f));
        }
    }

    private void notifyNeighbors(Level level, BlockPos blockPos, Direction direction) {
        level.updateNeighborsAt(blockPos, this);
        level.updateNeighborsAt(blockPos.relative(direction.getOpposite()), this);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (z || blockState.is(blockState2.getBlock())) {
            return;
        }
        boolean booleanValue = ((Boolean) blockState.getValue(ATTACHED)).booleanValue();
        boolean booleanValue2 = ((Boolean) blockState.getValue(POWERED)).booleanValue();
        if (booleanValue || booleanValue2) {
            calculateState(level, blockPos, blockState, true, false, -1, null);
        }
        if (booleanValue2) {
            level.updateNeighborsAt(blockPos, this);
            level.updateNeighborsAt(blockPos.relative(((Direction) blockState.getValue(FACING)).getOpposite()), this);
        }
        super.onRemove(blockState, level, blockPos, blockState2, z);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return ((Boolean) blockState.getValue(POWERED)).booleanValue() ? 15 : 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (((Boolean) blockState.getValue(POWERED)).booleanValue() && blockState.getValue(FACING) == direction) {
            return 15;
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isSignalSource(BlockState blockState) {
        return true;
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
        builder.add(FACING, POWERED, ATTACHED);
    }
}
