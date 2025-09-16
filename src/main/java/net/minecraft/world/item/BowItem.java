package net.minecraft.world.item;

import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/BowItem.class */
public class BowItem extends ProjectileWeaponItem implements Vanishable {
    public BowItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player) livingEntity;
        boolean z = player.abilities.instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, itemStack) > 0;
        ItemStack projectile = player.getProjectile(itemStack);
        if (projectile.isEmpty() && !z) {
            return;
        }
        if (projectile.isEmpty()) {
            projectile = new ItemStack(Items.ARROW);
        }
        float powerForTime = getPowerForTime(getUseDuration(itemStack) - i);
        if (powerForTime < 0.1d) {
            return;
        }
        boolean z2 = z && projectile.getItem() == Items.ARROW;
        if (!level.isClientSide) {
            AbstractArrow createArrow = ((ArrowItem) (projectile.getItem() instanceof ArrowItem ? projectile.getItem() : Items.ARROW)).createArrow(level, projectile, player);
            createArrow.shootFromRotation(player, player.xRot, player.yRot, 0.0f, powerForTime * 3.0f, 1.0f);
            if (powerForTime == 1.0f) {
                createArrow.setCritArrow(true);
            }
            int itemEnchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, itemStack);
            if (itemEnchantmentLevel > 0) {
                createArrow.setBaseDamage(createArrow.getBaseDamage() + (itemEnchantmentLevel * 0.5d) + 0.5d);
            }
            int itemEnchantmentLevel2 = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, itemStack);
            if (itemEnchantmentLevel2 > 0) {
                createArrow.setKnockback(itemEnchantmentLevel2);
            }
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, itemStack) > 0) {
                createArrow.setSecondsOnFire(100);
            }
            itemStack.hurtAndBreak(1, player, player2 -> {
                player2.broadcastBreakEvent(player.getUsedItemHand());
            });
            if (z2 || (player.abilities.instabuild && (projectile.getItem() == Items.SPECTRAL_ARROW || projectile.getItem() == Items.TIPPED_ARROW))) {
                createArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(createArrow);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, (1.0f / ((random.nextFloat() * 0.4f) + 1.2f)) + (powerForTime * 0.5f));
        if (!z2 && !player.abilities.instabuild) {
            projectile.shrink(1);
            if (projectile.isEmpty()) {
                player.inventory.removeItem(projectile);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    public static float getPowerForTime(int i) {
        float f = i / 20.0f;
        float f2 = ((f * f) + (f * 2.0f)) / 3.0f;
        if (f2 > 1.0f) {
            f2 = 1.0f;
        }
        return f2;
    }

    @Override // net.minecraft.world.item.Item
    public int getUseDuration(ItemStack itemStack) {
        return 72000;
    }

    @Override // net.minecraft.world.item.Item
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.BOW;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        boolean z = !player.getProjectile(itemInHand).isEmpty();
        if (player.abilities.instabuild || z) {
            player.startUsingItem(interactionHand);
            return InteractionResultHolder.consume(itemInHand);
        }
        return InteractionResultHolder.fail(itemInHand);
    }

    @Override // net.minecraft.world.item.ProjectileWeaponItem
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override // net.minecraft.world.item.ProjectileWeaponItem
    public int getDefaultProjectileRange() {
        return 15;
    }
}
