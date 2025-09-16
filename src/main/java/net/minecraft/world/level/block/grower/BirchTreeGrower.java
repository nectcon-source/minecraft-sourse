package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/grower/BirchTreeGrower.class */
public class BirchTreeGrower extends AbstractTreeGrower {
    @Override // net.minecraft.world.level.block.grower.AbstractTreeGrower
    @Nullable
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random random, boolean z) {
        return z ? Features.BIRCH_BEES_005 : Features.BIRCH;
    }
}
