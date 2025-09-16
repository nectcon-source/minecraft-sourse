package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/CarvingMaskDecoratorConfiguration.class */
public class CarvingMaskDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<CarvingMaskDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(GenerationStep.Carving.CODEC.fieldOf("step").forGetter(carvingMaskDecoratorConfiguration -> {
            return carvingMaskDecoratorConfiguration.step;
        }), Codec.FLOAT.fieldOf("probability").forGetter(carvingMaskDecoratorConfiguration2 -> {
            return Float.valueOf(carvingMaskDecoratorConfiguration2.probability);
        })).apply(instance, (v1, v2) -> {
            return new CarvingMaskDecoratorConfiguration(v1, v2);
        });
    });
    protected final GenerationStep.Carving step;
    protected final float probability;

    public CarvingMaskDecoratorConfiguration(GenerationStep.Carving carving, float f) {
        this.step = carving;
        this.probability = f;
    }
}
