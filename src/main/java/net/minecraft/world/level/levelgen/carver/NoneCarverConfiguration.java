package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/carver/NoneCarverConfiguration.class */
public class NoneCarverConfiguration implements CarverConfiguration {

    public static final Codec<NoneCarverConfiguration> CODEC = Codec.unit(() -> NoneCarverConfiguration.INSTANCE);
    public static final NoneCarverConfiguration INSTANCE = new NoneCarverConfiguration();
}
