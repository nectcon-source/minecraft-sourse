package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/FrequencyWithExtraChanceDecoratorConfiguration.class */
public class FrequencyWithExtraChanceDecoratorConfiguration implements DecoratorConfiguration {
    public static final Codec<FrequencyWithExtraChanceDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.INT.fieldOf("count").forGetter(frequencyWithExtraChanceDecoratorConfiguration -> {
            return Integer.valueOf(frequencyWithExtraChanceDecoratorConfiguration.count);
        }), Codec.FLOAT.fieldOf("extra_chance").forGetter(frequencyWithExtraChanceDecoratorConfiguration2 -> {
            return Float.valueOf(frequencyWithExtraChanceDecoratorConfiguration2.extraChance);
        }), Codec.INT.fieldOf("extra_count").forGetter(frequencyWithExtraChanceDecoratorConfiguration3 -> {
            return Integer.valueOf(frequencyWithExtraChanceDecoratorConfiguration3.extraCount);
        })).apply(instance, (v1, v2, v3) -> {
            return new FrequencyWithExtraChanceDecoratorConfiguration(v1, v2, v3);
        });
    });
    public final int count;
    public final float extraChance;
    public final int extraCount;

    public FrequencyWithExtraChanceDecoratorConfiguration(int i, float f, int i2) {
        this.count = i;
        this.extraChance = f;
        this.extraCount = i2;
    }
}
