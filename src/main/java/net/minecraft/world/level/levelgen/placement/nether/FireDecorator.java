package net.minecraft.world.level.levelgen.placement.nether;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/nether/FireDecorator.class */
public class FireDecorator extends SimpleFeatureDecorator<CountConfiguration> {
    public FireDecorator(Codec<CountConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.SimpleFeatureDecorator
    public Stream<BlockPos> place(Random random, CountConfiguration countConfiguration, BlockPos blockPos) {
        List<BlockPos> newArrayList = Lists.newArrayList();
        for (int i = 0; i < random.nextInt(random.nextInt(countConfiguration.count().sample(random)) + 1) + 1; i++) {
            newArrayList.add(new BlockPos(random.nextInt(16) + blockPos.getX(), random.nextInt(120) + 4, random.nextInt(16) + blockPos.getZ()));
        }
        return newArrayList.stream();
    }
}
