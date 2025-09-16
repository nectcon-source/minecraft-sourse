package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/placement/EdgeDecorator.class */
public abstract class EdgeDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
    protected abstract Heightmap.Types type(DC dc);

    public EdgeDecorator(Codec<DC> codec) {
        super(codec);
    }
}
