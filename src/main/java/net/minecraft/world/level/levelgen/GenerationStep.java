package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.StringRepresentable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/GenerationStep.class */
public class GenerationStep {

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/GenerationStep$Decoration.class */
    public enum Decoration {
        RAW_GENERATION,
        LAKES,
        LOCAL_MODIFICATIONS,
        UNDERGROUND_STRUCTURES,
        SURFACE_STRUCTURES,
        STRONGHOLDS,
        UNDERGROUND_ORES,
        UNDERGROUND_DECORATION,
        VEGETAL_DECORATION,
        TOP_LAYER_MODIFICATION
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/GenerationStep$Carving.class */
    public enum Carving implements StringRepresentable {
        AIR("air"),
        LIQUID("liquid");

        public static final Codec<Carving> CODEC = StringRepresentable.fromEnum(Carving::values, Carving::byName);
        private static final Map<String, Carving> BY_NAME = (Map) Arrays.stream(values()).collect(Collectors.toMap((v0) -> {
            return v0.getName();
        }, carving -> {
            return carving;
        }));
        private final String name;

        Carving(String str) {
            this.name = str;
        }

        public String getName() {
            return this.name;
        }

        @Nullable
        public static Carving byName(String str) {
            return BY_NAME.get(str);
        }

        @Override // net.minecraft.util.StringRepresentable
        public String getSerializedName() {
            return this.name;
        }
    }
}
