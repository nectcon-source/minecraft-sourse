package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/VinesFeature.class */
public class VinesFeature extends Feature<NoneFeatureConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();

    public VinesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        BlockPos.MutableBlockPos mutable = blockPos.mutable();
        for (int i = 64; i < 256; i++) {
            mutable.set(blockPos);
            mutable.move(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
            mutable.setY(i);
            if (worldGenLevel.isEmptyBlock(mutable)) {
                Direction[] directionArr = DIRECTIONS;
                int length = directionArr.length;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    }
                    Direction direction = directionArr[i2];
                    if (direction == Direction.DOWN || !VineBlock.isAcceptableNeighbour(worldGenLevel, mutable, direction)) {
                        i2++;
                    } else {
                        worldGenLevel.setBlock(mutable, (BlockState) Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(direction), true), 2);
                        break;
                    }
                }
            }
        }
        return true;
    }
}
