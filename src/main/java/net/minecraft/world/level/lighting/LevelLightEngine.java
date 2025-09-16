package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/lighting/LevelLightEngine.class */
public class LevelLightEngine implements LightEventListener {

    @Nullable
    private final LayerLightEngine<?, ?> blockEngine;

    @Nullable
    private final LayerLightEngine<?, ?> skyEngine;

    public LevelLightEngine(LightChunkGetter lightChunkGetter, boolean z, boolean z2) {
        this.blockEngine = z ? new BlockLightEngine(lightChunkGetter) : null;
        this.skyEngine = z2 ? new SkyLightEngine(lightChunkGetter) : null;
    }

    public void checkBlock(BlockPos blockPos) {
        if (this.blockEngine != null) {
            this.blockEngine.checkBlock(blockPos);
        }
        if (this.skyEngine != null) {
            this.skyEngine.checkBlock(blockPos);
        }
    }

    public void onBlockEmissionIncrease(BlockPos blockPos, int i) {
        if (this.blockEngine != null) {
            this.blockEngine.onBlockEmissionIncrease(blockPos, i);
        }
    }

    public boolean hasLightWork() {
        if (this.skyEngine == null || !this.skyEngine.hasLightWork()) {
            return this.blockEngine != null && this.blockEngine.hasLightWork();
        }
        return true;
    }

    public int runUpdates(int i, boolean z, boolean z2) {
        if (this.blockEngine != null && this.skyEngine != null) {
            int i2 = i / 2;
            int runUpdates = this.blockEngine.runUpdates(i2, z, z2);
            int runUpdates2 = this.skyEngine.runUpdates((i - i2) + runUpdates, z, z2);
            if (runUpdates == 0 && runUpdates2 > 0) {
                return this.blockEngine.runUpdates(runUpdates2, z, z2);
            }
            return runUpdates2;
        }
        if (this.blockEngine != null) {
            return this.blockEngine.runUpdates(i, z, z2);
        }
        if (this.skyEngine != null) {
            return this.skyEngine.runUpdates(i, z, z2);
        }
        return i;
    }

    @Override // net.minecraft.world.level.lighting.LightEventListener
    public void updateSectionStatus(SectionPos sectionPos, boolean z) {
        if (this.blockEngine != null) {
            this.blockEngine.updateSectionStatus(sectionPos, z);
        }
        if (this.skyEngine != null) {
            this.skyEngine.updateSectionStatus(sectionPos, z);
        }
    }

    public void enableLightSources(ChunkPos chunkPos, boolean z) {
        if (this.blockEngine != null) {
            this.blockEngine.enableLightSources(chunkPos, z);
        }
        if (this.skyEngine != null) {
            this.skyEngine.enableLightSources(chunkPos, z);
        }
    }

    public LayerLightEventListener getLayerListener(LightLayer lightLayer) {
        if (lightLayer == LightLayer.BLOCK) {
            if (this.blockEngine == null) {
                return LayerLightEventListener.DummyLightLayerEventListener.INSTANCE;
            }
            return this.blockEngine;
        }
        if (this.skyEngine == null) {
            return LayerLightEventListener.DummyLightLayerEventListener.INSTANCE;
        }
        return this.skyEngine;
    }

    public String getDebugData(LightLayer lightLayer, SectionPos sectionPos) {
        if (lightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                return this.blockEngine.getDebugData(sectionPos.asLong());
            }
            return "n/a";
        }
        if (this.skyEngine != null) {
            return this.skyEngine.getDebugData(sectionPos.asLong());
        }
        return "n/a";
    }

    public void queueSectionData(LightLayer lightLayer, SectionPos sectionPos, @Nullable DataLayer dataLayer, boolean z) {
        if (lightLayer == LightLayer.BLOCK) {
            if (this.blockEngine != null) {
                this.blockEngine.queueSectionData(sectionPos.asLong(), dataLayer, z);
            }
        } else if (this.skyEngine != null) {
            this.skyEngine.queueSectionData(sectionPos.asLong(), dataLayer, z);
        }
    }

    public void retainData(ChunkPos chunkPos, boolean z) {
        if (this.blockEngine != null) {
            this.blockEngine.retainData(chunkPos, z);
        }
        if (this.skyEngine != null) {
            this.skyEngine.retainData(chunkPos, z);
        }
    }

    public int getRawBrightness(BlockPos blockPos, int i) {
        return Math.max(this.blockEngine == null ? 0 : this.blockEngine.getLightValue(blockPos), this.skyEngine == null ? 0 : this.skyEngine.getLightValue(blockPos) - i);
    }
}
