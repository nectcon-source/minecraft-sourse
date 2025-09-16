package net.minecraft.world.level.levelgen.flat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/levelgen/flat/FlatLayerInfo.class */
public class FlatLayerInfo {
    public static final Codec<FlatLayerInfo> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(Codec.intRange(0, 256).fieldOf("height").forGetter((v0) -> {
            return v0.getHeight();
        }), Registry.BLOCK.fieldOf("block").orElse(Blocks.AIR).forGetter(flatLayerInfo -> {
            return flatLayerInfo.getBlockState().getBlock();
        })).apply(instance, (v1, v2) -> {
            return new FlatLayerInfo(v1, v2);
        });
    });
    private final BlockState blockState;
    private final int height;
    private int start;

    public FlatLayerInfo(int i, Block block) {
        this.height = i;
        this.blockState = block.defaultBlockState();
    }

    public int getHeight() {
        return this.height;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public int getStart() {
        return this.start;
    }

    public void setStart(int i) {
        this.start = i;
    }

    public String toString() {
        return (this.height != 1 ? this.height + "*" : "") + Registry.BLOCK.getKey(this.blockState.getBlock());
    }
}
