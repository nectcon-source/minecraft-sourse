package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/TrappedChestBlock.class */
public class TrappedChestBlock extends ChestBlock {
    public TrappedChestBlock(BlockBehaviour.Properties properties) {
        super(properties, () -> {
            return BlockEntityType.TRAPPED_CHEST;
        });
    }

    @Override // net.minecraft.world.level.block.ChestBlock, net.minecraft.world.level.block.EntityBlock
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new TrappedChestBlockEntity();
    }

    @Override // net.minecraft.world.level.block.ChestBlock
    protected Stat<ResourceLocation> getOpenChestStat() {
        return Stats.CUSTOM.get(Stats.TRIGGER_TRAPPED_CHEST);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return Mth.clamp(ChestBlockEntity.getOpenCount(blockGetter, blockPos), 0, 15);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (direction == Direction.UP) {
            return blockState.getSignal(blockGetter, blockPos, direction);
        }
        return 0;
    }
}
