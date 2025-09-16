package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/DiskConfiguration.class */
public class DiskConfiguration implements FeatureConfiguration {
    public static final Codec<DiskConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(BlockState.CODEC.fieldOf("state").forGetter(diskConfiguration -> {
            return diskConfiguration.state;
        }), UniformInt.codec(0, 4, 4).fieldOf("radius").forGetter(diskConfiguration2 -> {
            return diskConfiguration2.radius;
        }), Codec.intRange(0, 4).fieldOf("half_height").forGetter(diskConfiguration3 -> {
            return Integer.valueOf(diskConfiguration3.halfHeight);
        }), BlockState.CODEC.listOf().fieldOf("targets").forGetter(diskConfiguration4 -> {
            return diskConfiguration4.targets;
        })).apply(instance, (v1, v2, v3, v4) -> {
            return new DiskConfiguration(v1, v2, v3, v4);
        });
    });
    public final BlockState state;
    public final UniformInt radius;
    public final int halfHeight;
    public final List<BlockState> targets;

    public DiskConfiguration(BlockState blockState, UniformInt uniformInt, int i, List<BlockState> list) {
        this.state = blockState;
        this.radius = uniformInt;
        this.halfHeight = i;
        this.targets = list;
    }
}
