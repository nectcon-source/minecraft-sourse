package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/VillageFeature.class */
public class VillageFeature extends JigsawFeature {
    public VillageFeature(Codec<JigsawConfiguration> codec) {
        super(codec, 0, true, true);
    }
}
