package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/LegacySinglePoolElement.class */
public class LegacySinglePoolElement extends SinglePoolElement {
    public static final Codec<LegacySinglePoolElement> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(templateCodec(), processorsCodec(), projectionCodec()).apply(instance, LegacySinglePoolElement::new);
    });

    protected LegacySinglePoolElement(Either<ResourceLocation, StructureTemplate> either, Supplier<StructureProcessorList> supplier, StructureTemplatePool.Projection projection) {
        super(either, supplier, projection);
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement
    protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingBox, boolean z) {
        StructurePlaceSettings settings = super.getSettings(rotation, boundingBox, z);
        settings.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        settings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        return settings;
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement, net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.LEGACY;
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.SinglePoolElement
    public String toString() {
        return "LegacySingle[" + this.template + "]";
    }
}
