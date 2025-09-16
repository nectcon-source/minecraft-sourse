package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/FrostedIceBlock.class */
public class FrostedIceBlock extends IceBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public FrostedIceBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override // net.minecraft.world.level.block.IceBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        tick(blockState, serverLevel, blockPos, random);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if ((random.nextInt(3) == 0 || fewerNeigboursThan(serverLevel, blockPos, 4)) && serverLevel.getMaxLocalRawBrightness(blockPos) > (11 - ((Integer) blockState.getValue(AGE)).intValue()) - blockState.getLightBlock(serverLevel, blockPos) && slightlyMelt(blockState, serverLevel, blockPos)) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (Direction direction : Direction.values()) {
                mutableBlockPos.setWithOffset(blockPos, direction);
                BlockState blockState2 = serverLevel.getBlockState(mutableBlockPos);
                if (blockState2.is(this) && !slightlyMelt(blockState2, serverLevel, mutableBlockPos)) {
                    serverLevel.getBlockTicks().scheduleTick(mutableBlockPos, this, Mth.nextInt(random, 20, 40));
                }
            }
            return;
        }
        serverLevel.getBlockTicks().scheduleTick(blockPos, this, Mth.nextInt(random, 20, 40));
    }

    private boolean slightlyMelt(BlockState blockState, Level level, BlockPos blockPos) {
        int intValue = ((Integer) blockState.getValue(AGE)).intValue();
        if (intValue < 3) {
            level.setBlock(blockPos, (BlockState) blockState.setValue(AGE, Integer.valueOf(intValue + 1)), 2);
            return false;
        }
        melt(blockState, level, blockPos);
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        if (block == this && fewerNeigboursThan(level, blockPos, 2)) {
            melt(blockState, level, blockPos);
        }
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, z);
    }

    private boolean fewerNeigboursThan(BlockGetter blockGetter, BlockPos blockPos, int i) {
        int i2 = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            mutableBlockPos.setWithOffset(blockPos, direction);
            if (blockGetter.getBlockState(mutableBlockPos).is(this)) {
                i2++;
                if (i2 >= i) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override // net.minecraft.world.level.block.Block
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return ItemStack.EMPTY;
    }
}
