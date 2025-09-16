package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/StructureManager.class */
public class StructureManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<ResourceLocation, StructureTemplate> structureRepository = Maps.newHashMap();
    private final DataFixer fixerUpper;
    private ResourceManager resourceManager;
    private final Path generatedDir;

    public StructureManager(ResourceManager resourceManager, LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer) {
        this.resourceManager = resourceManager;
        this.fixerUpper = dataFixer;
        this.generatedDir = levelStorageAccess.getLevelPath(LevelResource.GENERATED_DIR).normalize();
    }

    public StructureTemplate getOrCreate(ResourceLocation resourceLocation) {
        StructureTemplate structureTemplate = get(resourceLocation);
        if (structureTemplate == null) {
            structureTemplate = new StructureTemplate();
            this.structureRepository.put(resourceLocation, structureTemplate);
        }
        return structureTemplate;
    }

    @Nullable
    public StructureTemplate get(ResourceLocation resourceLocation) {
        return this.structureRepository.computeIfAbsent(resourceLocation, resourceLocation2 -> {
            StructureTemplate loadFromGenerated = loadFromGenerated(resourceLocation2);
            return loadFromGenerated != null ? loadFromGenerated : loadFromResource(resourceLocation2);
        });
    }

    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.structureRepository.clear();
    }

    @Nullable
    private StructureTemplate loadFromResource(ResourceLocation resourceLocation) {
        try {
            Resource resource = this.resourceManager.getResource(new ResourceLocation(resourceLocation.getNamespace(), "structures/" + resourceLocation.getPath() + ".nbt"));
            Throwable th = null;
            try {
                StructureTemplate readStructure = readStructure(resource.getInputStream());
                if (resource != null) {
                    if (0 != 0) {
                        try {
                            resource.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    } else {
                        resource.close();
                    }
                }
                return readStructure;
            } catch (Throwable th3) {
                if (resource != null) {
                    if (0 != 0) {
                        try {
                            resource.close();
                        } catch (Throwable th4) {
                            th.addSuppressed(th4);
                        }
                    } else {
                        resource.close();
                    }
                }
                throw th3;
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (Throwable th5) {
            LOGGER.error("Couldn't load structure {}: {}", resourceLocation, th5.toString());
            return null;
        }
    }

    @Nullable
    private StructureTemplate loadFromGenerated(ResourceLocation resourceLocation) {
        if (!this.generatedDir.toFile().isDirectory()) {
            return null;
        }
        Path createAndValidatePathToStructure = createAndValidatePathToStructure(resourceLocation, ".nbt");
        try {
            InputStream fileInputStream = new FileInputStream(createAndValidatePathToStructure.toFile());
            Throwable th = null;
            try {
                try {
                    StructureTemplate readStructure = readStructure(fileInputStream);
                    if (fileInputStream != null) {
                        if (0 != 0) {
                            try {
                                fileInputStream.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        } else {
                            fileInputStream.close();
                        }
                    }
                    return readStructure;
                } finally {
                }
            } catch (Throwable th3) {
                if (fileInputStream != null) {
                    if (th != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable th4) {
                            th.addSuppressed(th4);
                        }
                    } else {
                        fileInputStream.close();
                    }
                }
                throw th3;
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e2) {
            LOGGER.error("Couldn't load structure from {}", createAndValidatePathToStructure, e2);
            return null;
        }
    }

    private StructureTemplate readStructure(InputStream inputStream) throws IOException {
        return readStructure(NbtIo.readCompressed(inputStream));
    }

    public StructureTemplate readStructure(CompoundTag compoundTag) {
        if (!compoundTag.contains("DataVersion", 99)) {
            compoundTag.putInt("DataVersion", 500);
        }
        StructureTemplate structureTemplate = new StructureTemplate();
        structureTemplate.load(NbtUtils.update(this.fixerUpper, DataFixTypes.STRUCTURE, compoundTag, compoundTag.getInt("DataVersion")));
        return structureTemplate;
    }

    public boolean save(ResourceLocation resourceLocation) {
        StructureTemplate structureTemplate = this.structureRepository.get(resourceLocation);
        if (structureTemplate == null) {
            return false;
        }
        Path createAndValidatePathToStructure = createAndValidatePathToStructure(resourceLocation, ".nbt");
        Path parent = createAndValidatePathToStructure.getParent();
        if (parent == null) {
            return false;
        }
        try {
            Files.createDirectories(Files.exists(parent, new LinkOption[0]) ? parent.toRealPath(new LinkOption[0]) : parent, new FileAttribute[0]);
            CompoundTag save = structureTemplate.save(new CompoundTag());
            try {
                OutputStream fileOutputStream = new FileOutputStream(createAndValidatePathToStructure.toFile());
                Throwable th = null;
                try {
                    try {
                        NbtIo.writeCompressed(save, fileOutputStream);
                        if (fileOutputStream != null) {
                            if (0 != 0) {
                                try {
                                    fileOutputStream.close();
                                } catch (Throwable th2) {
                                    th.addSuppressed(th2);
                                }
                            } else {
                                fileOutputStream.close();
                            }
                        }
                        return true;
                    } finally {
                    }
                } finally {
                }
            } catch (Throwable th3) {
                return false;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create parent directory: {}", parent);
            return false;
        }
    }

    public Path createPathToStructure(ResourceLocation resourceLocation, String str) {
        try {
            return FileUtil.createPathToResource(this.generatedDir.resolve(resourceLocation.getNamespace()).resolve("structures"), resourceLocation.getPath(), str);
        } catch (InvalidPathException e) {
            throw new ResourceLocationException("Invalid resource path: " + resourceLocation, e);
        }
    }

    private Path createAndValidatePathToStructure(ResourceLocation resourceLocation, String str) {
        if (resourceLocation.getPath().contains("//")) {
            throw new ResourceLocationException("Invalid resource path: " + resourceLocation);
        }
        Path createPathToStructure = createPathToStructure(resourceLocation, str);
        if (!createPathToStructure.startsWith(this.generatedDir) || !FileUtil.isPathNormalized(createPathToStructure) || !FileUtil.isPathPortable(createPathToStructure)) {
            throw new ResourceLocationException("Invalid resource path: " + createPathToStructure);
        }
        return createPathToStructure;
    }

    public void remove(ResourceLocation resourceLocation) {
        this.structureRepository.remove(resourceLocation);
    }
}
