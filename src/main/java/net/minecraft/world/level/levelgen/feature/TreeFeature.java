package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/TreeFeature.class */
public class TreeFeature extends Feature<TreeConfiguration> {
    public TreeFeature(Codec<TreeConfiguration> codec) {
        super(codec);
    }

    public static boolean isFree(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return validTreePos(levelSimulatedReader, blockPos) || levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            return blockState.is(BlockTags.LOGS);
        });
    }

    private static boolean isVine(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            return blockState.is(Blocks.VINE);
        });
    }

    private static boolean isBlockWater(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            return blockState.is(Blocks.WATER);
        });
    }

    public static boolean isAirOrLeaves(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            return blockState.isAir() || blockState.is(BlockTags.LEAVES);
        });
    }

    private static boolean isGrassOrDirtOrFarmland(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            Block block = blockState.getBlock();
            return isDirt(block) || block == Blocks.FARMLAND;
        });
    }

    private static boolean isReplaceablePlant(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> {
            return blockState.getMaterial() == Material.REPLACEABLE_PLANT;
        });
    }

    public static void setBlockKnownShape(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        levelWriter.setBlock(blockPos, blockState, 19);
    }

    public static boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return isAirOrLeaves(levelSimulatedReader, blockPos) || isReplaceablePlant(levelSimulatedReader, blockPos) || isBlockWater(levelSimulatedReader, blockPos);
    }

    private boolean doPlace(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos, Set<BlockPos> set, Set<BlockPos> set2, BoundingBox boundingBox, TreeConfiguration treeConfiguration) {
        BlockPos blockPos2;
        int y;
        int treeHeight = treeConfiguration.trunkPlacer.getTreeHeight(random);
        int foliageHeight = treeConfiguration.foliagePlacer.foliageHeight(random, treeHeight, treeConfiguration);
        int foliageRadius = treeConfiguration.foliagePlacer.foliageRadius(random, treeHeight - foliageHeight);
        if (!treeConfiguration.fromSapling) {
            int y2 = levelSimulatedRW.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR, blockPos).getY();
            int y3 = levelSimulatedRW.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPos).getY();
            if (y3 - y2 > treeConfiguration.maxWaterDepth) {
                return false;
            }
            if (treeConfiguration.heightmap == Heightmap.Types.OCEAN_FLOOR) {
                y = y2;
            } else if (treeConfiguration.heightmap == Heightmap.Types.WORLD_SURFACE) {
                y = y3;
            } else {
                y = levelSimulatedRW.getHeightmapPos(treeConfiguration.heightmap, blockPos).getY();
            }
            blockPos2 = new BlockPos(blockPos.getX(), y, blockPos.getZ());
        } else {
            blockPos2 = blockPos;
        }
        if (blockPos2.getY() < 1 || blockPos2.getY() + treeHeight + 1 > 256 || !isGrassOrDirtOrFarmland(levelSimulatedRW, blockPos2.below())) {
            return false;
        }
        OptionalInt minClippedHeight = treeConfiguration.minimumSize.minClippedHeight();
        int maxFreeTreeHeight = getMaxFreeTreeHeight(levelSimulatedRW, treeHeight, blockPos2, treeConfiguration);
        if (maxFreeTreeHeight < treeHeight && (!minClippedHeight.isPresent() || maxFreeTreeHeight < minClippedHeight.getAsInt())) {
            return false;
        }
        treeConfiguration.trunkPlacer.placeTrunk(levelSimulatedRW, random, maxFreeTreeHeight, blockPos2, set, boundingBox, treeConfiguration).forEach(foliageAttachment -> {
            treeConfiguration.foliagePlacer.createFoliage(levelSimulatedRW, random, treeConfiguration, maxFreeTreeHeight, foliageAttachment, foliageHeight, foliageRadius, set2, boundingBox);
        });
        return true;
    }

    private int getMaxFreeTreeHeight(LevelSimulatedReader levelSimulatedReader, int i, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i2 = 0; i2 <= i + 1; i2++) {
            int sizeAtHeight = treeConfiguration.minimumSize.getSizeAtHeight(i, i2);
            for (int i3 = -sizeAtHeight; i3 <= sizeAtHeight; i3++) {
                for (int i4 = -sizeAtHeight; i4 <= sizeAtHeight; i4++) {
                    mutableBlockPos.setWithOffset(blockPos, i3, i2, i4);
                    if (!isFree(levelSimulatedReader, mutableBlockPos) || (!treeConfiguration.ignoreVines && isVine(levelSimulatedReader, mutableBlockPos))) {
                        return i2 - 2;
                    }
                }
            }
        }
        return i;
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        setBlockKnownShape(levelWriter, blockPos, blockState);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public final boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, TreeConfiguration treeConfiguration) {
        Set<BlockPos> newHashSet = Sets.newHashSet();
        Set<BlockPos> newHashSet2 = Sets.newHashSet();
        Set<BlockPos> newHashSet3 = Sets.newHashSet();
        BoundingBox unknownBox = BoundingBox.getUnknownBox();
        boolean doPlace = doPlace(worldGenLevel, random, blockPos, newHashSet, newHashSet2, unknownBox, treeConfiguration);
        if (unknownBox.x0 > unknownBox.x1 || !doPlace || newHashSet.isEmpty()) {
            return false;
        }
        if (!treeConfiguration.decorators.isEmpty()) {
            List<BlockPos> newArrayList = Lists.newArrayList(newHashSet);
            List<BlockPos> newArrayList2 = Lists.newArrayList(newHashSet2);
            newArrayList.sort(Comparator.comparingInt((v0) -> {
                return v0.getY();
            }));
            newArrayList2.sort(Comparator.comparingInt((v0) -> {
                return v0.getY();
            }));
            treeConfiguration.decorators.forEach(treeDecorator -> {
                treeDecorator.place(worldGenLevel, random, newArrayList, newArrayList2, newHashSet3, unknownBox);
            });
        }
        StructureTemplate.updateShapeAtEdge(worldGenLevel, 3, updateLeaves(worldGenLevel, unknownBox, newHashSet, newHashSet3), unknownBox.x0, unknownBox.y0, unknownBox.z0);
        return true;
    }

    private DiscreteVoxelShape updateLeaves(LevelAccessor levelAccessor, BoundingBox boundingBox, Set<BlockPos> set, Set<BlockPos> set2) {
        List<Set<BlockPos>> newArrayList = Lists.newArrayList();
        DiscreteVoxelShape bitSetDiscreteVoxelShape = new BitSetDiscreteVoxelShape(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());
        for (int i = 0; i < 6; i++) {
            newArrayList.add(Sets.newHashSet());
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        Iterator it = Lists.newArrayList(set2).iterator();
        while (it.hasNext()) {
            BlockPos blockPos = (BlockPos) it.next();
            if (boundingBox.isInside(blockPos)) {
                bitSetDiscreteVoxelShape.setFull(blockPos.getX() - boundingBox.x0, blockPos.getY() - boundingBox.y0, blockPos.getZ() - boundingBox.z0, true, true);
            }
        }
        Iterator it2 = Lists.newArrayList(set).iterator();
        while (it2.hasNext()) {
            BlockPos blockPos2 = (BlockPos) it2.next();
            if (boundingBox.isInside(blockPos2)) {
                bitSetDiscreteVoxelShape.setFull(blockPos2.getX() - boundingBox.x0, blockPos2.getY() - boundingBox.y0, blockPos2.getZ() - boundingBox.z0, true, true);
            }
            for (Direction direction : Direction.values()) {
                mutableBlockPos.setWithOffset(blockPos2, direction);
                if (!set.contains(mutableBlockPos)) {
                    BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
                    if (blockState.hasProperty(BlockStateProperties.DISTANCE)) {
                        newArrayList.get(0).add(mutableBlockPos.immutable());
                        setBlockKnownShape(levelAccessor, mutableBlockPos, (BlockState) blockState.setValue(BlockStateProperties.DISTANCE, 1));
                        if (boundingBox.isInside(mutableBlockPos)) {
                            bitSetDiscreteVoxelShape.setFull(mutableBlockPos.getX() - boundingBox.x0, mutableBlockPos.getY() - boundingBox.y0, mutableBlockPos.getZ() - boundingBox.z0, true, true);
                        }
                    }
                }
            }
        }
        for (int i2 = 1; i2 < 6; i2++) {
            Set<BlockPos> set3 = newArrayList.get(i2 - 1);
            Set<BlockPos> set4 = newArrayList.get(i2);
            for (BlockPos blockPos3 : set3) {
                if (boundingBox.isInside(blockPos3)) {
                    bitSetDiscreteVoxelShape.setFull(blockPos3.getX() - boundingBox.x0, blockPos3.getY() - boundingBox.y0, blockPos3.getZ() - boundingBox.z0, true, true);
                }
                for (Direction direction2 : Direction.values()) {
                    mutableBlockPos.setWithOffset(blockPos3, direction2);
                    if (!set3.contains(mutableBlockPos) && !set4.contains(mutableBlockPos)) {
                        BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
                        if (blockState2.hasProperty(BlockStateProperties.DISTANCE) && ((Integer) blockState2.getValue(BlockStateProperties.DISTANCE)).intValue() > i2 + 1) {
                            setBlockKnownShape(levelAccessor, mutableBlockPos, (BlockState) blockState2.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(i2 + 1)));
                            if (boundingBox.isInside(mutableBlockPos)) {
                                bitSetDiscreteVoxelShape.setFull(mutableBlockPos.getX() - boundingBox.x0, mutableBlockPos.getY() - boundingBox.y0, mutableBlockPos.getZ() - boundingBox.z0, true, true);
                            }
                            set4.add(mutableBlockPos.immutable());
                        }
                    }
                }
            }
        }
        return bitSetDiscreteVoxelShape;
    }
}
