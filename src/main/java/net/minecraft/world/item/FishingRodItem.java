package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/FishingRodItem.class */
public class FishingRodItem extends Item implements Vanishable {
    public FishingRodItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (player.fishing != null) {
            if (!level.isClientSide) {
                itemInHand.hurtAndBreak(player.fishing.retrieve(itemInHand), player, player2 -> {
                    player2.broadcastBreakEvent(interactionHand);
                });
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0f, 0.4f / ((random.nextFloat() * 0.4f) + 0.8f));
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / ((random.nextFloat() * 0.4f) + 0.8f));
            if (!level.isClientSide) {
                level.addFreshEntity(new FishingHook(player, level, EnchantmentHelper.getFishingLuckBonus(itemInHand), EnchantmentHelper.getFishingSpeedBonus(itemInHand)));
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResultHolder.sidedSuccess(itemInHand, level.isClientSide());
    }

    @Override // net.minecraft.world.item.Item
    public int getEnchantmentValue() {
        return 1;
    }
}
