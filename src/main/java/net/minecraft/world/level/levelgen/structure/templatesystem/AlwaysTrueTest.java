package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/AlwaysTrueTest.class */
public class AlwaysTrueTest extends RuleTest {

    public static final Codec<AlwaysTrueTest> CODEC = Codec.unit(() -> AlwaysTrueTest.INSTANCE);
    public static final AlwaysTrueTest INSTANCE = new AlwaysTrueTest();

    private AlwaysTrueTest() {
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest
    public boolean test(BlockState blockState, Random random) {
        return true;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest
    protected RuleTestType<?> getType() {
        return RuleTestType.ALWAYS_TRUE_TEST;
    }
}
