package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/lighting/LightEventListener.class */
public interface LightEventListener {
    void updateSectionStatus(SectionPos sectionPos, boolean z);

    default void updateSectionStatus(BlockPos blockPos, boolean z) {
        updateSectionStatus(SectionPos.of(blockPos), z);
    }
}
