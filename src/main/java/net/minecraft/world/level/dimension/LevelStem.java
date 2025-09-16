package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/dimension/LevelStem.class */
public final class LevelStem {
    public static final Codec<LevelStem> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(DimensionType.CODEC.fieldOf("type").forGetter((v0) -> {
            return v0.typeSupplier();
        }), ChunkGenerator.CODEC.fieldOf("generator").forGetter((v0) -> {
            return v0.generator();
        })).apply(instance, instance.stable(LevelStem::new));
    });
    public static final ResourceKey<LevelStem> OVERWORLD = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<LevelStem> NETHER = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<LevelStem> END = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_end"));
    private static final LinkedHashSet<ResourceKey<LevelStem>> BUILTIN_ORDER = Sets.newLinkedHashSet(ImmutableList.of(OVERWORLD, NETHER, END));
    private final Supplier<DimensionType> type;
    private final ChunkGenerator generator;

    public LevelStem(Supplier<DimensionType> supplier, ChunkGenerator chunkGenerator) {
        this.type = supplier;
        this.generator = chunkGenerator;
    }

    public Supplier<DimensionType> typeSupplier() {
        return this.type;
    }

    public DimensionType type() {
        return this.type.get();
    }

    public ChunkGenerator generator() {
        return this.generator;
    }

    public static MappedRegistry<LevelStem> sortMap(MappedRegistry<LevelStem> mappedRegistry) {
        MappedRegistry<LevelStem> mappedRegistry2 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());
        Iterator<ResourceKey<LevelStem>> it = BUILTIN_ORDER.iterator();
        while (it.hasNext()) {
            ResourceKey<LevelStem> next = it.next();
            LevelStem levelStem = mappedRegistry.get(next);
            if (levelStem != null) {
                mappedRegistry2.register(next,  levelStem, mappedRegistry.lifecycle(levelStem));
            }
        }
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : mappedRegistry.entrySet()) {
            ResourceKey<LevelStem> key = entry.getKey();
            if (!BUILTIN_ORDER.contains(key)) {
                mappedRegistry2.register(key, entry.getValue(), mappedRegistry.lifecycle(entry.getValue()));
            }
        }
        return mappedRegistry2;
    }

    public static boolean stable(long j, MappedRegistry<LevelStem> mappedRegistry) {
        List<Map.Entry<ResourceKey<LevelStem>, LevelStem>> newArrayList = Lists.newArrayList(mappedRegistry.entrySet());
        if (newArrayList.size() != BUILTIN_ORDER.size()) {
            return false;
        }
        Map.Entry<ResourceKey<LevelStem>, LevelStem> entry = newArrayList.get(0);
        Map.Entry<ResourceKey<LevelStem>, LevelStem> entry2 = newArrayList.get(1);
        Map.Entry<ResourceKey<LevelStem>, LevelStem> entry3 = newArrayList.get(2);
        if (entry.getKey() != OVERWORLD || entry2.getKey() != NETHER || entry3.getKey() != END) {
            return false;
        }
        if ((!entry.getValue().type().equalTo(DimensionType.DEFAULT_OVERWORLD) && entry.getValue().type() != DimensionType.DEFAULT_OVERWORLD_CAVES) || !entry2.getValue().type().equalTo(DimensionType.DEFAULT_NETHER) || !entry3.getValue().type().equalTo(DimensionType.DEFAULT_END) || !(entry2.getValue().generator() instanceof NoiseBasedChunkGenerator) || !(entry3.getValue().generator() instanceof NoiseBasedChunkGenerator)) {
            return false;
        }
        NoiseBasedChunkGenerator noiseBasedChunkGenerator = (NoiseBasedChunkGenerator) entry2.getValue().generator();
        NoiseBasedChunkGenerator noiseBasedChunkGenerator2 = (NoiseBasedChunkGenerator) entry3.getValue().generator();
        if (!noiseBasedChunkGenerator.stable(j, NoiseGeneratorSettings.NETHER) || !noiseBasedChunkGenerator2.stable(j, NoiseGeneratorSettings.END) || !(noiseBasedChunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource) || !((MultiNoiseBiomeSource) noiseBasedChunkGenerator.getBiomeSource()).stable(j) || !(noiseBasedChunkGenerator2.getBiomeSource() instanceof TheEndBiomeSource) || !((TheEndBiomeSource) noiseBasedChunkGenerator2.getBiomeSource()).stable(j)) {
            return false;
        }
        return true;
    }
}
