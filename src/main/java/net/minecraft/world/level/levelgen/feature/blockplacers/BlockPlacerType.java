package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.blockplacers.BlockPlacer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/blockplacers/BlockPlacerType.class */
public class BlockPlacerType<P extends BlockPlacer> {
    public static final BlockPlacerType<SimpleBlockPlacer> SIMPLE_BLOCK_PLACER = register("simple_block_placer", SimpleBlockPlacer.CODEC);
    public static final BlockPlacerType<DoublePlantPlacer> DOUBLE_PLANT_PLACER = register("double_plant_placer", DoublePlantPlacer.CODEC);
    public static final BlockPlacerType<ColumnPlacer> COLUMN_PLACER = register("column_placer", ColumnPlacer.CODEC);
    private final Codec<P> codec;

//    private static <P extends BlockPlacer> BlockPlacerType<P> register(String str, Codec<P> codec) {
//        return (BlockPlacerType) Registry.register(Registry.BLOCK_PLACER_TYPES, str, new BlockPlacerType(codec));
//    }
private static <P extends BlockPlacer> BlockPlacerType<P> register(String str, Codec<P> codec) {
    // Создаем ResourceLocation из строки
    ResourceLocation id = new ResourceLocation(str);

    // Создаем экземпляр BlockPlacerType с указанием типа
    BlockPlacerType<P> type = new BlockPlacerType<>(codec);

    // Регистрируем и возвращаем
    return Registry.register(Registry.BLOCK_PLACER_TYPES, id, type);
}

    private BlockPlacerType(Codec<P> codec) {
        this.codec = codec;
    }

    public Codec<P> codec() {
        return this.codec;
    }
}
