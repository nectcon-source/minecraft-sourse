package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/EndGatewayConfiguration.class */
public class EndGatewayConfiguration implements FeatureConfiguration {
    public static final Codec<EndGatewayConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(BlockPos.CODEC.optionalFieldOf("exit").forGetter(endGatewayConfiguration -> {
            return endGatewayConfiguration.exit;
        }), Codec.BOOL.fieldOf("exact").forGetter(endGatewayConfiguration2 -> {
            return Boolean.valueOf(endGatewayConfiguration2.exact);
        })).apply(instance, (v1, v2) -> {
            return new EndGatewayConfiguration(v1, v2);
        });
    });
    private final Optional<BlockPos> exit;
    private final boolean exact;

    private EndGatewayConfiguration(Optional<BlockPos> optional, boolean z) {
        this.exit = optional;
        this.exact = z;
    }

    public static EndGatewayConfiguration knownExit(BlockPos blockPos, boolean z) {
        return new EndGatewayConfiguration(Optional.of(blockPos), z);
    }

    public static EndGatewayConfiguration delayedExitSearch() {
        return new EndGatewayConfiguration(Optional.empty(), false);
    }

    public Optional<BlockPos> getExit() {
        return this.exit;
    }

    public boolean isExitExact() {
        return this.exact;
    }
}
