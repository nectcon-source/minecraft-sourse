package net.minecraft.world.level;

import com.mojang.datafixers.DataFixUtils;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/StructureFeatureManager.class */
public class StructureFeatureManager {
    private final LevelAccessor level;
    private final WorldGenSettings worldGenSettings;

    public StructureFeatureManager(LevelAccessor levelAccessor, WorldGenSettings worldGenSettings) {
        this.level = levelAccessor;
        this.worldGenSettings = worldGenSettings;
    }

    public StructureFeatureManager forWorldGenRegion(WorldGenRegion worldGenRegion) {
        if (worldGenRegion.getLevel() != this.level) {
            throw new IllegalStateException("Using invalid feature manager (source level: " + worldGenRegion.getLevel() + ", region: " + worldGenRegion);
        }
        return new StructureFeatureManager(worldGenRegion, this.worldGenSettings);
    }

    public Stream<? extends StructureStart<?>> startsForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature) {
        return this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(structureFeature).stream().map(l -> {
            return SectionPos.of(new ChunkPos(l.longValue()), 0);
        }).map(sectionPos2 -> {
            return getStartForFeature(sectionPos2, structureFeature, this.level.getChunk(sectionPos2.x(), sectionPos2.z(), ChunkStatus.STRUCTURE_STARTS));
        }).filter(structureStart -> {
            return structureStart != null && structureStart.isValid();
        });
    }

    @Nullable
    public StructureStart<?> getStartForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature, FeatureAccess featureAccess) {
        return featureAccess.getStartForFeature(structureFeature);
    }

    public void setStartForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature, StructureStart<?> structureStart, FeatureAccess featureAccess) {
        featureAccess.setStartForFeature(structureFeature, structureStart);
    }

    public void addReferenceForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature, long j, FeatureAccess featureAccess) {
        featureAccess.addReferenceForFeature(structureFeature, j);
    }

    public boolean shouldGenerateFeatures() {
        return this.worldGenSettings.generateFeatures();
    }

    public StructureStart<?> getStructureAt(BlockPos blockPos, boolean z, StructureFeature<?> structureFeature) {
        return  DataFixUtils.orElse(startsForFeature(SectionPos.of(blockPos), structureFeature).filter(structureStart -> {
            return structureStart.getBoundingBox().isInside(blockPos);
        }).filter(structureStart2 -> {
            return !z || structureStart2.getPieces().stream().anyMatch(structurePiece -> {
                return structurePiece.getBoundingBox().isInside(blockPos);
            });
        }).findFirst(), StructureStart.INVALID_START);
    }
}
