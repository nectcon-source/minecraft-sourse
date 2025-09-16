package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/PosAlwaysTrueTest.class */
public class PosAlwaysTrueTest extends PosRuleTest {

    public static final Codec<PosAlwaysTrueTest> CODEC = Codec.unit(() -> PosAlwaysTrueTest.INSTANCE);
    public static final PosAlwaysTrueTest INSTANCE = new PosAlwaysTrueTest();

    private PosAlwaysTrueTest() {
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest
    public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random) {
        return true;
    }

    @Override // net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest
    protected PosRuleTestType<?> getType() {
        return PosRuleTestType.ALWAYS_TRUE_TEST;
    }
}
