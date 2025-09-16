package net.minecraft.world.level.lighting;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.DataLayer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/lighting/LayerLightEventListener.class */
public interface LayerLightEventListener extends LightEventListener {
    @Nullable
    DataLayer getDataLayerData(SectionPos sectionPos);

    int getLightValue(BlockPos blockPos);

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/lighting/LayerLightEventListener$DummyLightLayerEventListener.class */
    public enum DummyLightLayerEventListener implements LayerLightEventListener {
        INSTANCE;

        @Override // net.minecraft.world.level.lighting.LayerLightEventListener
        @Nullable
        public DataLayer getDataLayerData(SectionPos sectionPos) {
            return null;
        }

        @Override // net.minecraft.world.level.lighting.LayerLightEventListener
        public int getLightValue(BlockPos blockPos) {
            return 0;
        }

        @Override // net.minecraft.world.level.lighting.LightEventListener
        public void updateSectionStatus(SectionPos sectionPos, boolean z) {
        }
    }
}
