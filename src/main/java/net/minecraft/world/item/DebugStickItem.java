package net.minecraft.world.item;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/DebugStickItem.class */
public class DebugStickItem extends Item {
    public DebugStickItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }

    @Override // net.minecraft.world.item.Item
    public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        if (!level.isClientSide) {
            handleInteraction(player, blockState, level, blockPos, false, player.getItemInHand(InteractionHand.MAIN_HAND));
            return false;
        }
        return false;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        Player player = useOnContext.getPlayer();
        Level level = useOnContext.getLevel();
        if (!level.isClientSide && player != null) {
            BlockPos clickedPos = useOnContext.getClickedPos();
            handleInteraction(player, level.getBlockState(clickedPos), level, clickedPos, true, useOnContext.getItemInHand());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void handleInteraction(Player player, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, boolean z, ItemStack itemStack) {
        if (!player.canUseGameMasterBlocks()) {
            return;
        }
        Block block = blockState.getBlock();
        StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
        Collection<Property<?>> properties = stateDefinition.getProperties();
        String resourceLocation = Registry.BLOCK.getKey(block).toString();
        if (properties.isEmpty()) {
            message(player, new TranslatableComponent(getDescriptionId() + ".empty", resourceLocation));
            return;
        }
        CompoundTag orCreateTagElement = itemStack.getOrCreateTagElement("DebugProperty");
        Property<?> property = stateDefinition.getProperty(orCreateTagElement.getString(resourceLocation));
        if (z) {
            if (property == null) {
                property = properties.iterator().next();
            }
            BlockState cycleState = cycleState(blockState, property, player.isSecondaryUseActive());
            levelAccessor.setBlock(blockPos, cycleState, 18);
            message(player, new TranslatableComponent(getDescriptionId() + ".update", property.getName(), getNameHelper(cycleState, property)));
            return;
        }
        Property<?> property2 = (Property) getRelative(properties, property, player.isSecondaryUseActive());
        String name = property2.getName();
        orCreateTagElement.putString(resourceLocation, name);
        message(player, new TranslatableComponent(getDescriptionId() + ".select", name, getNameHelper(blockState, property2)));
    }

    private static <T extends Comparable<T>> BlockState cycleState(BlockState blockState, Property<T> property, boolean z) {
        return (BlockState) blockState.setValue(property,  getRelative(property.getPossibleValues(), blockState.getValue(property), z));
    }

    private static <T> T getRelative(Iterable<T> iterable, @Nullable T t, boolean z) {
        return z ? (T) Util.findPreviousInIterable(iterable, t) : (T) Util.findNextInIterable(iterable, t);
    }

    private static void message(Player player, Component component) {
        ((ServerPlayer) player).sendMessage(component, ChatType.GAME_INFO, Util.NIL_UUID);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static <T extends Comparable<T>> String getNameHelper(BlockState blockState, Property<T> property) {
        return property.getName(blockState.getValue(property));
    }
}
