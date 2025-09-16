package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/TridentItem.class */
public class TridentItem extends Item implements Vanishable {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public TridentItem(Item.Properties properties) {
        super(properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0d, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.9000000953674316d, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override // net.minecraft.world.item.Item
    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        return !player.isCreative();
    }

    @Override // net.minecraft.world.item.Item
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.SPEAR;
    }

    @Override // net.minecraft.world.item.Item
    public int getUseDuration(ItemStack itemStack) {
        return 72000;
    }

    @Override // net.minecraft.world.item.Item
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        SoundEvent soundEvent;
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player) livingEntity;
        if (getUseDuration(itemStack) - i < 10) {
            return;
        }
        int riptide = EnchantmentHelper.getRiptide(itemStack);
        if (riptide > 0 && !player.isInWaterOrRain()) {
            return;
        }
        if (!level.isClientSide) {
            itemStack.hurtAndBreak(1, player, player2 -> {
                player2.broadcastBreakEvent(livingEntity.getUsedItemHand());
            });
            if (riptide == 0) {
                ThrownTrident thrownTrident = new ThrownTrident(level, player, itemStack);
                thrownTrident.shootFromRotation(player, player.xRot, player.yRot, 0.0f, 2.5f + (riptide * 0.5f), 1.0f);
                if (player.abilities.instabuild) {
                    thrownTrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                level.addFreshEntity(thrownTrident);
                level.playSound((Player) null, thrownTrident, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
                if (!player.abilities.instabuild) {
                    player.inventory.removeItem(itemStack);
                }
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (riptide > 0) {
            float f = player.yRot;
            float f2 = player.xRot;
            float cos = (-Mth.sin(f * 0.017453292f)) * Mth.cos(f2 * 0.017453292f);
            float f3 = -Mth.sin(f2 * 0.017453292f);
            float cos2 = Mth.cos(f * 0.017453292f) * Mth.cos(f2 * 0.017453292f);
            float sqrt = Mth.sqrt((cos * cos) + (f3 * f3) + (cos2 * cos2));
            float f4 = 3.0f * ((1.0f + riptide) / 4.0f);
            player.push(cos * (f4 / sqrt), f3 * (f4 / sqrt), cos2 * (f4 / sqrt));
            player.startAutoSpinAttack(20);
            if (player.isOnGround()) {
                player.move(MoverType.SELF, new Vec3(0.0d, 1.1999999284744263d, 0.0d));
            }
            if (riptide >= 3) {
                soundEvent = SoundEvents.TRIDENT_RIPTIDE_3;
            } else if (riptide == 2) {
                soundEvent = SoundEvents.TRIDENT_RIPTIDE_2;
            } else {
                soundEvent = SoundEvents.TRIDENT_RIPTIDE_1;
            }
            level.playSound((Player) null, player, soundEvent, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (itemInHand.getDamageValue() >= itemInHand.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(itemInHand);
        }
        if (EnchantmentHelper.getRiptide(itemInHand) > 0 && !player.isInWaterOrRain()) {
            return InteractionResultHolder.fail(itemInHand);
        }
        player.startUsingItem(interactionHand);
        return InteractionResultHolder.consume(itemInHand);
    }

    @Override // net.minecraft.world.item.Item
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
        itemStack.hurtAndBreak(1, livingEntity2, livingEntity3 -> {
            livingEntity3.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });
        return true;
    }

    @Override // net.minecraft.world.item.Item
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
        if (blockState.getDestroySpeed(level, blockPos) != 0.0d) {
            itemStack.hurtAndBreak(2, livingEntity, livingEntity2 -> {
                livingEntity2.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
            return true;
        }
        return true;
    }

    @Override // net.minecraft.world.item.Item
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            return this.defaultModifiers;
        }
        return super.getDefaultAttributeModifiers(equipmentSlot);
    }

    @Override // net.minecraft.world.item.Item
    public int getEnchantmentValue() {
        return 1;
    }
}
