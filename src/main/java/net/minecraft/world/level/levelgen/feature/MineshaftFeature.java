package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.MineShaftPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/MineshaftFeature.class */
public class MineshaftFeature extends StructureFeature<MineshaftConfiguration> {
    public MineshaftFeature(Codec<MineshaftConfiguration> codec) {
        super(codec);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long j, WorldgenRandom worldgenRandom, int i, int i2, Biome biome, ChunkPos chunkPos, MineshaftConfiguration mineshaftConfiguration) {
        worldgenRandom.setLargeFeatureSeed(j, i, i2);
        return worldgenRandom.nextDouble() < ((double) mineshaftConfiguration.probability);
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public StructureFeature.StructureStartFactory<MineshaftConfiguration> getStartFactory() {
        return MineShaftStart::new;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/MineshaftFeature$Type.class */
    public enum Type implements StringRepresentable {
        NORMAL("normal"),
        MESA("mesa");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values, Type::byName);
        private static final Map<String, Type> BY_NAME = (Map) Arrays.stream(values()).collect(Collectors.toMap((v0) -> {
            return v0.getName();
        }, type -> {
            return type;
        }));
        private final String name;

        Type(String str) {
            this.name = str;
        }

        public String getName() {
            return this.name;
        }

        private static Type byName(String str) {
            return BY_NAME.get(str);
        }

        public static Type byId(int i) {
            if (i < 0 || i >= values().length) {
                return NORMAL;
            }
            return values()[i];
        }

        @Override // net.minecraft.util.StringRepresentable
        public String getSerializedName() {
            return this.name;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/MineshaftFeature$MineShaftStart.class */
    public static class MineShaftStart extends StructureStart<MineshaftConfiguration> {
        public MineShaftStart(StructureFeature<MineshaftConfiguration> structureFeature, int i, int i2, BoundingBox boundingBox, int i3, long j) {
            super(structureFeature, i, i2, boundingBox, i3, j);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int i2, Biome biome, MineshaftConfiguration mineshaftConfiguration) {
            MineShaftPieces.MineShaftRoom mineShaftRoom = new MineShaftPieces.MineShaftRoom(0, this.random, (i << 4) + 2, (i2 << 4) + 2, mineshaftConfiguration.type);
            this.pieces.add(mineShaftRoom);
            mineShaftRoom.addChildren(mineShaftRoom, this.pieces, this.random);
            calculateBoundingBox();
            if (mineshaftConfiguration.type == Type.MESA) {
                int seaLevel = ((chunkGenerator.getSeaLevel() - this.boundingBox.y1) + (this.boundingBox.getYSpan() / 2)) - (-5);
                this.boundingBox.move(0, seaLevel, 0);
                Iterator<StructurePiece> it = this.pieces.iterator();
                while (it.hasNext()) {
                    it.next().move(0, seaLevel, 0);
                }
                return;
            }
            moveBelowSeaLevel(chunkGenerator.getSeaLevel(), this.random, 10);
        }
    }
}
