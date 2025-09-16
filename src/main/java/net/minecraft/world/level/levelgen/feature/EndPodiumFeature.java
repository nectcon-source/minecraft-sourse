package net.minecraft.world.level.levelgen.feature;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/EndPodiumFeature.class */
public class EndPodiumFeature extends Feature<NoneFeatureConfiguration> {
    public static final BlockPos END_PODIUM_LOCATION = BlockPos.ZERO;
    private final boolean active;

    public EndPodiumFeature(boolean z) {
        super(NoneFeatureConfiguration.CODEC);
        this.active = z;
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        for (BlockPos blockPos2 : BlockPos.betweenClosed(new BlockPos(blockPos.getX() - 4, blockPos.getY() - 1, blockPos.getZ() - 4), new BlockPos(blockPos.getX() + 4, blockPos.getY() + 32, blockPos.getZ() + 4))) {
            boolean closerThan = blockPos2.closerThan(blockPos, 2.5d);
            if (closerThan || blockPos2.closerThan(blockPos, 3.5d)) {
                if (blockPos2.getY() < blockPos.getY()) {
                    if (closerThan) {
                        setBlock(worldGenLevel, blockPos2, Blocks.BEDROCK.defaultBlockState());
                    } else if (blockPos2.getY() < blockPos.getY()) {
                        setBlock(worldGenLevel, blockPos2, Blocks.END_STONE.defaultBlockState());
                    }
                } else if (blockPos2.getY() > blockPos.getY()) {
                    setBlock(worldGenLevel, blockPos2, Blocks.AIR.defaultBlockState());
                } else if (!closerThan) {
                    setBlock(worldGenLevel, blockPos2, Blocks.BEDROCK.defaultBlockState());
                } else if (this.active) {
                    setBlock(worldGenLevel, new BlockPos(blockPos2), Blocks.END_PORTAL.defaultBlockState());
                } else {
                    setBlock(worldGenLevel, new BlockPos(blockPos2), Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            setBlock(worldGenLevel, blockPos.above(i), Blocks.BEDROCK.defaultBlockState());
        }
        BlockPos above = blockPos.above(2);
        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
        while (it.hasNext()) {
            Direction next = it.next();
            setBlock(worldGenLevel, above.relative(next), (BlockState) Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, next));
        }
        return true;
    }
}
