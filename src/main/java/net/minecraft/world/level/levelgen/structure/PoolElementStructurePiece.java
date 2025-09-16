package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/PoolElementStructurePiece.class */
public class PoolElementStructurePiece extends StructurePiece {
    private static final Logger LOGGER = LogManager.getLogger();
    protected final StructurePoolElement element;
    protected BlockPos position;
    private final int groundLevelDelta;
    protected final Rotation rotation;
    private final List<JigsawJunction> junctions;
    private final StructureManager structureManager;

    public PoolElementStructurePiece(StructureManager structureManager, StructurePoolElement structurePoolElement, BlockPos blockPos, int i, Rotation rotation, BoundingBox boundingBox) {
        super(StructurePieceType.JIGSAW, 0);
        this.junctions = Lists.newArrayList();
        this.structureManager = structureManager;
        this.element = structurePoolElement;
        this.position = blockPos;
        this.groundLevelDelta = i;
        this.rotation = rotation;
        this.boundingBox = boundingBox;
    }

    public PoolElementStructurePiece(StructureManager structureManager, CompoundTag compoundTag) {
        super(StructurePieceType.JIGSAW, compoundTag);
        this.junctions = Lists.newArrayList();
        this.structureManager = structureManager;
        this.position = new BlockPos(compoundTag.getInt("PosX"), compoundTag.getInt("PosY"), compoundTag.getInt("PosZ"));
        this.groundLevelDelta = compoundTag.getInt("ground_level_delta");
        DataResult parse = StructurePoolElement.CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound("pool_element"));
        Logger logger = LOGGER;
        logger.getClass();
        this.element = (StructurePoolElement) parse.resultOrPartial(logger::error).orElse(EmptyPoolElement.INSTANCE);
        this.rotation = Rotation.valueOf(compoundTag.getString("rotation"));
        this.boundingBox = this.element.getBoundingBox(structureManager, this.position, this.rotation);
        ListTag list = compoundTag.getList("junctions", 10);
        this.junctions.clear();
        list.forEach(tag -> {
            this.junctions.add(JigsawJunction.deserialize(new Dynamic(NbtOps.INSTANCE, tag)));
        });
    }

    @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("PosX", this.position.getX());
        compoundTag.putInt("PosY", this.position.getY());
        compoundTag.putInt("PosZ", this.position.getZ());
        compoundTag.putInt("ground_level_delta", this.groundLevelDelta);
        StructurePoolElement.CODEC.encodeStart(NbtOps.INSTANCE, this.element).resultOrPartial(LOGGER::error).ifPresent(tag -> {
            compoundTag.put("pool_element", tag);
        });
        compoundTag.putString("rotation", this.rotation.name());
        ListTag listTag = new ListTag();
        Iterator<JigsawJunction> it = this.junctions.iterator();
        while (it.hasNext()) {
            listTag.add(it.next().serialize(NbtOps.INSTANCE).getValue());
        }
        compoundTag.put("junctions", listTag);
    }

    @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
    public boolean postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        return place(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, blockPos, false);
    }

    public boolean place(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, BlockPos blockPos, boolean z) {
        return this.element.place(this.structureManager, worldGenLevel, structureFeatureManager, chunkGenerator, this.position, blockPos, this.rotation, boundingBox, random, z);
    }

    @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
    public void move(int i, int i2, int i3) {
        super.move(i, i2, i3);
        this.position = this.position.offset(i, i2, i3);
    }

    @Override // net.minecraft.world.level.levelgen.structure.StructurePiece
    public Rotation getRotation() {
        return this.rotation;
    }

    public String toString() {
        return String.format("<%s | %s | %s | %s>", getClass().getSimpleName(), this.position, this.rotation, this.element);
    }

    public StructurePoolElement getElement() {
        return this.element;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public int getGroundLevelDelta() {
        return this.groundLevelDelta;
    }

    public void addJunction(JigsawJunction jigsawJunction) {
        this.junctions.add(jigsawJunction);
    }

    public List<JigsawJunction> getJunctions() {
        return this.junctions;
    }
}
