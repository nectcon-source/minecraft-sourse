package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/structure/templatesystem/StructureProcessorType.class */
public interface StructureProcessorType<P extends StructureProcessor> {
    public static final StructureProcessorType<BlockIgnoreProcessor> BLOCK_IGNORE = register("block_ignore", BlockIgnoreProcessor.CODEC);
    public static final StructureProcessorType<BlockRotProcessor> BLOCK_ROT = register("block_rot", BlockRotProcessor.CODEC);
    public static final StructureProcessorType<GravityProcessor> GRAVITY = register("gravity", GravityProcessor.CODEC);
    public static final StructureProcessorType<JigsawReplacementProcessor> JIGSAW_REPLACEMENT = register("jigsaw_replacement", JigsawReplacementProcessor.CODEC);
    public static final StructureProcessorType<RuleProcessor> RULE = register("rule", RuleProcessor.CODEC);
    public static final StructureProcessorType<NopProcessor> NOP = register("nop", NopProcessor.CODEC);
    public static final StructureProcessorType<BlockAgeProcessor> BLOCK_AGE = register("block_age", BlockAgeProcessor.CODEC);
    public static final StructureProcessorType<BlackstoneReplaceProcessor> BLACKSTONE_REPLACE = register("blackstone_replace", BlackstoneReplaceProcessor.CODEC);
    public static final StructureProcessorType<LavaSubmergedBlockProcessor> LAVA_SUBMERGED_BLOCK = register("lava_submerged_block", LavaSubmergedBlockProcessor.CODEC);
    public static final Codec<StructureProcessor> SINGLE_CODEC = Registry.STRUCTURE_PROCESSOR.dispatch("processor_type", (v0) -> {
        return v0.getType();
    }, (v0) -> {
        return v0.codec();
    });
    public static final Codec<StructureProcessorList> LIST_OBJECT_CODEC = SINGLE_CODEC.listOf().xmap(StructureProcessorList::new, (v0) -> {
        return v0.list();
    });
    public static final Codec<StructureProcessorList> DIRECT_CODEC = Codec.either(LIST_OBJECT_CODEC.fieldOf("processors").codec(), LIST_OBJECT_CODEC).xmap(either -> {
        return (StructureProcessorList) either.map(structureProcessorList -> {
            return structureProcessorList;
        }, structureProcessorList2 -> {
            return structureProcessorList2;
        });
    }, (v0) -> {
        return Either.left(v0);
    });
    public static final Codec<Supplier<StructureProcessorList>> LIST_CODEC = RegistryFileCodec.create(Registry.PROCESSOR_LIST_REGISTRY, DIRECT_CODEC);

    Codec<P> codec();

//    static <P extends StructureProcessor> StructureProcessorType<P> register(String str, Codec<P> codec) {
//        return (StructureProcessorType) Registry.register(Registry.STRUCTURE_PROCESSOR, str, () -> {
//            return codec;
//        });
//    }
static <P extends StructureProcessor> StructureProcessorType<P> register(String str, Codec<P> codec) {
    return Registry.register(
            Registry.STRUCTURE_PROCESSOR,
            str,
            () -> (Codec<P>)codec
    );
}
}
