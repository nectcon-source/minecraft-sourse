package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/surfacebuilders/SurfaceBuilderBaseConfiguration.class */
public class SurfaceBuilderBaseConfiguration implements SurfaceBuilderConfiguration {
    public static final Codec<SurfaceBuilderBaseConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(BlockState.CODEC.fieldOf("top_material").forGetter(surfaceBuilderBaseConfiguration -> {
            return surfaceBuilderBaseConfiguration.topMaterial;
        }), BlockState.CODEC.fieldOf("under_material").forGetter(surfaceBuilderBaseConfiguration2 -> {
            return surfaceBuilderBaseConfiguration2.underMaterial;
        }), BlockState.CODEC.fieldOf("underwater_material").forGetter(surfaceBuilderBaseConfiguration3 -> {
            return surfaceBuilderBaseConfiguration3.underwaterMaterial;
        })).apply(instance, SurfaceBuilderBaseConfiguration::new);
    });
    private final BlockState topMaterial;
    private final BlockState underMaterial;
    private final BlockState underwaterMaterial;

    public SurfaceBuilderBaseConfiguration(BlockState blockState, BlockState blockState2, BlockState blockState3) {
        this.topMaterial = blockState;
        this.underMaterial = blockState2;
        this.underwaterMaterial = blockState3;
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration
    public BlockState getTopMaterial() {
        return this.topMaterial;
    }

    @Override // net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration
    public BlockState getUnderMaterial() {
        return this.underMaterial;
    }

    public BlockState getUnderwaterMaterial() {
        return this.underwaterMaterial;
    }
}
