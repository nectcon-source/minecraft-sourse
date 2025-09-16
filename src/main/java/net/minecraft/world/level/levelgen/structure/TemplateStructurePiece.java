package net.minecraft.world.level.levelgen.structure;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Random;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/TemplateStructurePiece.class */
public abstract class TemplateStructurePiece extends StructurePiece {
    private static final Logger LOGGER = LogManager.getLogger();
    protected StructureTemplate template;
    protected StructurePlaceSettings placeSettings;
    protected BlockPos templatePosition;

    protected abstract void handleDataMarker(String str, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox);

    public TemplateStructurePiece(StructurePieceType structurePieceType, int i) {
        super(structurePieceType, i);
    }

    public TemplateStructurePiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
        super(structurePieceType, compoundTag);
        this.templatePosition = new BlockPos(compoundTag.getInt("TPX"), compoundTag.getInt("TPY"), compoundTag.getInt("TPZ"));
    }

    protected void setup(StructureTemplate structureTemplate, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings) {
        this.template = structureTemplate;
        setOrientation(Direction.NORTH);
        this.templatePosition = blockPos;
        this.placeSettings = structurePlaceSettings;
        this.boundingBox = structureTemplate.getBoundingBox(structurePlaceSettings, blockPos);
    }

    @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("TPX", this.templatePosition.getX());
        compoundTag.putInt("TPY", this.templatePosition.getY());
        compoundTag.putInt("TPZ", this.templatePosition.getZ());
    }

    @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
    public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        this.placeSettings.setBoundingBox(boundingBox);
        this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
        if (this.template.placeInWorld(worldGenLevel, this.templatePosition, blockPos, this.placeSettings, random, 2)) {
            for (StructureTemplate.StructureBlockInfo structureBlockInfo : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.STRUCTURE_BLOCK)) {
                if (structureBlockInfo.nbt != null && StructureMode.valueOf(structureBlockInfo.nbt.getString("mode")) == StructureMode.DATA) {
                    handleDataMarker(structureBlockInfo.nbt.getString("metadata"), structureBlockInfo.pos, worldGenLevel, random, boundingBox);
                }
            }
            for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.JIGSAW)) {
                if (structureBlockInfo2.nbt != null) {
                    String string = structureBlockInfo2.nbt.getString("final_state");
                    BlockStateParser blockStateParser = new BlockStateParser(new StringReader(string), false);
                    BlockState defaultBlockState = Blocks.AIR.defaultBlockState();
                    try {
                        blockStateParser.parse(true);
                        BlockState state = blockStateParser.getState();
                        if (state != null) {
                            defaultBlockState = state;
                        } else {
                            LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", string, structureBlockInfo2.pos);
                        }
                    } catch (CommandSyntaxException e) {
                        LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", string, structureBlockInfo2.pos);
                    }
                    worldGenLevel.setBlock(structureBlockInfo2.pos, defaultBlockState, 3);
                }
            }
            return true;
        }
        return true;
    }

    @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
    public void move(int i, int i2, int i3) {
        super.move(i, i2, i3);
        this.templatePosition = this.templatePosition.offset(i, i2, i3);
    }

    @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
    public Rotation getRotation() {
        return this.placeSettings.getRotation();
    }
}
