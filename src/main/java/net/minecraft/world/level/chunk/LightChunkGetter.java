package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/LightChunkGetter.class */
public interface LightChunkGetter {
    @Nullable
    BlockGetter getChunkForLighting(int i, int i2);

    BlockGetter getLevel();

    default void onLightUpdate(LightLayer lightLayer, SectionPos sectionPos) {
    }
}
