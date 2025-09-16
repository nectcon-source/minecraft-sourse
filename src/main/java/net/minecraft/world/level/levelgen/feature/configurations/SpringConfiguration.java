package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/SpringConfiguration.class */
public class SpringConfiguration implements FeatureConfiguration {
    public static final Codec<SpringConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(FluidState.CODEC.fieldOf("state").forGetter(springConfiguration -> {
            return springConfiguration.state;
        }), Codec.BOOL.fieldOf("requires_block_below").orElse(true).forGetter(springConfiguration2 -> {
            return Boolean.valueOf(springConfiguration2.requiresBlockBelow);
        }), Codec.INT.fieldOf("rock_count").orElse(4).forGetter(springConfiguration3 -> {
            return Integer.valueOf(springConfiguration3.rockCount);
        }), Codec.INT.fieldOf("hole_count").orElse(1).forGetter(springConfiguration4 -> {
            return Integer.valueOf(springConfiguration4.holeCount);
        }), Registry.BLOCK.listOf().fieldOf("valid_blocks").xmap((v0) -> {
            return ImmutableSet.copyOf(v0);
        }, (v0) -> {
            return ImmutableList.copyOf(v0);
        }).forGetter(springConfiguration5 -> {
            return (ImmutableSet<Block>) springConfiguration5.validBlocks;
        })).apply(instance, (v1, v2, v3, v4, v5) -> {
            return new SpringConfiguration(v1, v2, v3, v4, v5);
        });
    });
    public final FluidState state;
    public final boolean requiresBlockBelow;
    public final int rockCount;
    public final int holeCount;
    public final Set<Block> validBlocks;

    public SpringConfiguration(FluidState fluidState, boolean z, int i, int i2, Set<Block> set) {
        this.state = fluidState;
        this.requiresBlockBelow = z;
        this.rockCount = i;
        this.holeCount = i2;
        this.validBlocks = set;
    }
}
