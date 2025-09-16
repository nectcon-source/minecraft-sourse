package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ShearsItem.class */
public class ShearsItem extends Item {
    public ShearsItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
        if (!level.isClientSide && !blockState.getBlock().is(BlockTags.FIRE)) {
            itemStack.hurtAndBreak(1, livingEntity, livingEntity2 -> {
                livingEntity2.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }
        if (blockState.is(BlockTags.LEAVES) || blockState.is(Blocks.COBWEB) || blockState.is(Blocks.GRASS) || blockState.is(Blocks.FERN) || blockState.is(Blocks.DEAD_BUSH) || blockState.is(Blocks.VINE) || blockState.is(Blocks.TRIPWIRE) || blockState.is(BlockTags.WOOL)) {
            return true;
        }
        return super.mineBlock(itemStack, level, blockState, blockPos, livingEntity);
    }

    @Override // net.minecraft.world.item.Item
    public boolean isCorrectToolForDrops(BlockState blockState) {
        return blockState.is(Blocks.COBWEB) || blockState.is(Blocks.REDSTONE_WIRE) || blockState.is(Blocks.TRIPWIRE);
    }

    @Override // net.minecraft.world.item.Item
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        if (blockState.is(Blocks.COBWEB) || blockState.is(BlockTags.LEAVES)) {
            return 15.0f;
        }
        if (blockState.is(BlockTags.WOOL)) {
            return 5.0f;
        }
        return super.getDestroySpeed(itemStack, blockState);
    }
}
