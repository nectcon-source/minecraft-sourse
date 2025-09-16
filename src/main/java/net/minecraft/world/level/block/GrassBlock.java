package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractFlowerFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/GrassBlock.class */
public class GrassBlock extends SpreadingSnowyDirtBlock implements BonemealableBlock {
    public GrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
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
        BlockPos var5 = blockPos.above();
        BlockState var6 = Blocks.GRASS.defaultBlockState();

        label48:
        for(int var7 = 0; var7 < 128; ++var7) {
            BlockPos var8 = var5;

            for(int var9 = 0; var9 < var7 / 16; ++var9) {
                var8 = var8.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (!serverLevel.getBlockState(var8.below()).is(this) || serverLevel.getBlockState(var8).isCollisionShapeFullBlock(serverLevel, var8)) {
                    continue label48;
                }
            }

            BlockState var14 = serverLevel.getBlockState(var8);
            if (var14.is(var6.getBlock()) && random.nextInt(10) == 0) {
                ((BonemealableBlock)var6.getBlock()).performBonemeal(serverLevel, random, var8, var14);
            }

            if (var14.isAir()) {
                BlockState var10;
                if (random.nextInt(8) == 0) {
                    List<ConfiguredFeature<?, ?>> var11 = serverLevel.getBiome(var8).getGenerationSettings().getFlowerFeatures();
                    if (var11.isEmpty()) {
                        continue;
                    }

                    ConfiguredFeature<?, ?> var12 = var11.get(0);
                    AbstractFlowerFeature var13 = (AbstractFlowerFeature)var12.feature;
                    var10 = var13.getRandomFlower(random, var8, var12.config());
                } else {
                    var10 = var6;
                }

                if (var10.canSurvive(serverLevel, var8)) {
                    serverLevel.setBlock(var8, var10, 3);
                }
            }
        }
    }
}
