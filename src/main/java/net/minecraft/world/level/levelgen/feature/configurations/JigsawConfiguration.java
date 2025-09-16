package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/JigsawConfiguration.class */
public class JigsawConfiguration implements FeatureConfiguration {
    public static final Codec<JigsawConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter((v0) -> {
            return v0.startPool();
        }), Codec.intRange(0, 7).fieldOf("size").forGetter((v0) -> {
            return v0.maxDepth();
        })).apply(instance, (v1, v2) -> {
            return new JigsawConfiguration(v1, v2);
        });
    });
    private final Supplier<StructureTemplatePool> startPool;
    private final int maxDepth;

    public JigsawConfiguration(Supplier<StructureTemplatePool> supplier, int i) {
        this.startPool = supplier;
        this.maxDepth = i;
    }

    public int maxDepth() {
        return this.maxDepth;
    }

    public Supplier<StructureTemplatePool> startPool() {
        return this.startPool;
    }
}
