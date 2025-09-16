package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/stateproviders/RotatedBlockProvider.class */
public class RotatedBlockProvider extends BlockStateProvider {
    public static final Codec<RotatedBlockProvider> CODEC = BlockState.CODEC.fieldOf("state").xmap((v0) -> {
        return v0.getBlock();
    }, (v0) -> {
        return v0.defaultBlockState();
    }).xmap(RotatedBlockProvider::new, rotatedBlockProvider -> {
        return rotatedBlockProvider.block;
    }).codec();
    private final Block block;

    public RotatedBlockProvider(Block block) {
        this.block = block;
    }

    @Override // net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.ROTATED_BLOCK_PROVIDER;
    }

    @Override // net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider
    public BlockState getState(Random random, BlockPos blockPos) {
        return (BlockState) this.block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.getRandom(random));
    }
}
