package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanRuinPieces.class */
public class OceanRuinPieces {
    private static final ResourceLocation[] WARM_RUINS = {new ResourceLocation("underwater_ruin/warm_1"), new ResourceLocation("underwater_ruin/warm_2"), new ResourceLocation("underwater_ruin/warm_3"), new ResourceLocation("underwater_ruin/warm_4"), new ResourceLocation("underwater_ruin/warm_5"), new ResourceLocation("underwater_ruin/warm_6"), new ResourceLocation("underwater_ruin/warm_7"), new ResourceLocation("underwater_ruin/warm_8")};
    private static final ResourceLocation[] RUINS_BRICK = {new ResourceLocation("underwater_ruin/brick_1"), new ResourceLocation("underwater_ruin/brick_2"), new ResourceLocation("underwater_ruin/brick_3"), new ResourceLocation("underwater_ruin/brick_4"), new ResourceLocation("underwater_ruin/brick_5"), new ResourceLocation("underwater_ruin/brick_6"), new ResourceLocation("underwater_ruin/brick_7"), new ResourceLocation("underwater_ruin/brick_8")};
    private static final ResourceLocation[] RUINS_CRACKED = {new ResourceLocation("underwater_ruin/cracked_1"), new ResourceLocation("underwater_ruin/cracked_2"), new ResourceLocation("underwater_ruin/cracked_3"), new ResourceLocation("underwater_ruin/cracked_4"), new ResourceLocation("underwater_ruin/cracked_5"), new ResourceLocation("underwater_ruin/cracked_6"), new ResourceLocation("underwater_ruin/cracked_7"), new ResourceLocation("underwater_ruin/cracked_8")};
    private static final ResourceLocation[] RUINS_MOSSY = {new ResourceLocation("underwater_ruin/mossy_1"), new ResourceLocation("underwater_ruin/mossy_2"), new ResourceLocation("underwater_ruin/mossy_3"), new ResourceLocation("underwater_ruin/mossy_4"), new ResourceLocation("underwater_ruin/mossy_5"), new ResourceLocation("underwater_ruin/mossy_6"), new ResourceLocation("underwater_ruin/mossy_7"), new ResourceLocation("underwater_ruin/mossy_8")};
    private static final ResourceLocation[] BIG_RUINS_BRICK = {new ResourceLocation("underwater_ruin/big_brick_1"), new ResourceLocation("underwater_ruin/big_brick_2"), new ResourceLocation("underwater_ruin/big_brick_3"), new ResourceLocation("underwater_ruin/big_brick_8")};
    private static final ResourceLocation[] BIG_RUINS_MOSSY = {new ResourceLocation("underwater_ruin/big_mossy_1"), new ResourceLocation("underwater_ruin/big_mossy_2"), new ResourceLocation("underwater_ruin/big_mossy_3"), new ResourceLocation("underwater_ruin/big_mossy_8")};
    private static final ResourceLocation[] BIG_RUINS_CRACKED = {new ResourceLocation("underwater_ruin/big_cracked_1"), new ResourceLocation("underwater_ruin/big_cracked_2"), new ResourceLocation("underwater_ruin/big_cracked_3"), new ResourceLocation("underwater_ruin/big_cracked_8")};
    private static final ResourceLocation[] BIG_WARM_RUINS = {new ResourceLocation("underwater_ruin/big_warm_4"), new ResourceLocation("underwater_ruin/big_warm_5"), new ResourceLocation("underwater_ruin/big_warm_6"), new ResourceLocation("underwater_ruin/big_warm_7")};

    private static ResourceLocation getSmallWarmRuin(Random random) {
        return (ResourceLocation) Util.getRandom(WARM_RUINS, random);
    }

    private static ResourceLocation getBigWarmRuin(Random random) {
        return (ResourceLocation) Util.getRandom(BIG_WARM_RUINS, random);
    }

    public static void addPieces(StructureManager structureManager, BlockPos blockPos, Rotation rotation, List<StructurePiece> list, Random random, OceanRuinConfiguration oceanRuinConfiguration) {
        boolean z = random.nextFloat() <= oceanRuinConfiguration.largeProbability;
        addPiece(structureManager, blockPos, rotation, list, random, oceanRuinConfiguration, z, z ? 0.9f : 0.8f);
        if (z && random.nextFloat() <= oceanRuinConfiguration.clusterProbability) {
            addClusterRuins(structureManager, random, rotation, blockPos, oceanRuinConfiguration, list);
        }
    }

    private static void addClusterRuins(StructureManager structureManager, Random random, Rotation rotation, BlockPos blockPos, OceanRuinConfiguration oceanRuinConfiguration, List<StructurePiece> list) {
        int x = blockPos.getX();
        int z = blockPos.getZ();
        BlockPos offset = StructureTemplate.transform(new BlockPos(15, 0, 15), Mirror.NONE, rotation, BlockPos.ZERO).offset(x, 0, z);
        BoundingBox createProper = BoundingBox.createProper(x, 0, z, offset.getX(), 0, offset.getZ());
        BlockPos blockPos2 = new BlockPos(Math.min(x, offset.getX()), 0, Math.min(z, offset.getZ()));
        List<BlockPos> allPositions = allPositions(random, blockPos2.getX(), blockPos2.getZ());
        int nextInt = Mth.nextInt(random, 4, 8);
        for (int i = 0; i < nextInt; i++) {
            if (!allPositions.isEmpty()) {
                BlockPos remove = allPositions.remove(random.nextInt(allPositions.size()));
                int x2 = remove.getX();
                int z2 = remove.getZ();
                Rotation random2 = Rotation.getRandom(random);
                BlockPos offset2 = StructureTemplate.transform(new BlockPos(5, 0, 6), Mirror.NONE, random2, BlockPos.ZERO).offset(x2, 0, z2);
                if (!BoundingBox.createProper(x2, 0, z2, offset2.getX(), 0, offset2.getZ()).intersects(createProper)) {
                    addPiece(structureManager, remove, random2, list, random, oceanRuinConfiguration, false, 0.8f);
                }
            }
        }
    }

    private static List<BlockPos> allPositions(Random random, int i, int i2) {
        List<BlockPos> newArrayList = Lists.newArrayList();
        newArrayList.add(new BlockPos((i - 16) + Mth.nextInt(random, 1, 8), 90, i2 + 16 + Mth.nextInt(random, 1, 7)));
        newArrayList.add(new BlockPos((i - 16) + Mth.nextInt(random, 1, 8), 90, i2 + Mth.nextInt(random, 1, 7)));
        newArrayList.add(new BlockPos((i - 16) + Mth.nextInt(random, 1, 8), 90, (i2 - 16) + Mth.nextInt(random, 4, 8)));
        newArrayList.add(new BlockPos(i + Mth.nextInt(random, 1, 7), 90, i2 + 16 + Mth.nextInt(random, 1, 7)));
        newArrayList.add(new BlockPos(i + Mth.nextInt(random, 1, 7), 90, (i2 - 16) + Mth.nextInt(random, 4, 6)));
        newArrayList.add(new BlockPos(i + 16 + Mth.nextInt(random, 1, 7), 90, i2 + 16 + Mth.nextInt(random, 3, 8)));
        newArrayList.add(new BlockPos(i + 16 + Mth.nextInt(random, 1, 7), 90, i2 + Mth.nextInt(random, 1, 7)));
        newArrayList.add(new BlockPos(i + 16 + Mth.nextInt(random, 1, 7), 90, (i2 - 16) + Mth.nextInt(random, 4, 8)));
        return newArrayList;
    }

    private static void addPiece(StructureManager structureManager, BlockPos blockPos, Rotation rotation, List<StructurePiece> list, Random random, OceanRuinConfiguration oceanRuinConfiguration, boolean z, float f) {
        if (oceanRuinConfiguration.biomeTemp == OceanRuinFeature.Type.WARM) {
            list.add(new OceanRuinPiece(structureManager, z ? getBigWarmRuin(random) : getSmallWarmRuin(random), blockPos, rotation, f, oceanRuinConfiguration.biomeTemp, z));
            return;
        }
        if (oceanRuinConfiguration.biomeTemp == OceanRuinFeature.Type.COLD) {
            ResourceLocation[] resourceLocationArr = z ? BIG_RUINS_BRICK : RUINS_BRICK;
            ResourceLocation[] resourceLocationArr2 = z ? BIG_RUINS_CRACKED : RUINS_CRACKED;
            ResourceLocation[] resourceLocationArr3 = z ? BIG_RUINS_MOSSY : RUINS_MOSSY;
            int nextInt = random.nextInt(resourceLocationArr.length);
            list.add(new OceanRuinPiece(structureManager, resourceLocationArr[nextInt], blockPos, rotation, f, oceanRuinConfiguration.biomeTemp, z));
            list.add(new OceanRuinPiece(structureManager, resourceLocationArr2[nextInt], blockPos, rotation, 0.7f, oceanRuinConfiguration.biomeTemp, z));
            list.add(new OceanRuinPiece(structureManager, resourceLocationArr3[nextInt], blockPos, rotation, 0.5f, oceanRuinConfiguration.biomeTemp, z));
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanRuinPieces$OceanRuinPiece.class */
    public static class OceanRuinPiece extends TemplateStructurePiece {
        private final OceanRuinFeature.Type biomeType;
        private final float integrity;
        private final ResourceLocation templateLocation;
        private final Rotation rotation;
        private final boolean isLarge;

        public OceanRuinPiece(StructureManager structureManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, float f, OceanRuinFeature.Type type, boolean z) {
            super(StructurePieceType.OCEAN_RUIN, 0);
            this.templateLocation = resourceLocation;
            this.templatePosition = blockPos;
            this.rotation = rotation;
            this.integrity = f;
            this.biomeType = type;
            this.isLarge = z;
            loadTemplate(structureManager);
        }

        public OceanRuinPiece(StructureManager structureManager, CompoundTag compoundTag) {
            super(StructurePieceType.OCEAN_RUIN, compoundTag);
            this.templateLocation = new ResourceLocation(compoundTag.getString("Template"));
            this.rotation = Rotation.valueOf(compoundTag.getString("Rot"));
            this.integrity = compoundTag.getFloat("Integrity");
            this.biomeType = OceanRuinFeature.Type.valueOf(compoundTag.getString("BiomeType"));
            this.isLarge = compoundTag.getBoolean("IsLarge");
            loadTemplate(structureManager);
        }

        private void loadTemplate(StructureManager structureManager) {
            setup(structureManager.getOrCreate(this.templateLocation), this.templatePosition, new StructurePlaceSettings().setRotation(this.rotation).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR));
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        protected void addAdditionalSaveData(CompoundTag compoundTag) {
            super.addAdditionalSaveData(compoundTag);
            compoundTag.putString("Template", this.templateLocation.toString());
            compoundTag.putString("Rot", this.rotation.name());
            compoundTag.putFloat("Integrity", this.integrity);
            compoundTag.putString("BiomeType", this.biomeType.toString());
            compoundTag.putBoolean("IsLarge", this.isLarge);
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece
        protected void handleDataMarker(String str, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
            if ("chest".equals(str)) {
                serverLevelAccessor.setBlock(blockPos, (BlockState) Blocks.CHEST.defaultBlockState().setValue(ChestBlock.WATERLOGGED, Boolean.valueOf(serverLevelAccessor.getFluidState(blockPos).is(FluidTags.WATER))), 2);
                BlockEntity blockEntity = serverLevelAccessor.getBlockEntity(blockPos);
                if (blockEntity instanceof ChestBlockEntity) {
                    ((ChestBlockEntity) blockEntity).setLootTable(this.isLarge ? BuiltInLootTables.UNDERWATER_RUIN_BIG : BuiltInLootTables.UNDERWATER_RUIN_SMALL, random.nextLong());
                    return;
                }
                return;
            }
            if ("drowned".equals(str)) {
                Drowned create = EntityType.DROWNED.create(serverLevelAccessor.getLevel());
                create.setPersistenceRequired();
                create.moveTo(blockPos, 0.0f, 0.0f);
                create.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(blockPos), MobSpawnType.STRUCTURE, null, null);
                serverLevelAccessor.addFreshEntityWithPassengers(create);
                if (blockPos.getY() > serverLevelAccessor.getSeaLevel()) {
                    serverLevelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                } else {
                    serverLevelAccessor.setBlock(blockPos, Blocks.WATER.defaultBlockState(), 2);
                }
            }
        }

        @Override // net.minecraft.world.level.levelgen.structure.TemplateStructurePiece, net.minecraft.world.level.levelgen.structure.StructurePiece
        public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.placeSettings.clearProcessors().addProcessor(new BlockRotProcessor(this.integrity)).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
            this.templatePosition = new BlockPos(this.templatePosition.getX(), worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.templatePosition.getX(), this.templatePosition.getZ()), this.templatePosition.getZ());
            this.templatePosition = new BlockPos(this.templatePosition.getX(), getHeight(this.templatePosition, worldGenLevel, StructureTemplate.transform(new BlockPos(this.template.getSize().getX() - 1, 0, this.template.getSize().getZ() - 1), Mirror.NONE, this.rotation, BlockPos.ZERO).offset(this.templatePosition)), this.templatePosition.getZ());
            return super.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos);
        }

        private int getHeight(BlockPos var1, BlockGetter var2, BlockPos var3) {
            int var4 = var1.getY();
            int var5 = 512;
            int var6 = var4 - 1;
            int var7 = 0;

            for(BlockPos var9 : BlockPos.betweenClosed(var1, var3)) {
                int var10 = var9.getX();
                int var11 = var9.getZ();
                int var12 = var1.getY() - 1;
                BlockPos.MutableBlockPos var13 = new BlockPos.MutableBlockPos(var10, var12, var11);
                BlockState var14 = var2.getBlockState(var13);

                for(FluidState var15 = var2.getFluidState(var13); (var14.isAir() || var15.is(FluidTags.WATER) || var14.getBlock().is(BlockTags.ICE)) && var12 > 1; var15 = var2.getFluidState(var13)) {
                    --var12;
                    var13.set(var10, var12, var11);
                    var14 = var2.getBlockState(var13);
                }

                var5 = Math.min(var5, var12);
                if (var12 < var6 - 2) {
                    ++var7;
                }
            }

            int var16 = Math.abs(var1.getX() - var3.getX());
            if (var6 - var5 > 2 && var7 > var16 - 2) {
                var4 = var5 + 1;
            }

            return var4;
        }
    }
}
