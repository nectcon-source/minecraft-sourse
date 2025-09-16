package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/StructurePoolElement.class */
public abstract class StructurePoolElement {
    public static final Codec<StructurePoolElement> CODEC = Registry.STRUCTURE_POOL_ELEMENT.dispatch("element_type", (v0) -> {
        return v0.getType();
    }, (v0) -> {
        return v0.codec();
    });

    @Nullable
    private volatile StructureTemplatePool.Projection projection;

    public abstract List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager structureManager, BlockPos blockPos, Rotation rotation, Random random);

    public abstract BoundingBox getBoundingBox(StructureManager structureManager, BlockPos blockPos, Rotation rotation);

    public abstract boolean place(StructureManager structureManager, WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos2, Rotation rotation, BoundingBox boundingBox, Random random, boolean z);

    public abstract StructurePoolElementType<?> getType();

    protected static <E extends StructurePoolElement> RecordCodecBuilder<E, StructureTemplatePool.Projection> projectionCodec() {
        return StructureTemplatePool.Projection.CODEC.fieldOf("projection").forGetter((v0) -> {
            return v0.getProjection();
        });
    }

    protected StructurePoolElement(StructureTemplatePool.Projection projection) {
        this.projection = projection;
    }

    public void handleDataMarker(LevelAccessor levelAccessor, StructureTemplate.StructureBlockInfo structureBlockInfo, BlockPos blockPos, Rotation rotation, Random random, BoundingBox boundingBox) {
    }

    public StructurePoolElement setProjection(StructureTemplatePool.Projection projection) {
        this.projection = projection;
        return this;
    }

    public StructureTemplatePool.Projection getProjection() {
        StructureTemplatePool.Projection projection = this.projection;
        if (projection == null) {
            throw new IllegalStateException();
        }
        return projection;
    }

    public int getGroundLevelDelta() {
        return 1;
    }

    public static Function<StructureTemplatePool.Projection, EmptyPoolElement> empty() {
        return projection -> {
            return EmptyPoolElement.INSTANCE;
        };
    }

    public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String str) {
        return projection -> {
            return new LegacySinglePoolElement(Either.left(new ResourceLocation(str)), () -> {
                return ProcessorLists.EMPTY;
            }, projection);
        };
    }

    public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String str, StructureProcessorList structureProcessorList) {
        return projection -> {
            return new LegacySinglePoolElement(Either.left(new ResourceLocation(str)), () -> {
                return structureProcessorList;
            }, projection);
        };
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String str) {
        return projection -> {
            return new SinglePoolElement(Either.left(new ResourceLocation(str)), () -> {
                return ProcessorLists.EMPTY;
            }, projection);
        };
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String str, StructureProcessorList structureProcessorList) {
        return projection -> {
            return new SinglePoolElement(Either.left(new ResourceLocation(str)), () -> {
                return structureProcessorList;
            }, projection);
        };
    }

    public static Function<StructureTemplatePool.Projection, FeaturePoolElement> feature(ConfiguredFeature<?, ?> configuredFeature) {
        return projection -> {
            return new FeaturePoolElement(() -> {
                return configuredFeature;
            }, projection);
        };
    }

    public static Function<StructureTemplatePool.Projection, ListPoolElement> list(List<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>> list) {
        return projection -> {
            return new ListPoolElement((List) list.stream().map(function -> {
                return (StructurePoolElement) function.apply(projection);
            }).collect(Collectors.toList()), projection);
        };
    }
}
