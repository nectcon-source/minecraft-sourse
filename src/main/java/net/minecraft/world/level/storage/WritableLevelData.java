package net.minecraft.world.level.storage;

import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/WritableLevelData.class */
public interface WritableLevelData extends LevelData {
    void setXSpawn(int i);

    void setYSpawn(int i);

    void setZSpawn(int i);

    void setSpawnAngle(float f);

    default void setSpawn(BlockPos blockPos, float f) {
        setXSpawn(blockPos.getX());
        setYSpawn(blockPos.getY());
        setZSpawn(blockPos.getZ());
        setSpawnAngle(f);
    }
}
