package net.minecraft.world.item;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/SaddleItem.class */
public class SaddleItem extends Item {
    public SaddleItem(Item.Properties properties) {
        super(properties);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // net.minecraft.world.item.Item
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        if ((livingEntity instanceof Saddleable) && livingEntity.isAlive()) {
            Saddleable saddleable = (Saddleable) livingEntity;
            if (!saddleable.isSaddled() && saddleable.isSaddleable()) {
                if (!player.level.isClientSide) {
                    saddleable.equipSaddle(SoundSource.NEUTRAL);
                    itemStack.shrink(1);
                }
                return InteractionResult.sidedSuccess(player.level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }
}
