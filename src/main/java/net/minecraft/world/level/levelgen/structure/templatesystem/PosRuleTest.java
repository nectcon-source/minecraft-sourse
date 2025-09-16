package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/PosRuleTest.class */
public abstract class PosRuleTest {
    public static final Codec<PosRuleTest> CODEC = Registry.POS_RULE_TEST.dispatch("predicate_type", (v0) -> {
        return v0.getType();
    }, (v0) -> {
        return v0.codec();
    });

    public abstract boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random);

    protected abstract PosRuleTestType<?> getType();
}
