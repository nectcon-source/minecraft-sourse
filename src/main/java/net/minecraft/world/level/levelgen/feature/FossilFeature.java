package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/FossilFeature.class */
public class FossilFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation SPINE_1 = new ResourceLocation("fossil/spine_1");
    private static final ResourceLocation SPINE_2 = new ResourceLocation("fossil/spine_2");
    private static final ResourceLocation SPINE_3 = new ResourceLocation("fossil/spine_3");
    private static final ResourceLocation SPINE_4 = new ResourceLocation("fossil/spine_4");
    private static final ResourceLocation SPINE_1_COAL = new ResourceLocation("fossil/spine_1_coal");
    private static final ResourceLocation SPINE_2_COAL = new ResourceLocation("fossil/spine_2_coal");
    private static final ResourceLocation SPINE_3_COAL = new ResourceLocation("fossil/spine_3_coal");
    private static final ResourceLocation SPINE_4_COAL = new ResourceLocation("fossil/spine_4_coal");
    private static final ResourceLocation SKULL_1 = new ResourceLocation("fossil/skull_1");
    private static final ResourceLocation SKULL_2 = new ResourceLocation("fossil/skull_2");
    private static final ResourceLocation SKULL_3 = new ResourceLocation("fossil/skull_3");
    private static final ResourceLocation SKULL_4 = new ResourceLocation("fossil/skull_4");
    private static final ResourceLocation SKULL_1_COAL = new ResourceLocation("fossil/skull_1_coal");
    private static final ResourceLocation SKULL_2_COAL = new ResourceLocation("fossil/skull_2_coal");
    private static final ResourceLocation SKULL_3_COAL = new ResourceLocation("fossil/skull_3_coal");
    private static final ResourceLocation SKULL_4_COAL = new ResourceLocation("fossil/skull_4_coal");
    private static final ResourceLocation[] fossils = {SPINE_1, SPINE_2, SPINE_3, SPINE_4, SKULL_1, SKULL_2, SKULL_3, SKULL_4};
    private static final ResourceLocation[] fossilsCoal = {SPINE_1_COAL, SPINE_2_COAL, SPINE_3_COAL, SPINE_4_COAL, SKULL_1_COAL, SKULL_2_COAL, SKULL_3_COAL, SKULL_4_COAL};

    public FossilFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.Feature
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        Rotation random2 = Rotation.getRandom(random);
        int nextInt = random.nextInt(fossils.length);
        StructureManager structureManager = worldGenLevel.getLevel().getServer().getStructureManager();
        StructureTemplate orCreate = structureManager.getOrCreate(fossils[nextInt]);
        StructureTemplate orCreate2 = structureManager.getOrCreate(fossilsCoal[nextInt]);
        ChunkPos chunkPos = new ChunkPos(blockPos);
        StructurePlaceSettings addProcessor = new StructurePlaceSettings().setRotation(random2).setBoundingBox(new BoundingBox(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), 256, chunkPos.getMaxBlockZ())).setRandom(random).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        BlockPos size = orCreate.getSize(random2);
        int nextInt2 = random.nextInt(16 - size.getX());
        int nextInt3 = random.nextInt(16 - size.getZ());
        int i = 256;
        for (int i2 = 0; i2 < size.getX(); i2++) {
            for (int i3 = 0; i3 < size.getZ(); i3++) {
                i = Math.min(i, worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockPos.getX() + i2 + nextInt2, blockPos.getZ() + i3 + nextInt3));
            }
        }
        BlockPos zeroPositionWithTransform = orCreate.getZeroPositionWithTransform(blockPos.offset(nextInt2, Math.max((i - 15) - random.nextInt(10), 10), nextInt3), Mirror.NONE, random2);
        BlockRotProcessor blockRotProcessor = new BlockRotProcessor(0.9f);
        addProcessor.clearProcessors().addProcessor(blockRotProcessor);
        orCreate.placeInWorld(worldGenLevel, zeroPositionWithTransform, zeroPositionWithTransform, addProcessor, random, 4);
        addProcessor.popProcessor(blockRotProcessor);
        addProcessor.clearProcessors().addProcessor(new BlockRotProcessor(0.1f));
        orCreate2.placeInWorld(worldGenLevel, zeroPositionWithTransform, zeroPositionWithTransform, addProcessor, random, 4);
        return true;
    }
}
