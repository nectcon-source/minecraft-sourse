package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/StructureFeatureConfiguration.class */
public class StructureFeatureConfiguration {
    public static final Codec<StructureFeatureConfiguration> CODEC = RecordCodecBuilder.<StructureFeatureConfiguration>create((var0) -> var0.group(Codec.intRange(0, 4096).fieldOf("spacing").forGetter((var0x) -> var0x.spacing), Codec.intRange(0, 4096).fieldOf("separation").forGetter((var0x) -> var0x.separation), Codec.intRange(0, Integer.MAX_VALUE).fieldOf("salt").forGetter((var0x) -> var0x.salt)).apply(var0, StructureFeatureConfiguration::new)).comapFlatMap((var0) -> var0.spacing <= var0.separation ? DataResult.error("Spacing has to be smaller than separation") : DataResult.success(var0), Function.identity());
    private final int spacing;
    private final int separation;
    private final int salt;

    public StructureFeatureConfiguration(int i, int i2, int i3) {
        this.spacing = i;
        this.separation = i2;
        this.salt = i3;
    }

    public int spacing() {
        return this.spacing;
    }

    public int separation() {
        return this.separation;
    }

    public int salt() {
        return this.salt;
    }
}
