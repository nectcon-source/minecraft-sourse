package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/ReplaceBlockConfiguration.class */
public class ReplaceBlockConfiguration implements FeatureConfiguration {
    public static final Codec<ReplaceBlockConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(BlockState.CODEC.fieldOf("target").forGetter(replaceBlockConfiguration -> {
            return replaceBlockConfiguration.target;
        }), BlockState.CODEC.fieldOf("state").forGetter(replaceBlockConfiguration2 -> {
            return replaceBlockConfiguration2.state;
        })).apply(instance, ReplaceBlockConfiguration::new);
    });
    public final BlockState target;
    public final BlockState state;

    public ReplaceBlockConfiguration(BlockState blockState, BlockState blockState2) {
        this.target = blockState;
        this.state = blockState2;
    }
}
