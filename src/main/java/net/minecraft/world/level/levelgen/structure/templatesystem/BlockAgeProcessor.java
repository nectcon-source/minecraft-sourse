package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/BlockAgeProcessor.class */
public class BlockAgeProcessor extends StructureProcessor {
    public static final Codec<BlockAgeProcessor> CODEC = Codec.FLOAT.fieldOf("mossiness").xmap((v1) -> {
        return new BlockAgeProcessor(v1);
    }, blockAgeProcessor -> {
        return Float.valueOf(blockAgeProcessor.mossiness);
    }).codec();
    private final float mossiness;

    public BlockAgeProcessor(float f) {
        this.mossiness = f;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        Random random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        BlockState blockState = structureBlockInfo2.state;
        BlockPos blockPos3 = structureBlockInfo2.pos;
        BlockState blockState2 = null;
        if (blockState.is(Blocks.STONE_BRICKS) || blockState.is(Blocks.STONE) || blockState.is(Blocks.CHISELED_STONE_BRICKS)) {
            blockState2 = maybeReplaceFullStoneBlock(random);
        } else if (blockState.is(BlockTags.STAIRS)) {
            blockState2 = maybeReplaceStairs(random, structureBlockInfo2.state);
        } else if (blockState.is(BlockTags.SLABS)) {
            blockState2 = maybeReplaceSlab(random);
        } else if (blockState.is(BlockTags.WALLS)) {
            blockState2 = maybeReplaceWall(random);
        } else if (blockState.is(Blocks.OBSIDIAN)) {
            blockState2 = maybeReplaceObsidian(random);
        }
        if (blockState2 != null) {
            return new StructureTemplate.StructureBlockInfo(blockPos3, blockState2, structureBlockInfo2.nbt);
        }
        return structureBlockInfo2;
    }

    @Nullable
    private BlockState maybeReplaceFullStoneBlock(Random random) {
        if (random.nextFloat() >= 0.5f) {
            return null;
        }
        return getRandomBlock(random, new BlockState[]{Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(random, Blocks.STONE_BRICK_STAIRS)}, new BlockState[]{Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(random, Blocks.MOSSY_STONE_BRICK_STAIRS)});
    }

    @Nullable
    private BlockState maybeReplaceStairs(Random random, BlockState blockState) {
        Direction direction = (Direction) blockState.getValue(StairBlock.FACING);
        Half half = (Half) blockState.getValue(StairBlock.HALF);
        if (random.nextFloat() >= 0.5f) {
            return null;
        }
        return getRandomBlock(random, new BlockState[]{Blocks.STONE_SLAB.defaultBlockState(), Blocks.STONE_BRICK_SLAB.defaultBlockState()}, new BlockState[]{(BlockState) ((BlockState) Blocks.MOSSY_STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, direction)).setValue(StairBlock.HALF, half), Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState()});
    }

    @Nullable
    private BlockState maybeReplaceSlab(Random random) {
        if (random.nextFloat() < this.mossiness) {
            return Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState();
        }
        return null;
    }

    @Nullable
    private BlockState maybeReplaceWall(Random random) {
        if (random.nextFloat() < this.mossiness) {
            return Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState();
        }
        return null;
    }

    @Nullable
    private BlockState maybeReplaceObsidian(Random random) {
        if (random.nextFloat() < 0.15f) {
            return Blocks.CRYING_OBSIDIAN.defaultBlockState();
        }
        return null;
    }

    private static BlockState getRandomFacingStairs(Random random, Block block) {
        return (BlockState) ((BlockState) block.defaultBlockState().setValue(StairBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random))).setValue(StairBlock.HALF, Half.values()[random.nextInt(Half.values().length)]);
    }

    private BlockState getRandomBlock(Random random, BlockState[] blockStateArr, BlockState[] blockStateArr2) {
        if (random.nextFloat() < this.mossiness) {
            return getRandomBlock(random, blockStateArr2);
        }
        return getRandomBlock(random, blockStateArr);
    }

    private static BlockState getRandomBlock(Random random, BlockState[] blockStateArr) {
        return blockStateArr[random.nextInt(blockStateArr.length)];
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_AGE;
    }
}
