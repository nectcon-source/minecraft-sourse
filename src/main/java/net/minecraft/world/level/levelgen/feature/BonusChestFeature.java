package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/BonusChestFeature.class */
public class BonusChestFeature extends Feature<NoneFeatureConfiguration> {
    public BonusChestFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        ChunkPos chunkPos = new ChunkPos(blockPos);
        List<Integer> list = (List) IntStream.rangeClosed(chunkPos.getMinBlockX(), chunkPos.getMaxBlockX()).boxed().collect(Collectors.toList());
        Collections.shuffle(list, random);
        List<Integer> list2 = (List) IntStream.rangeClosed(chunkPos.getMinBlockZ(), chunkPos.getMaxBlockZ()).boxed().collect(Collectors.toList());
        Collections.shuffle(list2, random);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Integer num : list) {
            Iterator<Integer> it = list2.iterator();
            while (it.hasNext()) {
                mutableBlockPos.set(num.intValue(), 0, it.next().intValue());
                BlockPos heightmapPos = worldGenLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutableBlockPos);
                if (worldGenLevel.isEmptyBlock(heightmapPos) || worldGenLevel.getBlockState(heightmapPos).getCollisionShape(worldGenLevel, heightmapPos).isEmpty()) {
                    worldGenLevel.setBlock(heightmapPos, Blocks.CHEST.defaultBlockState(), 2);
                    RandomizableContainerBlockEntity.setLootTable(worldGenLevel, random, heightmapPos, BuiltInLootTables.SPAWN_BONUS_CHEST);
                    BlockState defaultBlockState = Blocks.TORCH.defaultBlockState();
                    Iterator<Direction> it2 = Direction.Plane.HORIZONTAL.iterator();
                    while (it2.hasNext()) {
                        BlockPos relative = heightmapPos.relative(it2.next());
                        if (defaultBlockState.canSurvive(worldGenLevel, relative)) {
                            worldGenLevel.setBlock(relative, defaultBlockState, 2);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
