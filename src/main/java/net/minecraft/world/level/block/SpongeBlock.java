package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SpongeBlock.class */
public class SpongeBlock extends Block {
    protected SpongeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        tryAbsorbWater(level, blockPos);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        tryAbsorbWater(level, blockPos);
        super.neighborChanged(blockState, level, blockPos, block, blockPos2, z);
    }

    protected void tryAbsorbWater(Level level, BlockPos blockPos) {
        if (removeWaterBreadthFirstSearch(level, blockPos)) {
            level.setBlock(blockPos, Blocks.WET_SPONGE.defaultBlockState(), 2);
            level.levelEvent(2001, blockPos, Block.getId(Blocks.WATER.defaultBlockState()));
        }
    }

    private boolean removeWaterBreadthFirstSearch(Level level, BlockPos blockPos) {
        Queue<Tuple<BlockPos, Integer>> newLinkedList = Lists.newLinkedList();
        newLinkedList.add(new Tuple<>(blockPos, 0));
        int i = 0;
        while (!newLinkedList.isEmpty()) {
            Tuple<BlockPos, Integer> poll = newLinkedList.poll();
            BlockPos a = poll.getA();
            int intValue = poll.getB().intValue();
            for (Direction direction : Direction.values()) {
                BlockPos relative = a.relative(direction);
                BlockState blockState = level.getBlockState(relative);
                FluidState fluidState = level.getFluidState(relative);
                Material material = blockState.getMaterial();
                if (fluidState.is(FluidTags.WATER)) {
                    if ((blockState.getBlock() instanceof BucketPickup) && ((BucketPickup) blockState.getBlock()).takeLiquid(level, relative, blockState) != Fluids.EMPTY) {
                        i++;
                        if (intValue < 6) {
                            newLinkedList.add(new Tuple<>(relative, Integer.valueOf(intValue + 1)));
                        }
                    } else if (blockState.getBlock() instanceof LiquidBlock) {
                        level.setBlock(relative, Blocks.AIR.defaultBlockState(), 3);
                        i++;
                        if (intValue < 6) {
                            newLinkedList.add(new Tuple<>(relative, Integer.valueOf(intValue + 1)));
                        }
                    } else if (material == Material.WATER_PLANT || material == Material.REPLACEABLE_WATER_PLANT) {
                        dropResources(blockState, level, relative, blockState.getBlock().isEntityBlock() ? level.getBlockEntity(relative) : null);
                        level.setBlock(relative, Blocks.AIR.defaultBlockState(), 3);
                        i++;
                        if (intValue < 6) {
                            newLinkedList.add(new Tuple<>(relative, Integer.valueOf(intValue + 1)));
                        }
                    }
                }
            }
            if (i > 64) {
                break;
            }
        }
        return i > 0;
    }
}
