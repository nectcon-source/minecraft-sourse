package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/RuleTest.class */
public abstract class RuleTest {
    public static final Codec<RuleTest> CODEC = Registry.RULE_TEST.dispatch("predicate_type", (v0) -> {
        return v0.getType();
    }, (v0) -> {
        return v0.codec();
    });

    public abstract boolean test(BlockState blockState, Random random);

    protected abstract RuleTestType<?> getType();
}
