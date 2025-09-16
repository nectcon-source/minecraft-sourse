package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/BlockStateConfiguration.class */
public class BlockStateConfiguration implements FeatureConfiguration {
    public static final Codec<BlockStateConfiguration> CODEC = BlockState.CODEC.fieldOf("state").xmap(BlockStateConfiguration::new, blockStateConfiguration -> {
        return blockStateConfiguration.state;
    }).codec();
    public final BlockState state;

    public BlockStateConfiguration(BlockState blockState) {
        this.state = blockState;
    }
}
