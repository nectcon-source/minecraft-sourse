package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanRuinFeature.class */
public class OceanRuinFeature extends StructureFeature<OceanRuinConfiguration> {
    public OceanRuinFeature(Codec<OceanRuinConfiguration> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.feature.StructureFeature
    public StructureFeature.StructureStartFactory<OceanRuinConfiguration> getStartFactory() {
        return OceanRuinStart::new;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanRuinFeature$OceanRuinStart.class */
    public static class OceanRuinStart extends StructureStart<OceanRuinConfiguration> {
        public OceanRuinStart(StructureFeature<OceanRuinConfiguration> structureFeature, int i, int i2, BoundingBox boundingBox, int i3, long j) {
            super(structureFeature, i, i2, boundingBox, i3, j);
        }

        @Override // net.minecraft.world.level.levelgen.structure.StructureStart
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int i2, Biome biome, OceanRuinConfiguration oceanRuinConfiguration) {
            OceanRuinPieces.addPieces(structureManager, new BlockPos(i * 16, 90, i2 * 16), Rotation.getRandom(this.random), this.pieces, this.random, oceanRuinConfiguration);
            calculateBoundingBox();
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/OceanRuinFeature$Type.class */
    public enum Type implements StringRepresentable {
        WARM("warm"),
        COLD("cold");

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

        @Nullable
        public static Type byName(String str) {
            return BY_NAME.get(str);
        }

        @Override // net.minecraft.util.StringRepresentable
        public String getSerializedName() {
            return this.name;
        }
    }
}
