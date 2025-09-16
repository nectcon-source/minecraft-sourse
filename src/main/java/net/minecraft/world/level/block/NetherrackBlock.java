package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/NetherrackBlock.class */
public class NetherrackBlock extends Block implements BonemealableBlock {
    public NetherrackBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean z) {
        if (!blockGetter.getBlockState(blockPos.above()).propagatesSkylightDown(blockGetter, blockPos)) {
            return false;
        }
        Iterator<BlockPos> it = BlockPos.betweenClosed(blockPos.offset(-1, -1, -1), blockPos.offset(1, 1, 1)).iterator();
        while (it.hasNext()) {
            if (blockGetter.getBlockState(it.next()).is(BlockTags.NYLIUM)) {
                return true;
            }
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        boolean z = false;
        boolean z2 = false;
        Iterator<BlockPos> it = BlockPos.betweenClosed(blockPos.offset(-1, -1, -1), blockPos.offset(1, 1, 1)).iterator();
        while (it.hasNext()) {
            BlockState blockState2 = serverLevel.getBlockState(it.next());
            if (blockState2.is(Blocks.WARPED_NYLIUM)) {
                z2 = true;
            }
            if (blockState2.is(Blocks.CRIMSON_NYLIUM)) {
                z = true;
            }
            if (z2 && z) {
                break;
            }
        }
        if (z2 && z) {
            serverLevel.setBlock(blockPos, random.nextBoolean() ? Blocks.WARPED_NYLIUM.defaultBlockState() : Blocks.CRIMSON_NYLIUM.defaultBlockState(), 3);
        } else if (z2) {
            serverLevel.setBlock(blockPos, Blocks.WARPED_NYLIUM.defaultBlockState(), 3);
        } else if (z) {
            serverLevel.setBlock(blockPos, Blocks.CRIMSON_NYLIUM.defaultBlockState(), 3);
        }
    }
}
