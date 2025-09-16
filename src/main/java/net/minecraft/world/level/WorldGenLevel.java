package net.minecraft.world.level;

import java.util.stream.Stream;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/WorldGenLevel.class */
public interface WorldGenLevel extends ServerLevelAccessor {
    long getSeed();

    Stream<? extends StructureStart<?>> startsForFeature(SectionPos sectionPos, StructureFeature<?> structureFeature);
}
