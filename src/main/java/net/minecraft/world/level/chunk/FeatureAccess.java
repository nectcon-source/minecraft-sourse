package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/chunk/FeatureAccess.class */
public interface FeatureAccess {
    @Nullable
    StructureStart<?> getStartForFeature(StructureFeature<?> structureFeature);

    void setStartForFeature(StructureFeature<?> structureFeature, StructureStart<?> structureStart);

    LongSet getReferencesForFeature(StructureFeature<?> structureFeature);

    void addReferenceForFeature(StructureFeature<?> structureFeature, long j);

    Map<StructureFeature<?>, LongSet> getAllReferences();

    void setAllReferences(Map<StructureFeature<?>, LongSet> map);
}
