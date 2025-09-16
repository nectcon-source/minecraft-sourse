package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/CommandStorage.class */
public class CommandStorage {
    private final Map<String, Container> namespaces = Maps.newHashMap();
    private final DimensionDataStorage storage;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/CommandStorage$Container.class */
    static class Container extends SavedData {
        private final Map<String, CompoundTag> storage;

        public Container(String str) {
            super(str);
            this.storage = Maps.newHashMap();
        }

        @Override // net.minecraft.world.level.saveddata.SavedData
        public void load(CompoundTag compoundTag) {
            CompoundTag compound = compoundTag.getCompound("contents");
            for (String str : compound.getAllKeys()) {
                this.storage.put(str, compound.getCompound(str));
            }
        }

        @Override // net.minecraft.world.level.saveddata.SavedData
        public CompoundTag save(CompoundTag compoundTag) {
            CompoundTag compoundTag2 = new CompoundTag();
            this.storage.forEach((str, compoundTag3) -> {
                compoundTag2.put(str, compoundTag3.copy());
            });
            compoundTag.put("contents", compoundTag2);
            return compoundTag;
        }

        public CompoundTag get(String str) {
            CompoundTag compoundTag = this.storage.get(str);
            return compoundTag != null ? compoundTag : new CompoundTag();
        }

        public void put(String str, CompoundTag compoundTag) {
            if (compoundTag.isEmpty()) {
                this.storage.remove(str);
            } else {
                this.storage.put(str, compoundTag);
            }
            setDirty();
        }

        public Stream<ResourceLocation> getKeys(String str) {
            return this.storage.keySet().stream().map(str2 -> {
                return new ResourceLocation(str, str2);
            });
        }
    }

    public CommandStorage(DimensionDataStorage dimensionDataStorage) {
        this.storage = dimensionDataStorage;
    }

    private Container newStorage(String str, String str2) {
        Container container = new Container(str2);
        this.namespaces.put(str, container);
        return container;
    }

    public CompoundTag get(ResourceLocation resourceLocation) {
        String namespace = resourceLocation.getNamespace();
        String createId = createId(namespace);
        Container container = (Container) this.storage.get(() -> {
            return newStorage(namespace, createId);
        }, createId);
        return container != null ? container.get(resourceLocation.getPath()) : new CompoundTag();
    }

    public void set(ResourceLocation resourceLocation, CompoundTag compoundTag) {
        String namespace = resourceLocation.getNamespace();
        String createId = createId(namespace);
        ((Container) this.storage.computeIfAbsent(() -> {
            return newStorage(namespace, createId);
        }, createId)).put(resourceLocation.getPath(), compoundTag);
    }

    public Stream<ResourceLocation> keys() {
        return this.namespaces.entrySet().stream().flatMap(entry -> {
            return ((Container) entry.getValue()).getKeys((String) entry.getKey());
        });
    }

    private static String createId(String str) {
        return "command_storage_" + str;
    }
}
