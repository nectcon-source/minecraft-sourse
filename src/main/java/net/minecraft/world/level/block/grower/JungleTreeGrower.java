package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/grower/JungleTreeGrower.class */
public class JungleTreeGrower extends AbstractMegaTreeGrower {
    @Override // net.minecraft.world.level.block.grower.AbstractTreeGrower
    @Nullable
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random random, boolean z) {
        return Features.JUNGLE_TREE_NO_VINE;
    }

    @Override // net.minecraft.world.level.block.grower.AbstractMegaTreeGrower
    @Nullable
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredMegaFeature(Random random) {
        return Features.MEGA_JUNGLE_TREE;
    }
}
