package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/RandomBlockMatchTest.class */
public class RandomBlockMatchTest extends RuleTest {
    public static final Codec<RandomBlockMatchTest> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Registry.BLOCK.fieldOf("block").forGetter(randomBlockMatchTest -> {
            return randomBlockMatchTest.block;
        }), Codec.FLOAT.fieldOf("probability").forGetter(randomBlockMatchTest2 -> {
            return Float.valueOf(randomBlockMatchTest2.probability);
        })).apply(instance, (v1, v2) -> {
            return new RandomBlockMatchTest(v1, v2);
        });
    });
    private final Block block;
    private final float probability;

    public RandomBlockMatchTest(Block block, float f) {
        this.block = block;
        this.probability = f;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest
    public boolean test(BlockState blockState, Random random) {
        return blockState.is(this.block) && random.nextFloat() < this.probability;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest
    protected RuleTestType<?> getType() {
        return RuleTestType.RANDOM_BLOCK_TEST;
    }
}
