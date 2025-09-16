package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/blockplacers/SimpleBlockPlacer.class */
public class SimpleBlockPlacer extends BlockPlacer {

    public static final Codec<SimpleBlockPlacer> CODEC = Codec.unit(() -> SimpleBlockPlacer.INSTANCE);
    public static final SimpleBlockPlacer INSTANCE = new SimpleBlockPlacer();

    @Override // net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer
    protected BlockPlacerType<?> type() {
        return BlockPlacerType.SIMPLE_BLOCK_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer
    public void place(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random) {
        levelAccessor.setBlock(blockPos, blockState, 2);
    }
}
