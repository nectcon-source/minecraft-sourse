package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/BlockPileConfiguration.class */
public class BlockPileConfiguration implements FeatureConfiguration {
    public static final Codec<BlockPileConfiguration> CODEC = BlockStateProvider.CODEC.fieldOf("state_provider").xmap(BlockPileConfiguration::new, blockPileConfiguration -> {
        return blockPileConfiguration.stateProvider;
    }).codec();

    public final BlockStateProvider stateProvider;

    public BlockPileConfiguration(BlockStateProvider blockStateProvider) {
        this.stateProvider = blockStateProvider;
    }
}
