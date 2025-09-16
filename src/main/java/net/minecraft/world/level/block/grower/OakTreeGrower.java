package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/grower/OakTreeGrower.class */
public class OakTreeGrower extends AbstractTreeGrower {
    @Override // net.minecraft.world.level.block.grower.AbstractTreeGrower
    @Nullable
    protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random random, boolean z) {
        return random.nextInt(10) == 0 ? z ? Features.FANCY_OAK_BEES_005 : Features.FANCY_OAK : z ? Features.OAK_BEES_005 : Features.OAK;
    }
}
