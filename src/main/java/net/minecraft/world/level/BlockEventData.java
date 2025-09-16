package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/BlockEventData.class */
public class BlockEventData {
    private final BlockPos pos;
    private final Block block;
    private final int paramA;
    private final int paramB;

    public BlockEventData(BlockPos blockPos, Block block, int i, int i2) {
        this.pos = blockPos;
        this.block = block;
        this.paramA = i;
        this.paramB = i2;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Block getBlock() {
        return this.block;
    }

    public int getParamA() {
        return this.paramA;
    }

    public int getParamB() {
        return this.paramB;
    }

    public boolean equals(Object obj) {
        if (obj instanceof BlockEventData) {
            BlockEventData blockEventData = (BlockEventData) obj;
            return this.pos.equals(blockEventData.pos) && this.paramA == blockEventData.paramA && this.paramB == blockEventData.paramB && this.block == blockEventData.block;
        }
        return false;
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * this.pos.hashCode()) + this.block.hashCode())) + this.paramA)) + this.paramB;
    }

    public String toString() {
        return "TE(" + this.pos + ")," + this.paramA + "," + this.paramB + "," + this.block;
    }
}
