package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/NetherVines.class */
public class NetherVines {
    public static boolean isValidGrowthState(BlockState blockState) {
        return blockState.isAir();
    }

    public static int getBlocksToGrowWhenBonemealed(Random random) {
        double d = 1.0d;
        int i = 0;
        while (random.nextDouble() < d) {
            d *= 0.826d;
            i++;
        }
        return i;
    }
}
