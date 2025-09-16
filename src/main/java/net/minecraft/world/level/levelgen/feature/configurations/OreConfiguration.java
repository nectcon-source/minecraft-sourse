package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/OreConfiguration.class */
public class OreConfiguration implements FeatureConfiguration {
    public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(RuleTest.CODEC.fieldOf("target").forGetter(oreConfiguration -> {
            return oreConfiguration.target;
        }), BlockState.CODEC.fieldOf("state").forGetter(oreConfiguration2 -> {
            return oreConfiguration2.state;
        }), Codec.intRange(0, 64).fieldOf("size").forGetter(oreConfiguration3 -> {
            return Integer.valueOf(oreConfiguration3.size);
        })).apply(instance, (v1, v2, v3) -> {
            return new OreConfiguration(v1, v2, v3);
        });
    });
    public final RuleTest target;
    public final int size;
    public final BlockState state;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/configurations/OreConfiguration$Predicates.class */
    public static final class Predicates {
        public static final RuleTest NATURAL_STONE = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);
        public static final RuleTest NETHERRACK = new BlockMatchTest(Blocks.NETHERRACK);
        public static final RuleTest NETHER_ORE_REPLACEABLES = new TagMatchTest(BlockTags.BASE_STONE_NETHER);
    }

    public OreConfiguration(RuleTest ruleTest, BlockState blockState, int i) {
        this.size = i;
        this.state = blockState;
        this.target = ruleTest;
    }
}
