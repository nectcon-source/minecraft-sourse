package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/BiomeSpecialEffects.class */
public class BiomeSpecialEffects {
    public static final Codec<BiomeSpecialEffects> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.INT.fieldOf("fog_color").forGetter(biomeSpecialEffects -> {
            return Integer.valueOf(biomeSpecialEffects.fogColor);
        }), Codec.INT.fieldOf("water_color").forGetter(biomeSpecialEffects2 -> {
            return Integer.valueOf(biomeSpecialEffects2.waterColor);
        }), Codec.INT.fieldOf("water_fog_color").forGetter(biomeSpecialEffects3 -> {
            return Integer.valueOf(biomeSpecialEffects3.waterFogColor);
        }), Codec.INT.fieldOf("sky_color").forGetter(biomeSpecialEffects4 -> {
            return Integer.valueOf(biomeSpecialEffects4.skyColor);
        }), Codec.INT.optionalFieldOf("foliage_color").forGetter(biomeSpecialEffects5 -> {
            return biomeSpecialEffects5.foliageColorOverride;
        }), Codec.INT.optionalFieldOf("grass_color").forGetter(biomeSpecialEffects6 -> {
            return biomeSpecialEffects6.grassColorOverride;
        }), GrassColorModifier.CODEC.optionalFieldOf("grass_color_modifier", GrassColorModifier.NONE).forGetter(biomeSpecialEffects7 -> {
            return biomeSpecialEffects7.grassColorModifier;
        }), AmbientParticleSettings.CODEC.optionalFieldOf("particle").forGetter(biomeSpecialEffects8 -> {
            return biomeSpecialEffects8.ambientParticleSettings;
        }), SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter(biomeSpecialEffects9 -> {
            return biomeSpecialEffects9.ambientLoopSoundEvent;
        }), AmbientMoodSettings.CODEC.optionalFieldOf("mood_sound").forGetter(biomeSpecialEffects10 -> {
            return biomeSpecialEffects10.ambientMoodSettings;
        }), AmbientAdditionsSettings.CODEC.optionalFieldOf("additions_sound").forGetter(biomeSpecialEffects11 -> {
            return biomeSpecialEffects11.ambientAdditionsSettings;
        }), Music.CODEC.optionalFieldOf("music").forGetter(biomeSpecialEffects12 -> {
            return biomeSpecialEffects12.backgroundMusic;
        })).apply(instance, (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12) -> {
            return new BiomeSpecialEffects(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12);
        });
    });
    private final int fogColor;
    private final int waterColor;
    private final int waterFogColor;
    private final int skyColor;
    private final Optional<Integer> foliageColorOverride;
    private final Optional<Integer> grassColorOverride;
    private final GrassColorModifier grassColorModifier;
    private final Optional<AmbientParticleSettings> ambientParticleSettings;
    private final Optional<SoundEvent> ambientLoopSoundEvent;
    private final Optional<AmbientMoodSettings> ambientMoodSettings;
    private final Optional<AmbientAdditionsSettings> ambientAdditionsSettings;
    private final Optional<Music> backgroundMusic;

    private BiomeSpecialEffects(int i, int i2, int i3, int i4, Optional<Integer> optional, Optional<Integer> optional2, GrassColorModifier grassColorModifier, Optional<AmbientParticleSettings> optional3, Optional<SoundEvent> optional4, Optional<AmbientMoodSettings> optional5, Optional<AmbientAdditionsSettings> optional6, Optional<Music> optional7) {
        this.fogColor = i;
        this.waterColor = i2;
        this.waterFogColor = i3;
        this.skyColor = i4;
        this.foliageColorOverride = optional;
        this.grassColorOverride = optional2;
        this.grassColorModifier = grassColorModifier;
        this.ambientParticleSettings = optional3;
        this.ambientLoopSoundEvent = optional4;
        this.ambientMoodSettings = optional5;
        this.ambientAdditionsSettings = optional6;
        this.backgroundMusic = optional7;
    }

    public int getFogColor() {
        return this.fogColor;
    }

    public int getWaterColor() {
        return this.waterColor;
    }

    public int getWaterFogColor() {
        return this.waterFogColor;
    }

    public int getSkyColor() {
        return this.skyColor;
    }

    public Optional<Integer> getFoliageColorOverride() {
        return this.foliageColorOverride;
    }

    public Optional<Integer> getGrassColorOverride() {
        return this.grassColorOverride;
    }

    public GrassColorModifier getGrassColorModifier() {
        return this.grassColorModifier;
    }

    public Optional<AmbientParticleSettings> getAmbientParticleSettings() {
        return this.ambientParticleSettings;
    }

    public Optional<SoundEvent> getAmbientLoopSoundEvent() {
        return this.ambientLoopSoundEvent;
    }

    public Optional<AmbientMoodSettings> getAmbientMoodSettings() {
        return this.ambientMoodSettings;
    }

    public Optional<AmbientAdditionsSettings> getAmbientAdditionsSettings() {
        return this.ambientAdditionsSettings;
    }

    public Optional<Music> getBackgroundMusic() {
        return this.backgroundMusic;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/BiomeSpecialEffects$Builder.class */
    public static class Builder {
        private OptionalInt fogColor = OptionalInt.empty();
        private OptionalInt waterColor = OptionalInt.empty();
        private OptionalInt waterFogColor = OptionalInt.empty();
        private OptionalInt skyColor = OptionalInt.empty();
        private Optional<Integer> foliageColorOverride = Optional.empty();
        private Optional<Integer> grassColorOverride = Optional.empty();
        private GrassColorModifier grassColorModifier = GrassColorModifier.NONE;
        private Optional<AmbientParticleSettings> ambientParticle = Optional.empty();
        private Optional<SoundEvent> ambientLoopSoundEvent = Optional.empty();
        private Optional<AmbientMoodSettings> ambientMoodSettings = Optional.empty();
        private Optional<AmbientAdditionsSettings> ambientAdditionsSettings = Optional.empty();
        private Optional<Music> backgroundMusic = Optional.empty();

        public Builder fogColor(int i) {
            this.fogColor = OptionalInt.of(i);
            return this;
        }

        public Builder waterColor(int i) {
            this.waterColor = OptionalInt.of(i);
            return this;
        }

        public Builder waterFogColor(int i) {
            this.waterFogColor = OptionalInt.of(i);
            return this;
        }

        public Builder skyColor(int i) {
            this.skyColor = OptionalInt.of(i);
            return this;
        }

        public Builder foliageColorOverride(int i) {
            this.foliageColorOverride = Optional.of(Integer.valueOf(i));
            return this;
        }

        public Builder grassColorOverride(int i) {
            this.grassColorOverride = Optional.of(Integer.valueOf(i));
            return this;
        }

        public Builder grassColorModifier(GrassColorModifier grassColorModifier) {
            this.grassColorModifier = grassColorModifier;
            return this;
        }

        public Builder ambientParticle(AmbientParticleSettings ambientParticleSettings) {
            this.ambientParticle = Optional.of(ambientParticleSettings);
            return this;
        }

        public Builder ambientLoopSound(SoundEvent soundEvent) {
            this.ambientLoopSoundEvent = Optional.of(soundEvent);
            return this;
        }

        public Builder ambientMoodSound(AmbientMoodSettings ambientMoodSettings) {
            this.ambientMoodSettings = Optional.of(ambientMoodSettings);
            return this;
        }

        public Builder ambientAdditionsSound(AmbientAdditionsSettings ambientAdditionsSettings) {
            this.ambientAdditionsSettings = Optional.of(ambientAdditionsSettings);
            return this;
        }

        public Builder backgroundMusic(Music music) {
            this.backgroundMusic = Optional.of(music);
            return this;
        }

        public BiomeSpecialEffects build() {
            return new BiomeSpecialEffects(this.fogColor.orElseThrow(() -> {
                return new IllegalStateException("Missing 'fog' color.");
            }), this.waterColor.orElseThrow(() -> {
                return new IllegalStateException("Missing 'water' color.");
            }), this.waterFogColor.orElseThrow(() -> {
                return new IllegalStateException("Missing 'water fog' color.");
            }), this.skyColor.orElseThrow(() -> {
                return new IllegalStateException("Missing 'sky' color.");
            }), this.foliageColorOverride, this.grassColorOverride, this.grassColorModifier, this.ambientParticle, this.ambientLoopSoundEvent, this.ambientMoodSettings, this.ambientAdditionsSettings, this.backgroundMusic);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/biome/BiomeSpecialEffects$GrassColorModifier.class */
    public enum GrassColorModifier implements StringRepresentable {
        NONE("none") { // from class: net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier.1
            @Override // net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier
            public int modifyColor(double d, double d2, int i) {
                return i;
            }
        },
        DARK_FOREST("dark_forest") { // from class: net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier.2
            @Override // net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier
            public int modifyColor(double d, double d2, int i) {
                return ((i & 16711422) + 2634762) >> 1;
            }
        },
        SWAMP("swamp") { // from class: net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier.3
            @Override // net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier
            public int modifyColor(double d, double d2, int i) {
                if (Biome.BIOME_INFO_NOISE.getValue(d * 0.0225d, d2 * 0.0225d, false) < -0.1d) {
                    return 5011004;
                }
                return 6975545;
            }
        };

        private final String name;
        public static final Codec<GrassColorModifier> CODEC = StringRepresentable.fromEnum(GrassColorModifier::values, GrassColorModifier::byName);
        private static final Map<String, GrassColorModifier> BY_NAME = (Map) Arrays.stream(values()).collect(Collectors.toMap((v0) -> {
            return v0.getName();
        }, grassColorModifier -> {
            return grassColorModifier;
        }));

        public abstract int modifyColor(double d, double d2, int i);

        GrassColorModifier(String str) {
            this.name = str;
        }

        public String getName() {
            return this.name;
        }

        @Override // net.minecraft.util.StringRepresentable
        public String getSerializedName() {
            return this.name;
        }

        public static GrassColorModifier byName(String str) {
            return BY_NAME.get(str);
        }
    }
}
