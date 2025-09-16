package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/RedstoneTorchBlock.class */
public class RedstoneTorchBlock extends TorchBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final Map<BlockGetter, List<Toggle>> RECENT_TOGGLES = new WeakHashMap();

    protected RedstoneTorchBlock(BlockBehaviour.Properties properties) {
        super(properties, DustParticleOptions.REDSTONE);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(LIT, true));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (z) {
            return;
        }
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (((Boolean) blockState.getValue(LIT)).booleanValue() && Direction.UP != direction) {
            return 15;
        }
        return 0;
    }

    protected boolean hasNeighborSignal(Level level, BlockPos blockPos, BlockState blockState) {
        return level.hasSignal(blockPos.below(), Direction.DOWN);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        boolean hasNeighborSignal = hasNeighborSignal(serverLevel, blockPos, blockState);
        List<Toggle> list = RECENT_TOGGLES.get(serverLevel);
        while (list != null && !list.isEmpty() && serverLevel.getGameTime() - list.get(0).when > 60) {
            list.remove(0);
        }
        if (((Boolean) blockState.getValue(LIT)).booleanValue()) {
            if (hasNeighborSignal) {
                serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(LIT, false), 3);
                if (isToggledTooFrequently(serverLevel, blockPos, true)) {
                    serverLevel.levelEvent(1502, blockPos, 0);
                    serverLevel.getBlockTicks().scheduleTick(blockPos, serverLevel.getBlockState(blockPos).getBlock(), 160);
                    return;
                }
                return;
            }
            return;
        }
        if (!hasNeighborSignal && !isToggledTooFrequently(serverLevel, blockPos, false)) {
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(LIT, true), 3);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        if (((Boolean) blockState.getValue(LIT)).booleanValue() == hasNeighborSignal(level, blockPos, blockState) && !level.getBlockTicks().willTickThisTick(blockPos, this)) {
            level.getBlockTicks().scheduleTick(blockPos, this, 2);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (direction == Direction.DOWN) {
            return blockState.getSignal(blockGetter, blockPos, direction);
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.TorchBlock, net.minecraft.world.level.block.Block
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (!((Boolean) blockState.getValue(LIT)).booleanValue()) {
            return;
        }
        level.addParticle(this.flameParticle, blockPos.getX() + 0.5d + ((random.nextDouble() - 0.5d) * 0.2d), blockPos.getY() + 0.7d + ((random.nextDouble() - 0.5d) * 0.2d), blockPos.getZ() + 0.5d + ((random.nextDouble() - 0.5d) * 0.2d), 0.0d, 0.0d, 0.0d);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/RedstoneTorchBlock$Toggle.class */
    public static class Toggle {
        private final BlockPos pos;
        private final long when;

        public Toggle(BlockPos blockPos, long j) {
            this.pos = blockPos;
            this.when = j;
        }
    }

    private static boolean isToggledTooFrequently(Level level, BlockPos blockPos, boolean z) {
        List<Toggle> computeIfAbsent = RECENT_TOGGLES.computeIfAbsent(level, blockGetter -> {
            return Lists.newArrayList();
        });
        if (z) {
            computeIfAbsent.add(new Toggle(blockPos.immutable(), level.getGameTime()));
        }
        int i = 0;
        for (int i2 = 0; i2 < computeIfAbsent.size(); i2++) {
            if (computeIfAbsent.get(i2).pos.equals(blockPos)) {
                i++;
                if (i >= 8) {
                    return true;
                }
            }
        }
        return false;
    }
}
