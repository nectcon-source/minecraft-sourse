package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/NopProcessor.class */
public class NopProcessor extends StructureProcessor {
    public static final Codec<NopProcessor> CODEC = Codec.unit(() -> NopProcessor.INSTANCE);
    public static final NopProcessor INSTANCE = new NopProcessor();

    private NopProcessor() {
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        return structureBlockInfo2;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.NOP;
    }
}
