package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/BlockRotProcessor.class */
public class BlockRotProcessor extends StructureProcessor {
    public static final Codec<BlockRotProcessor> CODEC = Codec.FLOAT.fieldOf("integrity").orElse(Float.valueOf(1.0f)).xmap((v1) -> {
        return new BlockRotProcessor(v1);
    }, blockRotProcessor -> {
        return Float.valueOf(blockRotProcessor.integrity);
    }).codec();
    private final float integrity;

    public BlockRotProcessor(float f) {
        this.integrity = f;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        Random random = structurePlaceSettings.getRandom(structureBlockInfo2.pos);
        if (this.integrity >= 1.0f || random.nextFloat() <= this.integrity) {
            return structureBlockInfo2;
        }
        return null;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_ROT;
    }
}
