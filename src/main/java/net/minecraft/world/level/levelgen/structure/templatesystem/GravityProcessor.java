package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/GravityProcessor.class */
public class GravityProcessor extends StructureProcessor {
    public static final Codec<GravityProcessor> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Heightmap.Types.CODEC.fieldOf("heightmap").orElse(Heightmap.Types.WORLD_SURFACE_WG).forGetter(gravityProcessor -> {
            return gravityProcessor.heightmap;
        }), Codec.INT.fieldOf("offset").orElse(0).forGetter(gravityProcessor2 -> {
            return Integer.valueOf(gravityProcessor2.offset);
        })).apply(instance, (v1, v2) -> {
            return new GravityProcessor(v1, v2);
        });
    });
    private final Heightmap.Types heightmap;
    private final int offset;

    public GravityProcessor(Heightmap.Types types, int i) {
        this.heightmap = types;
        this.offset = i;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        Heightmap.Types types;
        if (levelReader instanceof ServerLevel) {
            if (this.heightmap == Heightmap.Types.WORLD_SURFACE_WG) {
                types = Heightmap.Types.WORLD_SURFACE;
            } else if (this.heightmap == Heightmap.Types.OCEAN_FLOOR_WG) {
                types = Heightmap.Types.OCEAN_FLOOR;
            } else {
                types = this.heightmap;
            }
        } else {
            types = this.heightmap;
        }
        return new StructureTemplate.StructureBlockInfo(new BlockPos(structureBlockInfo2.pos.getX(), levelReader.getHeight(types, structureBlockInfo2.pos.getX(), structureBlockInfo2.pos.getZ()) + this.offset + structureBlockInfo.pos.getY(), structureBlockInfo2.pos.getZ()), structureBlockInfo2.state, structureBlockInfo2.nbt);
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.GRAVITY;
    }
}
