package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/SnowballItem.class */
public class SnowballItem extends Item {
    public SnowballItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / ((random.nextFloat() * 0.4f) + 0.8f));
        if (!level.isClientSide) {
            Snowball snowball = new Snowball(level, player);
            snowball.setItem(itemInHand);
            snowball.shootFromRotation(player, player.xRot, player.yRot, 0.0f, 1.5f, 1.0f);
            level.addFreshEntity(snowball);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.abilities.instabuild) {
            itemInHand.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(itemInHand, level.isClientSide());
    }
}
