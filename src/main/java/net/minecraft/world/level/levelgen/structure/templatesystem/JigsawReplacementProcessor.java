package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/JigsawReplacementProcessor.class */
public class JigsawReplacementProcessor extends StructureProcessor {

    public static final Codec<JigsawReplacementProcessor> CODEC = Codec.unit(() -> JigsawReplacementProcessor.INSTANCE);
    public static final JigsawReplacementProcessor INSTANCE = new JigsawReplacementProcessor();

    private JigsawReplacementProcessor() {
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelReader, BlockPos blockPos, BlockPos blockPos2, StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2, StructurePlaceSettings structurePlaceSettings) {
        if (!structureBlockInfo2.state.is(Blocks.JIGSAW)) {
            return structureBlockInfo2;
        }
        BlockStateParser blockStateParser = new BlockStateParser(new StringReader(structureBlockInfo2.nbt.getString("final_state")), false);
        try {
            blockStateParser.parse(true);
            if (blockStateParser.getState().is(Blocks.STRUCTURE_VOID)) {
                return null;
            }
            return new StructureTemplate.StructureBlockInfo(structureBlockInfo2.pos, blockStateParser.getState(), null);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException((Throwable) e);
        }
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.JIGSAW_REPLACEMENT;
    }
}
