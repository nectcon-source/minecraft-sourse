package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/PlayerDataStorage.class */
public class PlayerDataStorage {
    private static final Logger LOGGER = LogManager.getLogger();
    private final File playerDir;
    protected final DataFixer fixerUpper;

    public PlayerDataStorage(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer) {
        this.fixerUpper = dataFixer;
        this.playerDir = levelStorageAccess.getLevelPath(LevelResource.PLAYER_DATA_DIR).toFile();
        this.playerDir.mkdirs();
    }

    public void save(Player player) {
        try {
            CompoundTag saveWithoutId = player.saveWithoutId(new CompoundTag());
            File createTempFile = File.createTempFile(player.getStringUUID() + "-", ".dat", this.playerDir);
            NbtIo.writeCompressed(saveWithoutId, createTempFile);
            Util.safeReplaceFile(new File(this.playerDir, player.getStringUUID() + ".dat"), createTempFile, new File(this.playerDir, player.getStringUUID() + ".dat_old"));
        } catch (Exception e) {
            LOGGER.warn("Failed to save player data for {}", player.getName().getString());
        }
    }

    @Nullable
    public CompoundTag load(Player player) {
        CompoundTag compoundTag = null;
        try {
            File file = new File(this.playerDir, player.getStringUUID() + ".dat");
            if (file.exists() && file.isFile()) {
                compoundTag = NbtIo.readCompressed(file);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load player data for {}", player.getName().getString());
        }
        if (compoundTag != null) {
            player.load(NbtUtils.update(this.fixerUpper, DataFixTypes.PLAYER, compoundTag, compoundTag.contains("DataVersion", 3) ? compoundTag.getInt("DataVersion") : -1));
        }
        return compoundTag;
    }

    public String[] getSeenPlayers() {
        String[] list = this.playerDir.list();
        if (list == null) {
            list = new String[0];
        }
        for (int i = 0; i < list.length; i++) {
            if (list[i].endsWith(".dat")) {
                list[i] = list[i].substring(0, list[i].length() - 4);
            }
        }
        return list;
    }
}
