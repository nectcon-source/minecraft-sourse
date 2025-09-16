package net.minecraft.world.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/WaterLilyBlockItem.class */
public class WaterLilyBlockItem extends BlockItem {
    public WaterLilyBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override // net.minecraft.world.item.BlockItem, net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        return InteractionResult.PASS;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        BlockHitResult playerPOVHitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        return new InteractionResultHolder<>(super.useOn(new UseOnContext(player, interactionHand, playerPOVHitResult.withPosition(playerPOVHitResult.getBlockPos().above()))), player.getItemInHand(interactionHand));
    }
}
