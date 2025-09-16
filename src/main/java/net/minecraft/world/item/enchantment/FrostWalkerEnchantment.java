package net.minecraft.world.item.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/enchantment/FrostWalkerEnchantment.class */
public class FrostWalkerEnchantment extends Enchantment {
    public FrostWalkerEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlotArr) {
        super(rarity, EnchantmentCategory.ARMOR_FEET, equipmentSlotArr);
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMinCost(int i) {
        return i * 10;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxCost(int i) {
        return getMinCost(i) + 15;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean isTreasureOnly() {
        return true;
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public int getMaxLevel() {
        return 2;
    }

    public static void onEntityMoved(LivingEntity livingEntity, Level level, BlockPos blockPos, int i) {
        if (!livingEntity.isOnGround()) {
            return;
        }
        BlockState defaultBlockState = Blocks.FROSTED_ICE.defaultBlockState();
        float min = Math.min(16, 2 + i);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-min, -1.0d, -min), blockPos.offset(min, -1.0d, min))) {
            if (blockPos2.closerThan(livingEntity.position(), min)) {
                mutableBlockPos.set(blockPos2.getX(), blockPos2.getY() + 1, blockPos2.getZ());
                if (level.getBlockState(mutableBlockPos).isAir()) {
                    BlockState blockState = level.getBlockState(blockPos2);
                    if (blockState.getMaterial() == Material.WATER && ((Integer) blockState.getValue(LiquidBlock.LEVEL)).intValue() == 0 && defaultBlockState.canSurvive(level, blockPos2) && level.isUnobstructed(defaultBlockState, blockPos2, CollisionContext.empty())) {
                        level.setBlockAndUpdate(blockPos2, defaultBlockState);
                        level.getBlockTicks().scheduleTick(blockPos2, Blocks.FROSTED_ICE, Mth.nextInt(livingEntity.getRandom(), 60, 120));
                    }
                }
            }
        }
    }

    @Override // net.minecraft.world.item.enchantment.Enchantment
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.DEPTH_STRIDER;
    }
}
