package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ChorusFruitItem.class */
public class ChorusFruitItem extends Item {
    public ChorusFruitItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        ItemStack finishUsingItem = super.finishUsingItem(itemStack, level, livingEntity);
        if (!level.isClientSide) {
            double x = livingEntity.getX();
            double y = livingEntity.getY();
            double z = livingEntity.getZ();
            int i = 0;
            while (true) {
                if (i >= 16) {
                    break;
                }
                double x2 = livingEntity.getX() + ((livingEntity.getRandom().nextDouble() - 0.5d) * 16.0d);
                double clamp = Mth.clamp(livingEntity.getY() + (livingEntity.getRandom().nextInt(16) - 8), 0.0d, level.getHeight() - 1);
                double z2 = livingEntity.getZ() + ((livingEntity.getRandom().nextDouble() - 0.5d) * 16.0d);
                if (livingEntity.isPassenger()) {
                    livingEntity.stopRiding();
                }
                if (!livingEntity.randomTeleport(x2, clamp, z2, true)) {
                    i++;
                } else {
                    SoundEvent soundEvent = livingEntity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                    level.playSound(null, x, y, z, soundEvent, SoundSource.PLAYERS, 1.0f, 1.0f);
                    livingEntity.playSound(soundEvent, 1.0f, 1.0f);
                }
            }
            if (livingEntity instanceof Player) {
                ((Player) livingEntity).getCooldowns().addCooldown(this, 20);
            }
        }
        return finishUsingItem;
    }
}
