package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ScaffoldingBlockItem.class */
public class ScaffoldingBlockItem extends BlockItem {
    public ScaffoldingBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override // net.minecraft.world.item.BlockItem
    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext blockPlaceContext) {
        Direction horizontalDirection;
        BlockPos clickedPos = blockPlaceContext.getClickedPos();
        Level level = blockPlaceContext.getLevel();
        if (level.getBlockState(clickedPos).is(getBlock())) {
            if (blockPlaceContext.isSecondaryUseActive()) {
                horizontalDirection = blockPlaceContext.isInside() ? blockPlaceContext.getClickedFace().getOpposite() : blockPlaceContext.getClickedFace();
            } else {
                horizontalDirection = blockPlaceContext.getClickedFace() == Direction.UP ? blockPlaceContext.getHorizontalDirection() : Direction.UP;
            }
            int i = 0;
            BlockPos.MutableBlockPos move = clickedPos.mutable().move(horizontalDirection);
            while (i < 7) {
                if (!level.isClientSide && !Level.isInWorldBounds(move)) {
                    Player player = blockPlaceContext.getPlayer();
                    int maxBuildHeight = level.getMaxBuildHeight();
                    if ((player instanceof ServerPlayer) && move.getY() >= maxBuildHeight) {
                        ((ServerPlayer) player).connection.send(new ClientboundChatPacket(new TranslatableComponent("build.tooHigh", Integer.valueOf(maxBuildHeight)).withStyle(ChatFormatting.RED), ChatType.GAME_INFO, Util.NIL_UUID));
                        return null;
                    }
                    return null;
                }
                BlockState blockState = level.getBlockState(move);
                if (!blockState.is(getBlock())) {
                    if (blockState.canBeReplaced(blockPlaceContext)) {
                        return BlockPlaceContext.at(blockPlaceContext, move, horizontalDirection);
                    }
                    return null;
                }
                move.move(horizontalDirection);
                if (horizontalDirection.getAxis().isHorizontal()) {
                    i++;
                }
            }
            return null;
        }
        if (ScaffoldingBlock.getDistance(level, clickedPos) == 7) {
            return null;
        }
        return blockPlaceContext;
    }

    @Override // net.minecraft.world.item.BlockItem
    protected boolean mustSurvive() {
        return false;
    }
}
