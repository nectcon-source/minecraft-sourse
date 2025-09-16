package net.minecraft.world.level;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelData;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/LevelAccessor.class */
public interface LevelAccessor extends CommonLevelAccessor, LevelTimeAccess {
    TickList<Block> getBlockTicks();

    TickList<Fluid> getLiquidTicks();

    LevelData getLevelData();

    DifficultyInstance getCurrentDifficultyAt(BlockPos blockPos);

    ChunkSource getChunkSource();

    Random getRandom();

    void playSound(@Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float f2);

    void addParticle(ParticleOptions particleOptions, double d, double d2, double d3, double d4, double d5, double d6);

    void levelEvent(@Nullable Player player, int i, BlockPos blockPos, int i2);

    @Override // net.minecraft.world.level.LevelTimeAccess
    default long dayTime() {
        return getLevelData().getDayTime();
    }

    default Difficulty getDifficulty() {
        return getLevelData().getDifficulty();
    }

    default boolean hasChunk(int i, int i2) {
        return getChunkSource().hasChunk(i, i2);
    }

    default void blockUpdated(BlockPos blockPos, Block block) {
    }

    default int getHeight() {
        return dimensionType().logicalHeight();
    }

    default void levelEvent(int i, BlockPos blockPos, int i2) {
        levelEvent(null, i, blockPos, i2);
    }
}
