package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/LevelStorageSource.class */
public class LevelStorageSource {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral('-').appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('-').appendValue(ChronoField.DAY_OF_MONTH, 2).appendLiteral('_').appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral('-').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral('-').appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();
    private static final ImmutableList<String> OLD_SETTINGS_KEYS = ImmutableList.of("RandomSeed", "generatorName", "generatorOptions", "generatorVersion", "legacy_custom_options", "MapFeatures", "BonusChest");
    private final Path baseDir;
    private final Path backupDir;
    private final DataFixer fixerUpper;

    public LevelStorageSource(Path path, Path path2, DataFixer dataFixer) {
        this.fixerUpper = dataFixer;
        try {
            Files.createDirectories(Files.exists(path, new LinkOption[0]) ? path.toRealPath(new LinkOption[0]) : path, new FileAttribute[0]);
            this.baseDir = path;
            this.backupDir = path2;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static LevelStorageSource createDefault(Path path) {
        return new LevelStorageSource(path, path.resolve("../backups"), DataFixers.getDataFixer());
    }

    private static <T> Pair<WorldGenSettings, Lifecycle> readWorldGenSettings(Dynamic<T> dynamic, DataFixer dataFixer, int i) {

        Dynamic<T> var3 = dynamic.get("WorldGenSettings").orElseEmptyMap();
        UnmodifiableIterator var4 = OLD_SETTINGS_KEYS.iterator();

        while(var4.hasNext()) {
            String var5x = (String)var4.next();
            Optional<? extends Dynamic<?>> var6xx = dynamic.get(var5x).result();
            if (var6xx.isPresent()) {
                var3 = var3.set(var5x, (Dynamic)var6xx.get());
            }
        }

        Dynamic<T> var7x = dataFixer.update(References.WORLD_GEN_SETTINGS, var3, i, SharedConstants.getCurrentVersion().getWorldVersion());
        DataResult<WorldGenSettings> var8xx = WorldGenSettings.CODEC.parse(var7x);
        return Pair.of(
                var8xx.resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error))
                        .orElseGet(
                                () -> {
                                    Registry<DimensionType> var1x = RegistryLookupCodec.create(Registry.DIMENSION_TYPE_REGISTRY)
                                            .codec()
                                            .parse(var7x)
                                            .resultOrPartial(Util.prefix("Dimension type registry: ", LOGGER::error))
                                            .orElseThrow(() -> new IllegalStateException("Failed to get dimension registry"));
                                    Registry<Biome> var2x = RegistryLookupCodec.create(Registry.BIOME_REGISTRY)
                                            .codec()
                                            .parse(var7x)
                                            .resultOrPartial(Util.prefix("Biome registry: ", LOGGER::error))
                                            .orElseThrow(() -> new IllegalStateException("Failed to get biome registry"));
                                    Registry<NoiseGeneratorSettings> var3xx = RegistryLookupCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY)
                                            .codec()
                                            .parse(var7x)
                                            .resultOrPartial(Util.prefix("Noise settings registry: ", LOGGER::error))
                                            .orElseThrow(() -> new IllegalStateException("Failed to get noise settings registry"));
                                    return WorldGenSettings.makeDefault(var1x, var2x, var3xx);
                                }
                        ),
                var8xx.lifecycle()
        );
    }

    private static DataPackConfig readDataPackConfig(Dynamic<?> dynamic) {
        return  DataPackConfig.CODEC.parse(dynamic).resultOrPartial(LOGGER::error).orElse(DataPackConfig.DEFAULT);
    }

    public List<LevelSummary> getLevelList() throws LevelStorageException {
        if (!Files.isDirectory(this.baseDir, new LinkOption[0])) {
            throw new LevelStorageException(new TranslatableComponent("selectWorld.load_folder_access").getString());
        }
        List<LevelSummary> newArrayList = Lists.newArrayList();
        for (File file : this.baseDir.toFile().listFiles()) {
            if (file.isDirectory()) {
                try {
                    LevelSummary levelSummary = (LevelSummary) readLevelData(file, levelSummaryReader(file, DirectoryLock.isLocked(file.toPath())));
                    if (levelSummary != null) {
                        newArrayList.add(levelSummary);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to read {} lock", file, e);
                }
            }
        }
        return newArrayList;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getStorageVersion() {
        return 19133;
    }

    /* JADX INFO: Access modifiers changed from: private */
    @Nullable
    public <T> T readLevelData(File file, BiFunction<File, DataFixer, T> biFunction) {
        T apply;
        if (!file.exists()) {
            return null;
        }
        File file2 = new File(file, "level.dat");
        if (file2.exists() && (apply = biFunction.apply(file2, this.fixerUpper)) != null) {
            return apply;
        }
        File file3 = new File(file, "level.dat_old");
        if (file3.exists()) {
            return biFunction.apply(file3, this.fixerUpper);
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    @Nullable
    public static DataPackConfig getDataPacks(File file, DataFixer dataFixer) {
        try {
            CompoundTag var2 = NbtIo.readCompressed(file);
            CompoundTag var3x = var2.getCompound("Data");
            var3x.remove("Player");
            int var4xx = var3x.contains("DataVersion", 99) ? var3x.getInt("DataVersion") : -1;
            Dynamic<Tag> var4xxx = dataFixer.update(
                    DataFixTypes.LEVEL.getType(), new Dynamic(NbtOps.INSTANCE, var3x), var4xx, SharedConstants.getCurrentVersion().getWorldVersion()
            );
            return var4xxx.get("DataPacks").result().map(LevelStorageSource::readDataPackConfig).orElse(DataPackConfig.DEFAULT);
        } catch (Exception var6) {
            LOGGER.error("Exception reading {}", file, var6);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static BiFunction<File, DataFixer, PrimaryLevelData> getLevelData(DynamicOps<Tag> dynamicOps, DataPackConfig dataPackConfig) {
        return (file, dataFixer) -> {
            try {
                CompoundTag compound = NbtIo.readCompressed(file).getCompound("Data");
                CompoundTag compound2 = compound.contains("Player", 10) ? compound.getCompound("Player") : null;
                compound.remove("Player");
                int i = compound.contains("DataVersion", 99) ? compound.getInt("DataVersion") : -1;
                Dynamic<Tag> update = dataFixer.update(DataFixTypes.LEVEL.getType(), new Dynamic(dynamicOps, compound), i, SharedConstants.getCurrentVersion().getWorldVersion());
                Pair<WorldGenSettings, Lifecycle> readWorldGenSettings = readWorldGenSettings(update, dataFixer, i);
                return PrimaryLevelData.parse(update, dataFixer, i, compound2, LevelSettings.parse(update, dataPackConfig), LevelVersion.parse(update), (WorldGenSettings) readWorldGenSettings.getFirst(), (Lifecycle) readWorldGenSettings.getSecond());
            } catch (Exception e) {
                LOGGER.error("Exception reading {}", file, e);
                return null;
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public BiFunction<File, DataFixer, LevelSummary> levelSummaryReader(File file, boolean z) {
        return (file2, dataFixer) -> {
            try {
                CompoundTag compound = NbtIo.readCompressed(file2).getCompound("Data");
                compound.remove("Player");
                Dynamic<Tag> update = dataFixer.update(DataFixTypes.LEVEL.getType(), new Dynamic(NbtOps.INSTANCE, compound), compound.contains("DataVersion", 99) ? compound.getInt("DataVersion") : -1, SharedConstants.getCurrentVersion().getWorldVersion());
                LevelVersion parse = LevelVersion.parse(update);
                int levelDataVersion = parse.levelDataVersion();
                if (levelDataVersion == 19132 || levelDataVersion == 19133) {
                    return new LevelSummary(LevelSettings.parse(update, (DataPackConfig) update.get("DataPacks").result().map(LevelStorageSource::readDataPackConfig).orElse(DataPackConfig.DEFAULT)), parse, file.getName(), levelDataVersion != getStorageVersion(), z, new File(file, "icon.png"));
                }
                return null;
            } catch (Exception e) {
                LOGGER.error("Exception reading {}", file2, e);
                return null;
            }
        };
    }

    public boolean isNewLevelIdAcceptable(String str) {
        try {
            Path resolve = this.baseDir.resolve(str);
            Files.createDirectory(resolve, new FileAttribute[0]);
            Files.deleteIfExists(resolve);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean levelExists(String str) {
        return Files.isDirectory(this.baseDir.resolve(str), new LinkOption[0]);
    }

    public Path getBaseDir() {
        return this.baseDir;
    }

    public Path getBackupPath() {
        return this.backupDir;
    }

    public LevelStorageAccess createAccess(String str) throws IOException {
        return new LevelStorageAccess(str);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess.class */
    public class LevelStorageAccess implements AutoCloseable {
        private final DirectoryLock lock;
        private final Path levelPath;
        private final String levelId;
        private final Map<LevelResource, Path> resources = Maps.newHashMap();

        public LevelStorageAccess(String str) throws IOException {
            this.levelId = str;
            this.levelPath = LevelStorageSource.this.baseDir.resolve(str);
            this.lock = DirectoryLock.create(this.levelPath);
        }

        public String getLevelId() {
            return this.levelId;
        }

        public Path getLevelPath(LevelResource levelResource) {
            return this.resources.computeIfAbsent(levelResource, levelResource2 -> {
                return this.levelPath.resolve(levelResource2.getId());
            });
        }

        public File getDimensionPath(ResourceKey<Level> resourceKey) {
            return DimensionType.getStorageFolder(resourceKey, this.levelPath.toFile());
        }

        private void checkLock() {
            if (!this.lock.isValid()) {
                throw new IllegalStateException("Lock is no longer valid");
            }
        }

        public PlayerDataStorage createPlayerStorage() {
            checkLock();
            return new PlayerDataStorage(this, LevelStorageSource.this.fixerUpper);
        }

        public boolean requiresConversion() {
            LevelSummary summary = getSummary();
            return (summary == null || summary.levelVersion().levelDataVersion() == LevelStorageSource.this.getStorageVersion()) ? false : true;
        }

        public boolean convertLevel(ProgressListener progressListener) {
            checkLock();
            return McRegionUpgrader.convertLevel(this, progressListener);
        }

        @Nullable
        public LevelSummary getSummary() {
            checkLock();
            return (LevelSummary) LevelStorageSource.this.readLevelData(this.levelPath.toFile(), LevelStorageSource.this.levelSummaryReader(this.levelPath.toFile(), false));
        }

        @Nullable
        public WorldData getDataTag(DynamicOps<Tag> dynamicOps, DataPackConfig dataPackConfig) {
            checkLock();
            return (WorldData) LevelStorageSource.this.readLevelData(this.levelPath.toFile(), LevelStorageSource.getLevelData(dynamicOps, dataPackConfig));
        }

        @Nullable
        public DataPackConfig getDataPacks() {
            checkLock();
            return (DataPackConfig) LevelStorageSource.this.readLevelData(this.levelPath.toFile(), (file, dataFixer) -> {
                return LevelStorageSource.getDataPacks(file, dataFixer);
            });
        }

        public void saveDataTag(RegistryAccess registryAccess, WorldData worldData) {
            saveDataTag(registryAccess, worldData, null);
        }

        public void saveDataTag(RegistryAccess registryAccess, WorldData worldData, @Nullable CompoundTag compoundTag) {
            File file = this.levelPath.toFile();
            CompoundTag createTag = worldData.createTag(registryAccess, compoundTag);
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.put("Data", createTag);
            try {
                File createTempFile = File.createTempFile("level", ".dat", file);
                NbtIo.writeCompressed(compoundTag2, createTempFile);
                Util.safeReplaceFile(new File(file, "level.dat"), createTempFile, new File(file, "level.dat_old"));
            } catch (Exception e) {
                LevelStorageSource.LOGGER.error("Failed to save level {}", file, e);
            }
        }

        public File getIconFile() {
            checkLock();
            return this.levelPath.resolve("icon.png").toFile();
        }

        public void deleteLevel() throws IOException {
            checkLock();
            final Path resolve = this.levelPath.resolve("session.lock");
            for (int i = 1; i <= 5; i++) {
                LevelStorageSource.LOGGER.info("Attempt {}...", Integer.valueOf(i));
                try {
                    Files.walkFileTree(this.levelPath, new SimpleFileVisitor<Path>() { // from class: net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess.1
                        @Override // java.nio.file.SimpleFileVisitor, java.nio.file.FileVisitor
                        public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                            if (!path.equals(resolve)) {
                                LevelStorageSource.LOGGER.debug("Deleting {}", path);
                                Files.delete(path);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override // java.nio.file.SimpleFileVisitor, java.nio.file.FileVisitor
                        public FileVisitResult postVisitDirectory(Path path, IOException iOException) throws IOException {
                            if (iOException != null) {
                                throw iOException;
                            }
                            if (path.equals(LevelStorageAccess.this.levelPath)) {
                                LevelStorageAccess.this.lock.close();
                                Files.deleteIfExists(resolve);
                            }
                            Files.delete(path);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    return;
                } catch (IOException e) {
                    if (i < 5) {
                        LevelStorageSource.LOGGER.warn("Failed to delete {}", this.levelPath, e);
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException e2) {
                        }
                    } else {
                        throw e;
                    }
                }
            }
        }

        public void renameLevel(String str) throws IOException {
            checkLock();
            File file = new File(LevelStorageSource.this.baseDir.toFile(), this.levelId);
            if (!file.exists()) {
                return;
            }
            File file2 = new File(file, "level.dat");
            if (file2.exists()) {
                CompoundTag readCompressed = NbtIo.readCompressed(file2);
                readCompressed.getCompound("Data").putString("LevelName", str);
                NbtIo.writeCompressed(readCompressed, file2);
            }
        }

        public long makeWorldBackup() throws IOException {
            checkLock();
            String str = LocalDateTime.now().format(LevelStorageSource.FORMATTER) + "_" + this.levelId;
            Path backupPath = LevelStorageSource.this.getBackupPath();
            try {
                Files.createDirectories(Files.exists(backupPath, new LinkOption[0]) ? backupPath.toRealPath(new LinkOption[0]) : backupPath, new FileAttribute[0]);
                Path resolve = backupPath.resolve(FileUtil.findAvailableName(backupPath, str, ".zip"));
                final ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(resolve, new OpenOption[0])));
                Throwable th = null;
                try {
                    try {
                        final Path path = Paths.get(this.levelId, new String[0]);
                        Files.walkFileTree(this.levelPath, new SimpleFileVisitor<Path>() { // from class: net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess.2
                            @Override // java.nio.file.SimpleFileVisitor, java.nio.file.FileVisitor
                            public FileVisitResult visitFile(Path path2, BasicFileAttributes basicFileAttributes) throws IOException {
                                if (path2.endsWith("session.lock")) {
                                    return FileVisitResult.CONTINUE;
                                }
                                zipOutputStream.putNextEntry(new ZipEntry(path.resolve(LevelStorageAccess.this.levelPath.relativize(path2)).toString().replace('\\', '/')));
                                com.google.common.io.Files.asByteSource(path2.toFile()).copyTo(zipOutputStream);
                                zipOutputStream.closeEntry();
                                return FileVisitResult.CONTINUE;
                            }
                        });
                        if (zipOutputStream != null) {
                            if (0 != 0) {
                                try {
                                    zipOutputStream.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            } else {
                                zipOutputStream.close();
                            }
                        }
                        return Files.size(resolve);
                    } finally {
                    }
                } catch (Throwable th3) {
                    if (zipOutputStream != null) {
                        if (th != null) {
                            try {
                                zipOutputStream.close();
                            } catch (Throwable th4) {
                                th.addSuppressed(th4);
                            }
                        } else {
                            zipOutputStream.close();
                        }
                    }
                    throw th3;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override // java.lang.AutoCloseable
        public void close() throws IOException {
            this.lock.close();
        }
    }
}
