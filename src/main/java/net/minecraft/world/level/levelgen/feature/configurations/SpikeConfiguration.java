package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/SpikeConfiguration.class */
public class SpikeConfiguration implements FeatureConfiguration {
    public static final Codec<SpikeConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.BOOL.fieldOf("crystal_invulnerable").orElse(false).forGetter(spikeConfiguration -> {
            return Boolean.valueOf(spikeConfiguration.crystalInvulnerable);
        }), SpikeFeature.EndSpike.CODEC.listOf().fieldOf("spikes").forGetter(spikeConfiguration2 -> {
            return spikeConfiguration2.spikes;
        }), BlockPos.CODEC.optionalFieldOf("crystal_beam_target").forGetter(spikeConfiguration3 -> {
            return Optional.ofNullable(spikeConfiguration3.crystalBeamTarget);
        })).apply(instance, (v1, v2, v3) -> {
            return new SpikeConfiguration(v1, v2, v3);
        });
    });
    private final boolean crystalInvulnerable;
    private final List<SpikeFeature.EndSpike> spikes;

    @Nullable
    private final BlockPos crystalBeamTarget;

    public SpikeConfiguration(boolean z, List<SpikeFeature.EndSpike> list, @Nullable BlockPos blockPos) {
        this(z, list, (Optional<BlockPos>) Optional.ofNullable(blockPos));
    }

    private SpikeConfiguration(boolean z, List<SpikeFeature.EndSpike> list, Optional<BlockPos> optional) {
        this.crystalInvulnerable = z;
        this.spikes = list;
        this.crystalBeamTarget = optional.orElse(null);
    }

    public boolean isCrystalInvulnerable() {
        return this.crystalInvulnerable;
    }

    public List<SpikeFeature.EndSpike> getSpikes() {
        return this.spikes;
    }

    @Nullable
    public BlockPos getCrystalBeamTarget() {
        return this.crystalBeamTarget;
    }
}
