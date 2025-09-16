package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/ListPoolElement.class */
public class ListPoolElement extends StructurePoolElement {
    public static final Codec<ListPoolElement> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(StructurePoolElement.CODEC.listOf().fieldOf("elements").forGetter(listPoolElement -> {
            return listPoolElement.elements;
        }), projectionCodec()).apply(instance, ListPoolElement::new);
    });
    private final List<StructurePoolElement> elements;

    public ListPoolElement(List<StructurePoolElement> list, StructureTemplatePool.Projection projection) {
        super(projection);
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Elements are empty");
        }
        this.elements = list;
        setProjectionOnEachElement(projection);
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager structureManager, BlockPos blockPos, Rotation rotation, Random random) {
        return this.elements.get(0).getShuffledJigsawBlocks(structureManager, blockPos, rotation, random);
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public BoundingBox getBoundingBox(StructureManager structureManager, BlockPos blockPos, Rotation rotation) {
        BoundingBox unknownBox = BoundingBox.getUnknownBox();
        Iterator<StructurePoolElement> it = this.elements.iterator();
        while (it.hasNext()) {
            unknownBox.expand(it.next().getBoundingBox(structureManager, blockPos, rotation));
        }
        return unknownBox;
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public boolean place(StructureManager structureManager, WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos2, Rotation rotation, BoundingBox boundingBox, Random random, boolean z) {
        Iterator<StructurePoolElement> it = this.elements.iterator();
        while (it.hasNext()) {
            if (!it.next().place(structureManager, worldGenLevel, structureFeatureManager, chunkGenerator, blockPos, blockPos2, rotation, boundingBox, random, z)) {
                return false;
            }
        }
        return true;
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.LIST;
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public StructurePoolElement setProjection(StructureTemplatePool.Projection projection) {
        super.setProjection(projection);
        setProjectionOnEachElement(projection);
        return this;
    }

    public String toString() {
        return "List[" + ((String) this.elements.stream().map((v0) -> {
            return v0.toString();
        }).collect(Collectors.joining(", "))) + "]";
    }

    private void setProjectionOnEachElement(StructureTemplatePool.Projection projection) {
        this.elements.forEach(structurePoolElement -> {
            structurePoolElement.setProjection(projection);
        });
    }
}
