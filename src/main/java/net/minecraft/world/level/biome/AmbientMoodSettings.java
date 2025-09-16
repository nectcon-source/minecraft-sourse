package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/AmbientMoodSettings.class */
public class AmbientMoodSettings {
    public static final Codec<AmbientMoodSettings> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(SoundEvent.CODEC.fieldOf("sound").forGetter(ambientMoodSettings -> {
            return ambientMoodSettings.soundEvent;
        }), Codec.INT.fieldOf("tick_delay").forGetter(ambientMoodSettings2 -> {
            return Integer.valueOf(ambientMoodSettings2.tickDelay);
        }), Codec.INT.fieldOf("block_search_extent").forGetter(ambientMoodSettings3 -> {
            return Integer.valueOf(ambientMoodSettings3.blockSearchExtent);
        }), Codec.DOUBLE.fieldOf("offset").forGetter(ambientMoodSettings4 -> {
            return Double.valueOf(ambientMoodSettings4.soundPositionOffset);
        })).apply(instance, (v1, v2, v3, v4) -> {
            return new AmbientMoodSettings(v1, v2, v3, v4);
        });
    });
    public static final AmbientMoodSettings LEGACY_CAVE_SETTINGS = new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0d);
    private SoundEvent soundEvent;
    private int tickDelay;
    private int blockSearchExtent;
    private double soundPositionOffset;

    public AmbientMoodSettings(SoundEvent soundEvent, int i, int i2, double d) {
        this.soundEvent = soundEvent;
        this.tickDelay = i;
        this.blockSearchExtent = i2;
        this.soundPositionOffset = d;
    }

    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    public int getTickDelay() {
        return this.tickDelay;
    }

    public int getBlockSearchExtent() {
        return this.blockSearchExtent;
    }

    public double getSoundPositionOffset() {
        return this.soundPositionOffset;
    }
}
