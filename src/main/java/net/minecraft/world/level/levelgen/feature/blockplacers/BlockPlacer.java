package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/blockplacers/BlockPlacer.class */
public abstract class BlockPlacer {
    public static final Codec<BlockPlacer> CODEC = Registry.BLOCK_PLACER_TYPES.dispatch((v0) -> {
        return v0.type();
    }, (v0) -> {
        return v0.codec();
    });

    public abstract void place(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random);

    protected abstract BlockPlacerType<?> type();
}
