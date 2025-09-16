package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/ReplaceSphereConfiguration.class */
public class ReplaceSphereConfiguration implements FeatureConfiguration {
    public static final Codec<ReplaceSphereConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(BlockState.CODEC.fieldOf("target").forGetter(replaceSphereConfiguration -> {
            return replaceSphereConfiguration.targetState;
        }), BlockState.CODEC.fieldOf("state").forGetter(replaceSphereConfiguration2 -> {
            return replaceSphereConfiguration2.replaceState;
        }), UniformInt.CODEC.fieldOf("radius").forGetter(replaceSphereConfiguration3 -> {
            return replaceSphereConfiguration3.radius;
        })).apply(instance, ReplaceSphereConfiguration::new);
    });
    public final BlockState targetState;
    public final BlockState replaceState;
    private final UniformInt radius;

    public ReplaceSphereConfiguration(BlockState blockState, BlockState blockState2, UniformInt uniformInt) {
        this.targetState = blockState;
        this.replaceState = blockState2;
        this.radius = uniformInt;
    }

    public UniformInt radius() {
        return this.radius;
    }
}
