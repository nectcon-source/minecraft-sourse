package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.sounds.SoundEvent;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/AmbientAdditionsSettings.class */
public class AmbientAdditionsSettings {
    public static final Codec<AmbientAdditionsSettings> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(SoundEvent.CODEC.fieldOf("sound").forGetter(ambientAdditionsSettings -> {
            return ambientAdditionsSettings.soundEvent;
        }), Codec.DOUBLE.fieldOf("tick_chance").forGetter(ambientAdditionsSettings2 -> {
            return Double.valueOf(ambientAdditionsSettings2.tickChance);
        })).apply(instance, (v1, v2) -> {
            return new AmbientAdditionsSettings(v1, v2);
        });
    });
    private SoundEvent soundEvent;
    private double tickChance;

    public AmbientAdditionsSettings(SoundEvent soundEvent, double d) {
        this.soundEvent = soundEvent;
        this.tickChance = d;
    }

    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    public double getTickChance() {
        return this.tickChance;
    }
}
