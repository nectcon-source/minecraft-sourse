package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/feature/structures/FeaturePoolElement.class */
public class FeaturePoolElement extends StructurePoolElement {
    public static final Codec<FeaturePoolElement> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(ConfiguredFeature.CODEC.fieldOf("feature").forGetter(featurePoolElement -> {
            return featurePoolElement.feature;
        }), projectionCodec()).apply(instance, FeaturePoolElement::new);
    });
    private final Supplier<ConfiguredFeature<?, ?>> feature;
    private final CompoundTag defaultJigsawNBT;

    protected FeaturePoolElement(Supplier<ConfiguredFeature<?, ?>> supplier, StructureTemplatePool.Projection projection) {
        super(projection);
        this.feature = supplier;
        this.defaultJigsawNBT = fillDefaultJigsawNBT();
    }

    private CompoundTag fillDefaultJigsawNBT() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("name", "minecraft:bottom");
        compoundTag.putString("final_state", "minecraft:air");
        compoundTag.putString("pool", "minecraft:empty");
        compoundTag.putString("target", "minecraft:empty");
        compoundTag.putString("joint", JigsawBlockEntity.JointType.ROLLABLE.getSerializedName());
        return compoundTag;
    }

    public BlockPos getSize(StructureManager structureManager, Rotation rotation) {
        return BlockPos.ZERO;
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager structureManager, BlockPos blockPos, Rotation rotation, Random random) {
        List<StructureTemplate.StructureBlockInfo> newArrayList = Lists.newArrayList();
        newArrayList.add(new StructureTemplate.StructureBlockInfo(blockPos, (BlockState) Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, FrontAndTop.fromFrontAndTop(Direction.DOWN, Direction.SOUTH)), this.defaultJigsawNBT));
        return newArrayList;
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public BoundingBox getBoundingBox(StructureManager structureManager, BlockPos blockPos, Rotation rotation) {
        BlockPos size = getSize(structureManager, rotation);
        return new BoundingBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + size.getX(), blockPos.getY() + size.getY(), blockPos.getZ() + size.getZ());
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public boolean place(StructureManager structureManager, WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockPos blockPos2, Rotation rotation, BoundingBox boundingBox, Random random, boolean z) {
        return this.feature.get().place(worldGenLevel, chunkGenerator, random, blockPos);
    }

    @Override // net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.FEATURE;
    }

    public String toString() {
        return "Feature[" + Registry.FEATURE.getKey(this.feature.get().feature()) + "]";
    }
}
