package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/BonemealableBlock.class */
public interface BonemealableBlock {
    boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean z);

    boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState);

    void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState);
}
