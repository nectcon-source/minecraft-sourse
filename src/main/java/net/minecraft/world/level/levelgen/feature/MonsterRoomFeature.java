package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/MonsterRoomFeature.class */
public class MonsterRoomFeature extends Feature<NoneFeatureConfiguration> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final EntityType<?>[] MOBS = {EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public MonsterRoomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        int nextInt = random.nextInt(2) + 2;
        int i = (-nextInt) - 1;
        int i2 = nextInt + 1;
        int nextInt2 = random.nextInt(2) + 2;
        int i3 = (-nextInt2) - 1;
        int i4 = nextInt2 + 1;
        int i5 = 0;
        for (int i6 = i; i6 <= i2; i6++) {
            for (int i7 = -1; i7 <= 4; i7++) {
                for (int i8 = i3; i8 <= i4; i8++) {
                    BlockPos offset = blockPos.offset(i6, i7, i8);
                    boolean isSolid = worldGenLevel.getBlockState(offset).getMaterial().isSolid();
                    if (i7 == -1 && !isSolid) {
                        return false;
                    }
                    if (i7 == 4 && !isSolid) {
                        return false;
                    }
                    if ((i6 == i || i6 == i2 || i8 == i3 || i8 == i4) && i7 == 0 && worldGenLevel.isEmptyBlock(offset) && worldGenLevel.isEmptyBlock(offset.above())) {
                        i5++;
                    }
                }
            }
        }
        if (i5 < 1 || i5 > 5) {
            return false;
        }
        for (int i9 = i; i9 <= i2; i9++) {
            for (int i10 = 3; i10 >= -1; i10--) {
                for (int i11 = i3; i11 <= i4; i11++) {
                    BlockPos offset2 = blockPos.offset(i9, i10, i11);
                    BlockState blockState = worldGenLevel.getBlockState(offset2);
                    if (i9 == i || i10 == -1 || i11 == i3 || i9 == i2 || i10 == 4 || i11 == i4) {
                        if (offset2.getY() >= 0 && !worldGenLevel.getBlockState(offset2.below()).getMaterial().isSolid()) {
                            worldGenLevel.setBlock(offset2, AIR, 2);
                        } else if (blockState.getMaterial().isSolid() && !blockState.is(Blocks.CHEST)) {
                            if (i10 == -1 && random.nextInt(4) != 0) {
                                worldGenLevel.setBlock(offset2, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 2);
                            } else {
                                worldGenLevel.setBlock(offset2, Blocks.COBBLESTONE.defaultBlockState(), 2);
                            }
                        }
                    } else if (!blockState.is(Blocks.CHEST) && !blockState.is(Blocks.SPAWNER)) {
                        worldGenLevel.setBlock(offset2, AIR, 2);
                    }
                }
            }
        }
        for (int i12 = 0; i12 < 2; i12++) {
            int i13 = 0;
            while (true) {
                if (i13 < 3) {
                    BlockPos blockPos2 = new BlockPos((blockPos.getX() + random.nextInt((nextInt * 2) + 1)) - nextInt, blockPos.getY(), (blockPos.getZ() + random.nextInt((nextInt2 * 2) + 1)) - nextInt2);
                    if (worldGenLevel.isEmptyBlock(blockPos2)) {
                        int i14 = 0;
                        Iterator<Direction> it = Direction.Plane.HORIZONTAL.iterator();
                        while (it.hasNext()) {
                            if (worldGenLevel.getBlockState(blockPos2.relative(it.next())).getMaterial().isSolid()) {
                                i14++;
                            }
                        }
                        if (i14 == 1) {
                            worldGenLevel.setBlock(blockPos2, StructurePiece.reorient(worldGenLevel, blockPos2, Blocks.CHEST.defaultBlockState()), 2);
                            RandomizableContainerBlockEntity.setLootTable(worldGenLevel, random, blockPos2, BuiltInLootTables.SIMPLE_DUNGEON);
                            break;
                        }
                    }
                    i13++;
                }
            }
        }
        worldGenLevel.setBlock(blockPos, Blocks.SPAWNER.defaultBlockState(), 2);
        BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos);
        if (blockEntity instanceof SpawnerBlockEntity) {
            ((SpawnerBlockEntity) blockEntity).getSpawner().setEntityId(randomEntityId(random));
            return true;
        }
        LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", Integer.valueOf(blockPos.getX()), Integer.valueOf(blockPos.getY()), Integer.valueOf(blockPos.getZ()));
        return true;
    }

    private EntityType<?> randomEntityId(Random random) {
        return (EntityType) Util.getRandom(MOBS, random);
    }
}
