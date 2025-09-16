package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/stateproviders/PlainFlowerProvider.class */
public class PlainFlowerProvider extends BlockStateProvider {

    public static final Codec<PlainFlowerProvider> CODEC = Codec.unit(() -> PlainFlowerProvider.INSTANCE);
    public static final PlainFlowerProvider INSTANCE = new PlainFlowerProvider();
    private static final BlockState[] LOW_NOISE_FLOWERS = {Blocks.ORANGE_TULIP.defaultBlockState(), Blocks.RED_TULIP.defaultBlockState(), Blocks.PINK_TULIP.defaultBlockState(), Blocks.WHITE_TULIP.defaultBlockState()};
    private static final BlockState[] HIGH_NOISE_FLOWERS = {Blocks.POPPY.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState()};

    @Override // net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.PLAIN_FLOWER_PROVIDER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
    public BlockState getState(Random random, BlockPos blockPos) {
        if (Biome.BIOME_INFO_NOISE.getValue(blockPos.getX() / 200.0d, blockPos.getZ() / 200.0d, false) < -0.8d) {
            return (BlockState) Util.getRandom(LOW_NOISE_FLOWERS, random);
        }
        if (random.nextInt(3) > 0) {
            return (BlockState) Util.getRandom(HIGH_NOISE_FLOWERS, random);
        }
        return Blocks.DANDELION.defaultBlockState();
    }
}
