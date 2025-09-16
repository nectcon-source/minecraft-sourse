package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/StructureStart.class */
public abstract class StructureStart<C extends FeatureConfiguration> {
    public static final StructureStart<?> INVALID_START = new StructureStart<MineshaftConfiguration>(StructureFeature.MINESHAFT, 0, 0, BoundingBox.getUnknownBox(), 0, 0) { // from class: net.minecraft.world.level.levelgen.structure.StructureStart.1
        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int i2, Biome biome, MineshaftConfiguration mineshaftConfiguration) {
        }
    };
    private final StructureFeature<C> feature;
    protected BoundingBox boundingBox;
    private final int chunkX;
    private final int chunkZ;
    private int references;
    protected final List<StructurePiece> pieces = Lists.newArrayList();
    protected final WorldgenRandom random = new WorldgenRandom();

    public abstract void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int i2, Biome biome, C c);

    public StructureStart(StructureFeature<C> structureFeature, int i, int i2, BoundingBox boundingBox, int i3, long j) {
        this.feature = structureFeature;
        this.chunkX = i;
        this.chunkZ = i2;
        this.references = i3;
        this.random.setLargeFeatureSeed(j, i, i2);
        this.boundingBox = boundingBox;
    }

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public List<StructurePiece> getPieces() {
        return this.pieces;
    }

    public void placeInChunk(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos) {
        synchronized (this.pieces) {
            if (this.pieces.isEmpty()) {
                return;
            }
            BoundingBox boundingBox2 = this.pieces.get(0).boundingBox;
            Vec3i center = boundingBox2.getCenter();
            BlockPos blockPos = new BlockPos(center.getX(), boundingBox2.y0, center.getZ());
            Iterator<StructurePiece> it = this.pieces.iterator();
            while (it.hasNext()) {
                StructurePiece next = it.next();
                if (next.getBoundingBox().intersects(boundingBox) && !next.postProcess(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, blockPos)) {
                    it.remove();
                }
            }
            calculateBoundingBox();
        }
    }

    protected void calculateBoundingBox() {
        this.boundingBox = BoundingBox.getUnknownBox();
        Iterator<StructurePiece> it = this.pieces.iterator();
        while (it.hasNext()) {
            this.boundingBox.expand(it.next().getBoundingBox());
        }
    }

    public CompoundTag createTag(int i, int i2) {
        CompoundTag compoundTag = new CompoundTag();
        if (isValid()) {
            compoundTag.putString("id", Registry.STRUCTURE_FEATURE.getKey(getFeature()).toString());
            compoundTag.putInt("ChunkX", i);
            compoundTag.putInt("ChunkZ", i2);
            compoundTag.putInt("references", this.references);
            compoundTag.put("BB", this.boundingBox.createTag());
            ListTag listTag = new ListTag();
            synchronized (this.pieces) {
                Iterator<StructurePiece> it = this.pieces.iterator();
                while (it.hasNext()) {
                    listTag.add(it.next().createTag());
                }
            }
            compoundTag.put("Children", listTag);
            return compoundTag;
        }
        compoundTag.putString("id", "INVALID");
        return compoundTag;
    }

    protected void moveBelowSeaLevel(int i, Random random, int i2) {
        int i3 = i - i2;
        int ySpan = this.boundingBox.getYSpan() + 1;
        if (ySpan < i3) {
            ySpan += random.nextInt(i3 - ySpan);
        }
        int i4 = ySpan - this.boundingBox.y1;
        this.boundingBox.move(0, i4, 0);
        Iterator<StructurePiece> it = this.pieces.iterator();
        while (it.hasNext()) {
            it.next().move(0, i4, 0);
        }
    }

    protected void moveInsideHeights(Random random, int i, int i2) {
        int i3;
        int ySpan = ((i2 - i) + 1) - this.boundingBox.getYSpan();
        if (ySpan > 1) {
            i3 = i + random.nextInt(ySpan);
        } else {
            i3 = i;
        }
        int i4 = i3 - this.boundingBox.y0;
        this.boundingBox.move(0, i4, 0);
        Iterator<StructurePiece> it = this.pieces.iterator();
        while (it.hasNext()) {
            it.next().move(0, i4, 0);
        }
    }

    public boolean isValid() {
        return !this.pieces.isEmpty();
    }

    public int getChunkX() {
        return this.chunkX;
    }

    public int getChunkZ() {
        return this.chunkZ;
    }

    public BlockPos getLocatePos() {
        return new BlockPos(this.chunkX << 4, 0, this.chunkZ << 4);
    }

    public boolean canBeReferenced() {
        return this.references < getMaxReferences();
    }

    public void addReference() {
        this.references++;
    }

    public int getReferences() {
        return this.references;
    }

    protected int getMaxReferences() {
        return 1;
    }

    public StructureFeature<?> getFeature() {
        return this.feature;
    }
}
