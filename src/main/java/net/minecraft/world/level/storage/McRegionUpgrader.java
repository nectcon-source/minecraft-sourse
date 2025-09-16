package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.storage.OldChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/McRegionUpgrader.class */
public class McRegionUpgrader {
    private static final Logger LOGGER = LogManager.getLogger();

    static boolean convertLevel(LevelStorageSource.LevelStorageAccess levelStorageAccess, ProgressListener progressListener) {
        BiomeSource overworldBiomeSource;
        progressListener.progressStagePercentage(0);
        List<File> newArrayList = Lists.newArrayList();
        List<File> newArrayList2 = Lists.newArrayList();
        List<File> newArrayList3 = Lists.newArrayList();
        File dimensionPath = levelStorageAccess.getDimensionPath(Level.OVERWORLD);
        File dimensionPath2 = levelStorageAccess.getDimensionPath(Level.NETHER);
        File dimensionPath3 = levelStorageAccess.getDimensionPath(Level.END);
        LOGGER.info("Scanning folders...");
        addRegionFiles(dimensionPath, newArrayList);
        if (dimensionPath2.exists()) {
            addRegionFiles(dimensionPath2, newArrayList2);
        }
        if (dimensionPath3.exists()) {
            addRegionFiles(dimensionPath3, newArrayList3);
        }
        int size = newArrayList.size() + newArrayList2.size() + newArrayList3.size();
        LOGGER.info("Total conversion count is {}", Integer.valueOf(size));
        RegistryAccess.RegistryHolder builtin = RegistryAccess.builtin();
        WorldData dataTag = levelStorageAccess.getDataTag(RegistryReadOps.create(NbtOps.INSTANCE, ResourceManager.Empty.INSTANCE, builtin), DataPackConfig.DEFAULT);
        long seed = dataTag != null ? dataTag.worldGenSettings().seed() : 0L;
        Registry<Biome> registryOrThrow = builtin.registryOrThrow(Registry.BIOME_REGISTRY);
        if (dataTag != null && dataTag.worldGenSettings().isFlatWorld()) {
            overworldBiomeSource = new FixedBiomeSource(registryOrThrow.getOrThrow(Biomes.PLAINS));
        } else {
            overworldBiomeSource = new OverworldBiomeSource(seed, false, false, registryOrThrow);
        }
        convertRegions(builtin, new File(dimensionPath, "region"), newArrayList, overworldBiomeSource, 0, size, progressListener);
        convertRegions(builtin, new File(dimensionPath2, "region"), newArrayList2, new FixedBiomeSource(registryOrThrow.getOrThrow(Biomes.NETHER_WASTES)), newArrayList.size(), size, progressListener);
        convertRegions(builtin, new File(dimensionPath3, "region"), newArrayList3, new FixedBiomeSource(registryOrThrow.getOrThrow(Biomes.THE_END)), newArrayList.size() + newArrayList2.size(), size, progressListener);
        makeMcrLevelDatBackup(levelStorageAccess);
        levelStorageAccess.saveDataTag(builtin, dataTag);
        return true;
    }

    private static void makeMcrLevelDatBackup(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        File file = levelStorageAccess.getLevelPath(LevelResource.LEVEL_DATA_FILE).toFile();
        if (!file.exists()) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
        } else if (!file.renameTo(new File(file.getParent(), "level.dat_mcr"))) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
        }
    }

    private static void convertRegions(RegistryAccess.RegistryHolder registryHolder, File file, Iterable<File> iterable, BiomeSource biomeSource, int i, int i2, ProgressListener progressListener) {
        Iterator<File> it = iterable.iterator();
        while (it.hasNext()) {
            convertRegion(registryHolder, file, it.next(), biomeSource, i, i2, progressListener);
            i++;
            progressListener.progressStagePercentage((int) Math.round((100.0d * i) / i2));
        }
    }

    /* JADX WARN: Failed to apply debug info
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.applyWithWiderIgnoreUnknown(TypeUpdate.java:74)
    	at jadx.core.dex.visitors.debuginfo.DebugInfoApplyVisitor.applyDebugInfo(DebugInfoApplyVisitor.java:137)
    	at jadx.core.dex.visitors.debuginfo.DebugInfoApplyVisitor.applyDebugInfo(DebugInfoApplyVisitor.java:133)
    	at jadx.core.dex.visitors.debuginfo.DebugInfoApplyVisitor.searchAndApplyVarDebugInfo(DebugInfoApplyVisitor.java:75)
    	at jadx.core.dex.visitors.debuginfo.DebugInfoApplyVisitor.lambda$applyDebugInfo$0(DebugInfoApplyVisitor.java:68)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
    	at jadx.core.dex.visitors.debuginfo.DebugInfoApplyVisitor.applyDebugInfo(DebugInfoApplyVisitor.java:68)
    	at jadx.core.dex.visitors.debuginfo.DebugInfoApplyVisitor.visit(DebugInfoApplyVisitor.java:55)
     */
    /* JADX WARN: Failed to calculate best type for var: r19v0 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.calculateFromBounds(FixTypesVisitor.java:156)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.setBestType(FixTypesVisitor.java:133)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.deduceType(FixTypesVisitor.java:238)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryDeduceTypes(FixTypesVisitor.java:221)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:91)
     */
    /* JADX WARN: Failed to calculate best type for var: r19v0 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Failed to calculate best type for var: r20v0 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.calculateFromBounds(FixTypesVisitor.java:156)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.setBestType(FixTypesVisitor.java:133)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.deduceType(FixTypesVisitor.java:238)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.tryDeduceTypes(FixTypesVisitor.java:221)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:91)
     */
    /* JADX WARN: Failed to calculate best type for var: r20v0 ??
    java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.InsnArg.getType()" because "changeArg" is null
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.moveListener(TypeUpdate.java:439)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.runListeners(TypeUpdate.java:232)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.requestUpdate(TypeUpdate.java:212)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeForSsaVar(TypeUpdate.java:183)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.updateTypeChecked(TypeUpdate.java:112)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:83)
    	at jadx.core.dex.visitors.typeinference.TypeUpdate.apply(TypeUpdate.java:56)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.calculateFromBounds(TypeInferenceVisitor.java:145)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.setBestType(TypeInferenceVisitor.java:123)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.lambda$runTypePropagation$2(TypeInferenceVisitor.java:101)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.runTypePropagation(TypeInferenceVisitor.java:101)
    	at jadx.core.dex.visitors.typeinference.TypeInferenceVisitor.visit(TypeInferenceVisitor.java:75)
     */
    /* JADX WARN: Finally extract failed */
    /* JADX WARN: Multi-variable type inference failed. Error: java.lang.NullPointerException: Cannot invoke "jadx.core.dex.instructions.args.RegisterArg.getSVar()" because the return value of "jadx.core.dex.nodes.InsnNode.getResult()" is null
    	at jadx.core.dex.visitors.typeinference.AbstractTypeConstraint.collectRelatedVars(AbstractTypeConstraint.java:31)
    	at jadx.core.dex.visitors.typeinference.AbstractTypeConstraint.<init>(AbstractTypeConstraint.java:19)
    	at jadx.core.dex.visitors.typeinference.TypeSearch$1.<init>(TypeSearch.java:376)
    	at jadx.core.dex.visitors.typeinference.TypeSearch.makeMoveConstraint(TypeSearch.java:376)
    	at jadx.core.dex.visitors.typeinference.TypeSearch.makeConstraint(TypeSearch.java:361)
    	at jadx.core.dex.visitors.typeinference.TypeSearch.collectConstraints(TypeSearch.java:341)
    	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
    	at jadx.core.dex.visitors.typeinference.TypeSearch.run(TypeSearch.java:60)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.runMultiVariableSearch(FixTypesVisitor.java:116)
    	at jadx.core.dex.visitors.typeinference.FixTypesVisitor.visit(FixTypesVisitor.java:91)
     */
    /* JADX WARN: Not initialized variable reg: 19, insn: 0x02bd: MOVE (r0 I:??[int, float, boolean, short, byte, char, OBJECT, ARRAY]) = 
      (r19 I:??[int, float, boolean, short, byte, char, OBJECT, ARRAY] A[D('☃' net.minecraft.world.level.chunk.storage.RegionFile)])
     A[TRY_LEAVE], block:B:157:0x02bd */
    /* JADX WARN: Not initialized variable reg: 20, insn: 0x02c2: MOVE (r0 I:??[int, float, boolean, short, byte, char, OBJECT, ARRAY]) = (r20 I:??[int, float, boolean, short, byte, char, OBJECT, ARRAY]), block:B:159:0x02c2 */
    /* JADX WARN: Type inference failed for: r19v0, names: [☃], types: [net.minecraft.world.level.chunk.storage.RegionFile] */
    /* JADX WARN: Type inference failed for: r20v0, types: [java.lang.Throwable] */
    private static void convertRegion(RegistryAccess.RegistryHolder registryHolder, File file, File file2, BiomeSource biomeSource, int i, int i2, ProgressListener progressListener) {
        String name = file2.getName();
        try {
            try {
                RegionFile regionFile = new RegionFile(file2, file, true);
                Throwable th = null;
                RegionFile regionFile2 = new RegionFile(new File(file, name.substring(0, name.length() - ".mcr".length()) + ".mca"), file, true);
                Throwable th2 = null;
                for (int i3 = 0; i3 < 32; i3++) {
                    for (int i4 = 0; i4 < 32; i4++) {
                        try {
                            ChunkPos chunkPos = new ChunkPos(i3, i4);
                            if (regionFile.hasChunk(chunkPos) && !regionFile2.hasChunk(chunkPos)) {
                                try {
                                    DataInputStream chunkDataInputStream = regionFile.getChunkDataInputStream(chunkPos);
                                    Throwable th3 = null;
                                    if (chunkDataInputStream == null) {
                                        try {
                                            try {
                                                LOGGER.warn("Failed to fetch input stream for chunk {}", chunkPos);
                                                if (chunkDataInputStream != null) {
                                                    if (0 != 0) {
                                                        try {
                                                            chunkDataInputStream.close();
                                                        } catch (Throwable th4) {
                                                            th3.addSuppressed(th4);
                                                        }
                                                    } else {
                                                        chunkDataInputStream.close();
                                                    }
                                                }
                                            } catch (Throwable th5) {
                                                th3 = th5;
                                                throw th5;
                                            }
                                        } catch (Throwable th6) {
                                            if (chunkDataInputStream != null) {
                                                if (th3 != null) {
                                                    try {
                                                        chunkDataInputStream.close();
                                                    } catch (Throwable th7) {
                                                        th3.addSuppressed(th7);
                                                    }
                                                } else {
                                                    chunkDataInputStream.close();
                                                }
                                            }
                                            throw th6;
                                        }
                                    } else {
                                        CompoundTag read = NbtIo.read(chunkDataInputStream);
                                        if (chunkDataInputStream != null) {
                                            if (0 != 0) {
                                                try {
                                                    chunkDataInputStream.close();
                                                } catch (Throwable th8) {
                                                    th3.addSuppressed(th8);
                                                }
                                            } else {
                                                chunkDataInputStream.close();
                                            }
                                        }
                                        OldChunkStorage.OldLevelChunk load = OldChunkStorage.load(read.getCompound("Level"));
                                        CompoundTag compoundTag = new CompoundTag();
                                        CompoundTag compoundTag2 = new CompoundTag();
                                        compoundTag.put("Level", compoundTag2);
                                        OldChunkStorage.convertToAnvilFormat(registryHolder, load, compoundTag2, biomeSource);
                                        DataOutputStream chunkDataOutputStream = regionFile2.getChunkDataOutputStream(chunkPos);
                                        Throwable th9 = null;
                                        try {
                                            try {
                                                NbtIo.write(compoundTag, chunkDataOutputStream);
                                                if (chunkDataOutputStream != null) {
                                                    if (0 != 0) {
                                                        try {
                                                            chunkDataOutputStream.close();
                                                        } catch (Throwable th10) {
                                                            th9.addSuppressed(th10);
                                                        }
                                                    } else {
                                                        chunkDataOutputStream.close();
                                                    }
                                                }
                                            } catch (Throwable th11) {
                                                th9 = th11;
                                                throw th11;
                                            }
                                        } catch (Throwable th12) {
                                            if (chunkDataOutputStream != null) {
                                                if (th9 != null) {
                                                    try {
                                                        chunkDataOutputStream.close();
                                                    } catch (Throwable th13) {
                                                        th9.addSuppressed(th13);
                                                    }
                                                } else {
                                                    chunkDataOutputStream.close();
                                                }
                                            }
                                            throw th12;
                                        }
                                    }
                                } catch (IOException e) {
                                    LOGGER.warn("Failed to read data for chunk {}", chunkPos, e);
                                }
                            }
                        } catch (Throwable th14) {
                            if (regionFile2 != null) {
                                if (0 != 0) {
                                    try {
                                        regionFile2.close();
                                    } catch (Throwable th15) {
                                        th2.addSuppressed(th15);
                                    }
                                } else {
                                    regionFile2.close();
                                }
                            }
                            throw th14;
                        }
                    }
                    int round = (int) Math.round((100.0d * (i * 1024)) / (i2 * 1024));
                    int round2 = (int) Math.round((100.0d * (((i3 + 1) * 32) + (i * 1024))) / (i2 * 1024));
                    if (round2 > round) {
                        progressListener.progressStagePercentage(round2);
                    }
                }
                if (regionFile2 != null) {
                    if (0 != 0) {
                        try {
                            regionFile2.close();
                        } catch (Throwable th16) {
                            th2.addSuppressed(th16);
                        }
                    } else {
                        regionFile2.close();
                    }
                }
                if (regionFile != null) {
                    if (0 != 0) {
                        try {
                            regionFile.close();
                        } catch (Throwable th17) {
                            th.addSuppressed(th17);
                        }
                    } else {
                        regionFile.close();
                    }
                }
            } catch (IOException e2) {
                LOGGER.error("Failed to upgrade region file {}", file2, e2);
            }
        } finally {
        }
    }

    private static void addRegionFiles(File file, Collection<File> collection) {
        File[] listFiles = new File(file, "region").listFiles((file2, str) -> {
            return str.endsWith(".mcr");
        });
        if (listFiles != null) {
            Collections.addAll(collection, listFiles);
        }
    }
}
