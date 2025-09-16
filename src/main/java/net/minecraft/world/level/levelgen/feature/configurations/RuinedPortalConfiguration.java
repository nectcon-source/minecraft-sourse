package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/RuinedPortalConfiguration.class */
public class RuinedPortalConfiguration implements FeatureConfiguration {
    public static final Codec<RuinedPortalConfiguration> CODEC = RuinedPortalFeature.Type.CODEC.fieldOf("portal_type").xmap(RuinedPortalConfiguration::new, ruinedPortalConfiguration -> {
        return ruinedPortalConfiguration.portalType;
    }).codec();
    public final RuinedPortalFeature.Type portalType;

    public RuinedPortalConfiguration(RuinedPortalFeature.Type type) {
        this.portalType = type;
    }
}
