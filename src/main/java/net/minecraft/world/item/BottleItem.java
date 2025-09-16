package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/BottleItem.class */
public class BottleItem extends Item {
    public BottleItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        List<AreaEffectCloud> entitiesOfClass = level.getEntitiesOfClass(AreaEffectCloud.class, player.getBoundingBox().inflate(2.0d), areaEffectCloud -> {
            return areaEffectCloud != null && areaEffectCloud.isAlive() && (areaEffectCloud.getOwner() instanceof EnderDragon);
        });
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (!entitiesOfClass.isEmpty()) {
            AreaEffectCloud areaEffectCloud2 = entitiesOfClass.get(0);
            areaEffectCloud2.setRadius(areaEffectCloud2.getRadius() - 0.5f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0f, 1.0f);
            return InteractionResultHolder.sidedSuccess(turnBottleIntoItem(itemInHand, player, new ItemStack(Items.DRAGON_BREATH)), level.isClientSide());
        }
        HitResult playerPOVHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (playerPOVHitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemInHand);
        }
        if (playerPOVHitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) playerPOVHitResult).getBlockPos();
            if (!level.mayInteract(player, blockPos)) {
                return InteractionResultHolder.pass(itemInHand);
            }
            if (level.getFluidState(blockPos).is(FluidTags.WATER)) {
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0f, 1.0f);
                return InteractionResultHolder.sidedSuccess(turnBottleIntoItem(itemInHand, player, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)), level.isClientSide());
            }
        }
        return InteractionResultHolder.pass(itemInHand);
    }

    protected ItemStack turnBottleIntoItem(ItemStack itemStack, Player player, ItemStack itemStack2) {
        player.awardStat(Stats.ITEM_USED.get(this));
        return ItemUtils.createFilledResult(itemStack, player, itemStack2);
    }
}
