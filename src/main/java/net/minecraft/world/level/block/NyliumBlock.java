package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.Features;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.NetherForestVegetationFeature;
import net.minecraft.world.level.levelgen.feature.TwistingVinesFeature;
import net.minecraft.world.level.lighting.LayerLightEngine;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/NyliumBlock.class */
public class NyliumBlock extends Block implements BonemealableBlock {
    protected NyliumBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    private static boolean canBeNylium(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos above = blockPos.above();
        BlockState blockState2 = levelReader.getBlockState(above);
        return LayerLightEngine.getLightBlockInto(levelReader, blockState, blockPos, blockState2, above, Direction.UP, blockState2.getLightBlock(levelReader, above)) < levelReader.getMaxLightLevel();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!canBeNylium(blockState, serverLevel, blockPos)) {
            serverLevel.setBlockAndUpdate(blockPos, Blocks.NETHERRACK.defaultBlockState());
        }
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean z) {
        return blockGetter.getBlockState(blockPos.above()).isAir();
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        BlockState blockState2 = serverLevel.getBlockState(blockPos);
        BlockPos above = blockPos.above();
        if (blockState2.is(Blocks.CRIMSON_NYLIUM)) {
            NetherForestVegetationFeature.place(serverLevel, random, above, Features.Configs.CRIMSON_FOREST_CONFIG, 3, 1);
            return;
        }
        if (blockState2.is(Blocks.WARPED_NYLIUM)) {
            NetherForestVegetationFeature.place(serverLevel, random, above, Features.Configs.WARPED_FOREST_CONFIG, 3, 1);
            NetherForestVegetationFeature.place(serverLevel, random, above, Features.Configs.NETHER_SPROUTS_CONFIG, 3, 1);
            if (random.nextInt(8) == 0) {
                TwistingVinesFeature.place(serverLevel, random, above, 3, 1, 2);
            }
        }
    }
}
