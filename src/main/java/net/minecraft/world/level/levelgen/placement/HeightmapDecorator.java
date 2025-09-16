package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/HeightmapDecorator.class */
public class HeightmapDecorator<DC extends DecoratorConfiguration> extends BaseHeightmapDecorator<DC> {
    public HeightmapDecorator(Codec<DC> codec) {
        super(codec);
    }

    @Override // net.minecraft.world.level.levelgen.placement.EdgeDecorator
    protected Heightmap.Types type(DC dc) {
        return Heightmap.Types.MOTION_BLOCKING;
    }
}
