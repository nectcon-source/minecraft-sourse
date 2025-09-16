package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/AmbientParticleSettings.class */
public class AmbientParticleSettings {
    public static final Codec<AmbientParticleSettings> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(ParticleTypes.CODEC.fieldOf("options").forGetter(ambientParticleSettings -> {
            return ambientParticleSettings.options;
        }), Codec.FLOAT.fieldOf("probability").forGetter(ambientParticleSettings2 -> {
            return Float.valueOf(ambientParticleSettings2.probability);
        })).apply(instance, (v1, v2) -> {
            return new AmbientParticleSettings(v1, v2);
        });
    });
    private final ParticleOptions options;
    private final float probability;

    public AmbientParticleSettings(ParticleOptions particleOptions, float f) {
        this.options = particleOptions;
        this.probability = f;
    }

    public ParticleOptions getOptions() {
        return this.options;
    }

    public boolean canSpawn(Random random) {
        return random.nextFloat() <= this.probability;
    }
}
