package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/NoteBlock.class */
public class NoteBlock extends Block {
    public static final EnumProperty<NoteBlockInstrument> INSTRUMENT = BlockStateProperties.NOTEBLOCK_INSTRUMENT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty NOTE = BlockStateProperties.NOTE;

    public NoteBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(INSTRUMENT, NoteBlockInstrument.HARP)).setValue(NOTE, 0)).setValue(POWERED, false));
    }

    @Override // net.minecraft.world.level.block.Block
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState) defaultBlockState().setValue(INSTRUMENT, NoteBlockInstrument.byState(blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().below())));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.DOWN) {
            return (BlockState) blockState.setValue(INSTRUMENT, NoteBlockInstrument.byState(blockState2));
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        boolean hasNeighborSignal = level.hasNeighborSignal(blockPos);
        if (hasNeighborSignal != ((Boolean) blockState.getValue(POWERED)).booleanValue()) {
            if (hasNeighborSignal) {
                playNote(level, blockPos);
            }
            level.setBlock(blockPos, (BlockState) blockState.setValue(POWERED, Boolean.valueOf(hasNeighborSignal)), 3);
        }
    }

    private void playNote(Level level, BlockPos blockPos) {
        if (level.getBlockState(blockPos.above()).isAir()) {
            level.blockEvent(blockPos, this, 0, 0);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        level.setBlock(blockPos, blockState.cycle(NOTE), 3);
        playNote(level, blockPos);
        player.awardStat(Stats.TUNE_NOTEBLOCK);
        return InteractionResult.CONSUME;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        if (level.isClientSide) {
            return;
        }
        playNote(level, blockPos);
        player.awardStat(Stats.PLAY_NOTEBLOCK);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int i2) {
        int intValue = ((Integer) blockState.getValue(NOTE)).intValue();
        level.playSound((Player) null, blockPos, ((NoteBlockInstrument) blockState.getValue(INSTRUMENT)).getSoundEvent(), SoundSource.RECORDS, 3.0f, (float) Math.pow(2.0d, (intValue - 12) / 12.0d));
        level.addParticle(ParticleTypes.NOTE, blockPos.getX() + 0.5d, blockPos.getY() + 1.2d, blockPos.getZ() + 0.5d, intValue / 24.0d, 0.0d, 0.0d);
        return true;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INSTRUMENT, POWERED, NOTE);
    }
}
