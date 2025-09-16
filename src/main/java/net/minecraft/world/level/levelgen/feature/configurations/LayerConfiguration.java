package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/LayerConfiguration.class */
public class LayerConfiguration implements FeatureConfiguration {
    public static final Codec<LayerConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.intRange(0, 255).fieldOf("height").forGetter(layerConfiguration -> {
            return Integer.valueOf(layerConfiguration.height);
        }), BlockState.CODEC.fieldOf("state").forGetter(layerConfiguration2 -> {
            return layerConfiguration2.state;
        })).apply(instance, (v1, v2) -> {
            return new LayerConfiguration(v1, v2);
        });
    });
    public final int height;
    public final BlockState state;

    public LayerConfiguration(int i, BlockState blockState) {
        this.height = i;
        this.state = blockState;
    }
}
