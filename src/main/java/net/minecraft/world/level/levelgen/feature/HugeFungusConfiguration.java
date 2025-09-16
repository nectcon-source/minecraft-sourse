package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/HugeFungusConfiguration.class */
public class HugeFungusConfiguration implements FeatureConfiguration {
    public static final Codec<HugeFungusConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(BlockState.CODEC.fieldOf("valid_base_block").forGetter(hugeFungusConfiguration -> {
            return hugeFungusConfiguration.validBaseState;
        }), BlockState.CODEC.fieldOf("stem_state").forGetter(hugeFungusConfiguration2 -> {
            return hugeFungusConfiguration2.stemState;
        }), BlockState.CODEC.fieldOf("hat_state").forGetter(hugeFungusConfiguration3 -> {
            return hugeFungusConfiguration3.hatState;
        }), BlockState.CODEC.fieldOf("decor_state").forGetter(hugeFungusConfiguration4 -> {
            return hugeFungusConfiguration4.decorState;
        }), Codec.BOOL.fieldOf("planted").orElse(false).forGetter(hugeFungusConfiguration5 -> {
            return Boolean.valueOf(hugeFungusConfiguration5.planted);
        })).apply(instance, (v1, v2, v3, v4, v5) -> {
            return new HugeFungusConfiguration(v1, v2, v3, v4, v5);
        });
    });
    public static final HugeFungusConfiguration HUGE_CRIMSON_FUNGI_PLANTED_CONFIG = new HugeFungusConfiguration(Blocks.CRIMSON_NYLIUM.defaultBlockState(), Blocks.CRIMSON_STEM.defaultBlockState(), Blocks.NETHER_WART_BLOCK.defaultBlockState(), Blocks.SHROOMLIGHT.defaultBlockState(), true);
    public static final HugeFungusConfiguration HUGE_CRIMSON_FUNGI_NOT_PLANTED_CONFIG = new HugeFungusConfiguration(HUGE_CRIMSON_FUNGI_PLANTED_CONFIG.validBaseState, HUGE_CRIMSON_FUNGI_PLANTED_CONFIG.stemState, HUGE_CRIMSON_FUNGI_PLANTED_CONFIG.hatState, HUGE_CRIMSON_FUNGI_PLANTED_CONFIG.decorState, false);
    public static final HugeFungusConfiguration HUGE_WARPED_FUNGI_PLANTED_CONFIG = new HugeFungusConfiguration(Blocks.WARPED_NYLIUM.defaultBlockState(), Blocks.WARPED_STEM.defaultBlockState(), Blocks.WARPED_WART_BLOCK.defaultBlockState(), Blocks.SHROOMLIGHT.defaultBlockState(), true);
    public static final HugeFungusConfiguration HUGE_WARPED_FUNGI_NOT_PLANTED_CONFIG = new HugeFungusConfiguration(HUGE_WARPED_FUNGI_PLANTED_CONFIG.validBaseState, HUGE_WARPED_FUNGI_PLANTED_CONFIG.stemState, HUGE_WARPED_FUNGI_PLANTED_CONFIG.hatState, HUGE_WARPED_FUNGI_PLANTED_CONFIG.decorState, false);
    public final BlockState validBaseState;
    public final BlockState stemState;
    public final BlockState hatState;
    public final BlockState decorState;
    public final boolean planted;

    public HugeFungusConfiguration(BlockState blockState, BlockState blockState2, BlockState blockState3, BlockState blockState4, boolean z) {
        this.validBaseState = blockState;
        this.stemState = blockState2;
        this.hatState = blockState3;
        this.decorState = blockState4;
        this.planted = z;
    }
}
