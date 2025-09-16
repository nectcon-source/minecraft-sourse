package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/SinglePoolElement.class */
public class SinglePoolElement extends StructurePoolElement {
    private static final Codec<Either<ResourceLocation, StructureTemplate>> TEMPLATE_CODEC = Codec.of(SinglePoolElement::encodeTemplate, ResourceLocation.CODEC.map((v0) -> {
        return Either.left(v0);
    }));
    public static final Codec<SinglePoolElement> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(templateCodec(), processorsCodec(), projectionCodec()).apply(instance, SinglePoolElement::new);
    });
    protected final Either<ResourceLocation, StructureTemplate> template;
    protected final Supplier<StructureProcessorList> processors;

    private static <T> DataResult<T> encodeTemplate(Either<ResourceLocation, StructureTemplate> either, DynamicOps<T> dynamicOps, T t) {
        Optional<ResourceLocation> left = either.left();
        if (!left.isPresent()) {
            return DataResult.error("Can not serialize a runtime pool element");
        }
        return ResourceLocation.CODEC.encode(left.get(), dynamicOps, t);
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Supplier<StructureProcessorList>> processorsCodec() {
        return StructureProcessorType.LIST_CODEC.fieldOf("processors").forGetter(singlePoolElement -> {
            return singlePoolElement.processors;
        });
    }

    protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Either<ResourceLocation, StructureTemplate>> templateCodec() {
        return TEMPLATE_CODEC.fieldOf("location").forGetter(singlePoolElement -> {
            return singlePoolElement.template;
        });
    }

    protected SinglePoolElement(Either<ResourceLocation, StructureTemplate> either, Supplier<StructureProcessorList> supplier, StructureTemplatePool.Projection projection) {
        super(projection);
        this.template = either;
        this.processors = supplier;
    }

    public SinglePoolElement(StructureTemplate structureTemplate) {
        this(Either.right(structureTemplate), () -> {
            return ProcessorLists.EMPTY;
        }, StructureTemplatePool.Projection.RIGID);
    }

    private StructureTemplate getTemplate(StructureManager structureManager) {
        Either<ResourceLocation, StructureTemplate> either = this.template;
        structureManager.getClass();
        return (StructureTemplate) either.map(structureManager::getOrCreate, Function.identity());
    }

    public List<StructureTemplate.StructureBlockInfo> getDataMarkers(StructureManager structureManager, BlockPos blockPos, Rotation rotation, boolean z) {
        List<StructureTemplate.StructureBlockInfo> filterBlocks = getTemplate(structureManager).filterBlocks(blockPos, new StructurePlaceSettings().setRotation(rotation), Blocks.STRUCTURE_BLOCK, z);
        List<StructureTemplate.StructureBlockInfo> newArrayList = Lists.newArrayList();
        for (StructureTemplate.StructureBlockInfo structureBlockInfo : filterBlocks) {
            if (structureBlockInfo.nbt != null && StructureMode.valueOf(structureBlockInfo.nbt.getString("mode")) == StructureMode.DATA) {
                newArrayList.add(structureBlockInfo);
            }
        }
        return newArrayList;
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager structureManager, BlockPos blockPos, Rotation rotation, Random random) {
        List<StructureTemplate.StructureBlockInfo> filterBlocks = getTemplate(structureManager).filterBlocks(blockPos, new StructurePlaceSettings().setRotation(rotation), Blocks.JIGSAW, true);
        Collections.shuffle(filterBlocks, random);
        return filterBlocks;
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public BoundingBox getBoundingBox(StructureManager structureManager, BlockPos blockPos, Rotation rotation) {
        return getTemplate(structureManager).getBoundingBox(new StructurePlaceSettings().setRotation(rotation), blockPos);
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public boolean place(StructureManager structureManager, WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos2, Rotation rotation, BoundingBox boundingBox, Random random, boolean z) {
        StructureTemplate template = getTemplate(structureManager);
        StructurePlaceSettings settings = getSettings(rotation, boundingBox, z);
        if (template.placeInWorld(worldGenLevel, blockPos, blockPos2, settings, random, 18)) {
            Iterator<StructureTemplate.StructureBlockInfo> it = StructureTemplate.processBlockInfos(worldGenLevel, blockPos, blockPos2, settings, getDataMarkers(structureManager, blockPos, rotation, false)).iterator();
            while (it.hasNext()) {
                handleDataMarker(worldGenLevel, it.next(), blockPos, rotation, random, boundingBox);
            }
            return true;
        }
        return false;
    }

    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, boolean z) {
        StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings();
        structurePlaceSettings.setBoundingBox(boundingBox);
        structurePlaceSettings.setRotation(rotation);
        structurePlaceSettings.setKnownShape(true);
        structurePlaceSettings.setIgnoreEntities(false);
        structurePlaceSettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        structurePlaceSettings.setFinalizeEntities(true);
        if (!z) {
            structurePlaceSettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
        }
        List<StructureProcessor> list = this.processors.get().list();
        structurePlaceSettings.getClass();
        list.forEach(structurePlaceSettings::addProcessor);
        ImmutableList<StructureProcessor> processors = getProjection().getProcessors();
        structurePlaceSettings.getClass();
        processors.forEach(structurePlaceSettings::addProcessor);
        return structurePlaceSettings;
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.SINGLE;
    }

    public String toString() {
        return "Single[" + this.template + "]";
    }
}
