package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoTickList;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/storage/ChunkSerializer.class */
public class ChunkSerializer {
    private static final Logger LOGGER = LogManager.getLogger();

    public static ProtoChunk read(ServerLevel serverLevel, StructureManager structureManager, PoiManager poiManager, ChunkPos chunkPos, CompoundTag compoundTag) {
        ChunkAccess chunkAccess;
        TickList<Block> tickList;
        TickList<Fluid> tickList2;
        BiomeSource biomeSource = serverLevel.getChunkSource().getGenerator().getBiomeSource();
        CompoundTag compound = compoundTag.getCompound("Level");
        ChunkPos chunkPos2 = new ChunkPos(compound.getInt("xPos"), compound.getInt("zPos"));
        if (!Objects.equals(chunkPos, chunkPos2)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", chunkPos, chunkPos, chunkPos2);
        }
        ChunkBiomeContainer chunkBiomeContainer = new ChunkBiomeContainer(serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), chunkPos, biomeSource, compound.contains("Biomes", 11) ? compound.getIntArray("Biomes") : null);
        UpgradeData upgradeData = compound.contains("UpgradeData", 10) ? new UpgradeData(compound.getCompound("UpgradeData")) : UpgradeData.EMPTY;
        ProtoTickList<Block> protoTickList = new ProtoTickList<>(block -> {
            return block == null || block.defaultBlockState().isAir();
        }, chunkPos, compound.getList("ToBeTicked", 9));
        ProtoTickList<Fluid> protoTickList2 = new ProtoTickList<>(fluid -> {
            return fluid == null || fluid == Fluids.EMPTY;
        }, chunkPos, compound.getList("LiquidsToBeTicked", 9));
        boolean z = compound.getBoolean("isLightOn");
        ListTag list = compound.getList("Sections", 10);
        LevelChunkSection[] levelChunkSectionArr = new LevelChunkSection[16];
        boolean hasSkyLight = serverLevel.dimensionType().hasSkyLight();
        LevelLightEngine lightEngine = serverLevel.getChunkSource().getLightEngine();
        if (z) {
            lightEngine.retainData(chunkPos, true);
        }
        for (int i = 0; i < list.size(); i++) {
            CompoundTag compound2 = list.getCompound(i);
            int i2 = compound2.getByte("Y");
            if (compound2.contains("Palette", 9) && compound2.contains("BlockStates", 12)) {
                LevelChunkSection levelChunkSection = new LevelChunkSection(i2 << 4);
                levelChunkSection.getStates().read(compound2.getList("Palette", 10), compound2.getLongArray("BlockStates"));
                levelChunkSection.recalcBlockCounts();
                if (!levelChunkSection.isEmpty()) {
                    levelChunkSectionArr[i2] = levelChunkSection;
                }
                poiManager.checkConsistencyWithBlocks(chunkPos, levelChunkSection);
            }
            if (z) {
                if (compound2.contains("BlockLight", 7)) {
                    lightEngine.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkPos, i2), new DataLayer(compound2.getByteArray("BlockLight")), true);
                }
                if (hasSkyLight && compound2.contains("SkyLight", 7)) {
                    lightEngine.queueSectionData(LightLayer.SKY, SectionPos.of(chunkPos, i2), new DataLayer(compound2.getByteArray("SkyLight")), true);
                }
            }
        }
        long j = compound.getLong("InhabitedTime");
        ChunkStatus.ChunkType chunkTypeFromTag = getChunkTypeFromTag(compoundTag);
        if (chunkTypeFromTag == ChunkStatus.ChunkType.LEVELCHUNK) {
            if (compound.contains("TileTicks", 9)) {
                tickList = ChunkTickList.create(compound.getList("TileTicks", 10), Registry.BLOCK::getKey, Registry.BLOCK::get);
            } else {
                tickList = protoTickList;
            }
            if (compound.contains("LiquidTicks", 9)) {
                               tickList2 = ChunkTickList.create(compound.getList("LiquidTicks", 10), Registry.FLUID::getKey, Registry.FLUID::get);
            } else {
                tickList2 = protoTickList2;
            }
            chunkAccess = new LevelChunk(serverLevel.getLevel(), chunkPos, chunkBiomeContainer, upgradeData, tickList, tickList2, j, levelChunkSectionArr, levelChunk -> {
                postLoadChunk(compound, levelChunk);
            });
        } else {
            ProtoChunk protoChunk = new ProtoChunk(chunkPos, upgradeData, levelChunkSectionArr, protoTickList, protoTickList2);
            protoChunk.setBiomes(chunkBiomeContainer);
            chunkAccess = protoChunk;
            chunkAccess.setInhabitedTime(j);
            protoChunk.setStatus(ChunkStatus.byName(compound.getString("Status")));
            if (chunkAccess.getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                protoChunk.setLightEngine(lightEngine);
            }
            if (!z && chunkAccess.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
                for (BlockPos blockPos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), 255, chunkPos.getMaxBlockZ())) {
                    if (chunkAccess.getBlockState(blockPos).getLightEmission() != 0) {
                        protoChunk.addLight(blockPos);
                    }
                }
            }
        }
        chunkAccess.setLightCorrect(z);
        CompoundTag compound3 = compound.getCompound("Heightmaps");
        EnumSet<Heightmap.Types> noneOf = EnumSet.noneOf(Heightmap.Types.class);
        Iterator it = chunkAccess.getStatus().heightmapsAfter().iterator();
        while (it.hasNext()) {
            Heightmap.Types types = (Heightmap.Types) it.next();
            String serializationKey = types.getSerializationKey();
            if (compound3.contains(serializationKey, 12)) {
                chunkAccess.setHeightmap(types, compound3.getLongArray(serializationKey));
            } else {
                noneOf.add(types);
            }
        }
        Heightmap.primeHeightmaps(chunkAccess, noneOf);
        CompoundTag compound4 = compound.getCompound("Structures");
        chunkAccess.setAllStarts(unpackStructureStart(structureManager, compound4, serverLevel.getSeed()));
        chunkAccess.setAllReferences(unpackStructureReferences(chunkPos, compound4));
        if (compound.getBoolean("shouldSave")) {
            chunkAccess.setUnsaved(true);
        }
        ListTag list4 = compound.getList("PostProcessing", 9);
        for (int i3 = 0; i3 < list4.size(); i3++) {
            ListTag list5 = list4.getList(i3);
            for (int i4 = 0; i4 < list5.size(); i4++) {
                chunkAccess.addPackedPostProcess(list5.getShort(i4), i3);
            }
        }
        if (chunkTypeFromTag == ChunkStatus.ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk) chunkAccess);
        }
        ProtoChunk protoChunk2 = (ProtoChunk) chunkAccess;
        ListTag list6 = compound.getList("Entities", 10);
        for (int i5 = 0; i5 < list6.size(); i5++) {
            protoChunk2.addEntity(list6.getCompound(i5));
        }
        ListTag list7 = compound.getList("TileEntities", 10);
        for (int i6 = 0; i6 < list7.size(); i6++) {
            chunkAccess.setBlockEntityNbt(list7.getCompound(i6));
        }
        ListTag list8 = compound.getList("Lights", 9);
        for (int i7 = 0; i7 < list8.size(); i7++) {
            ListTag list9 = list8.getList(i7);
            for (int i8 = 0; i8 < list9.size(); i8++) {
                protoChunk2.addLight(list9.getShort(i8), i7);
            }
        }
        CompoundTag compound5 = compound.getCompound("CarvingMasks");
        for (String str : compound5.getAllKeys()) {
            protoChunk2.setCarvingMask(GenerationStep.Carving.valueOf(str), BitSet.valueOf(compound5.getByteArray(str)));
        }
        return protoChunk2;
    }

    public static CompoundTag write(ServerLevel serverLevel, ChunkAccess chunkAccess) {
        ChunkPos pos = chunkAccess.getPos();
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        compoundTag.put("Level", compoundTag2);
        compoundTag2.putInt("xPos", pos.x);
        compoundTag2.putInt("zPos", pos.z);
        compoundTag2.putLong("LastUpdate", serverLevel.getGameTime());
        compoundTag2.putLong("InhabitedTime", chunkAccess.getInhabitedTime());
        compoundTag2.putString("Status", chunkAccess.getStatus().getName());
        UpgradeData upgradeData = chunkAccess.getUpgradeData();
        if (!upgradeData.isEmpty()) {
            compoundTag2.put("UpgradeData", upgradeData.write());
        }
        LevelChunkSection[] sections = chunkAccess.getSections();
        ListTag listTag = new ListTag();
        LevelLightEngine lightEngine = serverLevel.getChunkSource().getLightEngine();
        boolean isLightCorrect = chunkAccess.isLightCorrect();
        for (int i = -1; i < 17; i++) {
            int i2 = i;
            LevelChunkSection levelChunkSection = (LevelChunkSection) Arrays.stream(sections).filter(levelChunkSection2 -> {
                return levelChunkSection2 != null && (levelChunkSection2.bottomBlockY() >> 4) == i2;
            }).findFirst().orElse(LevelChunk.EMPTY_SECTION);
            DataLayer dataLayerData = lightEngine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(pos, i2));
            DataLayer dataLayerData2 = lightEngine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(pos, i2));
            if (levelChunkSection != LevelChunk.EMPTY_SECTION || dataLayerData != null || dataLayerData2 != null) {
                CompoundTag compoundTag3 = new CompoundTag();
                compoundTag3.putByte("Y", (byte) (i2 & 255));
                if (levelChunkSection != LevelChunk.EMPTY_SECTION) {
                    levelChunkSection.getStates().write(compoundTag3, "Palette", "BlockStates");
                }
                if (dataLayerData != null && !dataLayerData.isEmpty()) {
                    compoundTag3.putByteArray("BlockLight", dataLayerData.getData());
                }
                if (dataLayerData2 != null && !dataLayerData2.isEmpty()) {
                    compoundTag3.putByteArray("SkyLight", dataLayerData2.getData());
                }
                listTag.add(compoundTag3);
            }
        }
        compoundTag2.put("Sections", listTag);
        if (isLightCorrect) {
            compoundTag2.putBoolean("isLightOn", true);
        }
        ChunkBiomeContainer biomes = chunkAccess.getBiomes();
        if (biomes != null) {
            compoundTag2.putIntArray("Biomes", biomes.writeBiomes());
        }
        ListTag listTag2 = new ListTag();
        Iterator<BlockPos> it = chunkAccess.getBlockEntitiesPos().iterator();
        while (it.hasNext()) {
            CompoundTag blockEntityNbtForSaving = chunkAccess.getBlockEntityNbtForSaving(it.next());
            if (blockEntityNbtForSaving != null) {
                listTag2.add(blockEntityNbtForSaving);
            }
        }
        compoundTag2.put("TileEntities", listTag2);
        ListTag listTag3 = new ListTag();
        if (chunkAccess.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
            LevelChunk levelChunk = (LevelChunk) chunkAccess;
            levelChunk.setLastSaveHadEntities(false);
            for (int i3 = 0; i3 < levelChunk.getEntitySections().length; i3++) {
                Iterator<Entity> it2 = levelChunk.getEntitySections()[i3].iterator();
                while (it2.hasNext()) {
                    Entity next = it2.next();
                    CompoundTag compoundTag4 = new CompoundTag();
                    if (next.save(compoundTag4)) {
                        levelChunk.setLastSaveHadEntities(true);
                        listTag3.add(compoundTag4);
                    }
                }
            }
        } else {
            ProtoChunk protoChunk = (ProtoChunk) chunkAccess;
            listTag3.addAll(protoChunk.getEntities());
            compoundTag2.put("Lights", packOffsets(protoChunk.getPackedLights()));
            CompoundTag compoundTag5 = new CompoundTag();
            for (GenerationStep.Carving carving : GenerationStep.Carving.values()) {
                BitSet carvingMask = protoChunk.getCarvingMask(carving);
                if (carvingMask != null) {
                    compoundTag5.putByteArray(carving.toString(), carvingMask.toByteArray());
                }
            }
            compoundTag2.put("CarvingMasks", compoundTag5);
        }
        compoundTag2.put("Entities", listTag3);
        TickList<Block> blockTicks = chunkAccess.getBlockTicks();
        if (blockTicks instanceof ProtoTickList) {
            compoundTag2.put("ToBeTicked", ((ProtoTickList) blockTicks).save());
        } else if (blockTicks instanceof ChunkTickList) {
            compoundTag2.put("TileTicks", ((ChunkTickList) blockTicks).save());
        } else {
            compoundTag2.put("TileTicks", serverLevel.getBlockTicks().save(pos));
        }
        TickList<Fluid> liquidTicks = chunkAccess.getLiquidTicks();
        if (liquidTicks instanceof ProtoTickList) {
            compoundTag2.put("LiquidsToBeTicked", ((ProtoTickList) liquidTicks).save());
        } else if (liquidTicks instanceof ChunkTickList) {
            compoundTag2.put("LiquidTicks", ((ChunkTickList) liquidTicks).save());
        } else {
            compoundTag2.put("LiquidTicks", serverLevel.getLiquidTicks().save(pos));
        }
        compoundTag2.put("PostProcessing", packOffsets(chunkAccess.getPostProcessing()));
        CompoundTag compoundTag6 = new CompoundTag();
        for (Map.Entry<Heightmap.Types, Heightmap> entry : chunkAccess.getHeightmaps()) {
            if (chunkAccess.getStatus().heightmapsAfter().contains(entry.getKey())) {
                compoundTag6.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
            }
        }
        compoundTag2.put("Heightmaps", compoundTag6);
        compoundTag2.put("Structures", packStructureData(pos, chunkAccess.getAllStarts(), chunkAccess.getAllReferences()));
        return compoundTag;
    }

    public static ChunkStatus.ChunkType getChunkTypeFromTag(@Nullable CompoundTag compoundTag) {
        ChunkStatus byName;
        if (compoundTag != null && (byName = ChunkStatus.byName(compoundTag.getCompound("Level").getString("Status"))) != null) {
            return byName.getChunkType();
        }
        return ChunkStatus.ChunkType.PROTOCHUNK;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void postLoadChunk(CompoundTag compoundTag, LevelChunk levelChunk) {
        ListTag list = compoundTag.getList("Entities", 10);
        Level level = levelChunk.getLevel();
        for (int i = 0; i < list.size(); i++) {
            EntityType.loadEntityRecursive(list.getCompound(i), level, entity -> {
                levelChunk.addEntity(entity);
                return entity;
            });
            levelChunk.setLastSaveHadEntities(true);
        }
        ListTag list2 = compoundTag.getList("TileEntities", 10);
        for (int i2 = 0; i2 < list2.size(); i2++) {
            CompoundTag compound = list2.getCompound(i2);
            if (compound.getBoolean("keepPacked")) {
                levelChunk.setBlockEntityNbt(compound);
            } else {
                BlockEntity loadStatic = BlockEntity.loadStatic(levelChunk.getBlockState(new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"))), compound);
                if (loadStatic != null) {
                    levelChunk.addBlockEntity(loadStatic);
                }
            }
        }
    }

    private static CompoundTag packStructureData(ChunkPos chunkPos, Map<StructureFeature<?>, StructureStart<?>> map, Map<StructureFeature<?>, LongSet> map2) {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag compoundTag2 = new CompoundTag();
        for (Map.Entry<StructureFeature<?>, StructureStart<?>> entry : map.entrySet()) {
            compoundTag2.put(entry.getKey().getFeatureName(), entry.getValue().createTag(chunkPos.x, chunkPos.z));
        }
        compoundTag.put("Starts", compoundTag2);
        CompoundTag compoundTag3 = new CompoundTag();
        for (Map.Entry<StructureFeature<?>, LongSet> entry2 : map2.entrySet()) {
            compoundTag3.put(entry2.getKey().getFeatureName(), new LongArrayTag(entry2.getValue()));
        }
        compoundTag.put("References", compoundTag3);
        return compoundTag;
    }

    private static Map<StructureFeature<?>, StructureStart<?>> unpackStructureStart(StructureManager structureManager, CompoundTag compoundTag, long j) {
        Map<StructureFeature<?>, StructureStart<?>> newHashMap = Maps.newHashMap();
        CompoundTag compound = compoundTag.getCompound("Starts");
        for (String str : compound.getAllKeys()) {
            String lowerCase = str.toLowerCase(Locale.ROOT);
            StructureFeature<?> structureFeature = (StructureFeature) StructureFeature.STRUCTURES_REGISTRY.get(lowerCase);
            if (structureFeature == null) {
                LOGGER.error("Unknown structure start: {}", lowerCase);
            } else {
                StructureStart<?> loadStaticStart = StructureFeature.loadStaticStart(structureManager, compound.getCompound(str), j);
                if (loadStaticStart != null) {
                    newHashMap.put(structureFeature, loadStaticStart);
                }
            }
        }
        return newHashMap;
    }

    private static Map<StructureFeature<?>, LongSet> unpackStructureReferences(ChunkPos chunkPos, CompoundTag compoundTag) {
        HashMap newHashMap = Maps.newHashMap();
        CompoundTag compound = compoundTag.getCompound("References");
        for (String str : compound.getAllKeys()) {
            newHashMap.put(StructureFeature.STRUCTURES_REGISTRY.get(str.toLowerCase(Locale.ROOT)), new LongOpenHashSet(Arrays.stream(compound.getLongArray(str)).filter(j -> {
                ChunkPos chunkPos2 = new ChunkPos(j);
                if (chunkPos2.getChessboardDistance(chunkPos) > 8) {
                    LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", str, chunkPos2, chunkPos);
                    return false;
                }
                return true;
            }).toArray()));
        }
        return newHashMap;
    }

    public static ListTag packOffsets(ShortList[] shortListArr) {
        ListTag listTag = new ListTag();
        for (ShortList shortList : shortListArr) {
            ListTag listTag2 = new ListTag();
            if (shortList != null) {
                ShortListIterator it = shortList.iterator();
                while (it.hasNext()) {
                    listTag2.add(ShortTag.valueOf(((Short) it.next()).shortValue()));
                }
            }
            listTag.add(listTag2);
        }
        return listTag;
    }
}
