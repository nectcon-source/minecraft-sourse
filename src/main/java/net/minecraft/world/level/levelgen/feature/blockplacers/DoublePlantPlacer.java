package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/blockplacers/DoublePlantPlacer.class */
public class DoublePlantPlacer extends BlockPlacer {
    public static final Codec<DoublePlantPlacer> CODEC = Codec.unit(() -> DoublePlantPlacer.INSTANCE);
    public static final DoublePlantPlacer INSTANCE = new DoublePlantPlacer();

    @Override // net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer
    protected BlockPlacerType<?> type() {
        return BlockPlacerType.DOUBLE_PLANT_PLACER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer
    public void place(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random) {
        ((DoublePlantBlock) blockState.getBlock()).placeAt(levelAccessor, blockPos, 2);
    }
}
