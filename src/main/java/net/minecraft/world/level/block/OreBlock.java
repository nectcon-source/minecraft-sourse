package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/OreBlock.class */
public class OreBlock extends Block {
    public OreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected int xpOnDrop(Random random) {
        if (this == Blocks.COAL_ORE) {
            return Mth.nextInt(random, 0, 2);
        }
        if (this == Blocks.DIAMOND_ORE) {
            return Mth.nextInt(random, 3, 7);
        }
        if (this == Blocks.EMERALD_ORE) {
            return Mth.nextInt(random, 3, 7);
        }
        if (this == Blocks.LAPIS_ORE) {
            return Mth.nextInt(random, 2, 5);
        }
        if (this == Blocks.NETHER_QUARTZ_ORE) {
            return Mth.nextInt(random, 2, 5);
        }
        if (this == Blocks.NETHER_GOLD_ORE) {
            return Mth.nextInt(random, 0, 1);
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
        int xpOnDrop;
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0 && (xpOnDrop = xpOnDrop(serverLevel.random)) > 0) {
            popExperience(serverLevel, blockPos, xpOnDrop);
        }
    }
}
