package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SmithingTableBlock.class */
public class SmithingTableBlock extends CraftingTableBlock {
    private static final Component CONTAINER_TITLE = new TranslatableComponent("container.upgrade");

    protected SmithingTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.CraftingTableBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return new SimpleMenuProvider((i, inventory, player) -> {
            return new SmithingMenu(i, inventory, ContainerLevelAccess.create(level, blockPos));
        }, CONTAINER_TITLE);
    }

    @Override // net.minecraft.world.level.block.CraftingTableBlock, net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        player.openMenu(blockState.getMenuProvider(level, blockPos));
        player.awardStat(Stats.INTERACT_WITH_SMITHING_TABLE);
        return InteractionResult.CONSUME;
    }
}
