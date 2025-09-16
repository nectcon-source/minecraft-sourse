package net.minecraft.world.item;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/BlockItem.class */
public class BlockItem extends Item {

    @Deprecated
    private final Block block;

    public BlockItem(Block block, Item.Properties properties) {
        super(properties);
        this.block = block;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        InteractionResult place = place(new BlockPlaceContext(useOnContext));
        if (!place.consumesAction() && isEdible()) {
            return use(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand()).getResult();
        }
        return place;
    }

    public InteractionResult place(BlockPlaceContext blockPlaceContext) {
        if (!blockPlaceContext.canPlace()) {
            return InteractionResult.FAIL;
        }
        BlockPlaceContext updatePlacementContext = updatePlacementContext(blockPlaceContext);
        if (updatePlacementContext == null) {
            return InteractionResult.FAIL;
        }
        BlockState placementState = getPlacementState(updatePlacementContext);
        if (placementState == null) {
            return InteractionResult.FAIL;
        }
        if (!placeBlock(updatePlacementContext, placementState)) {
            return InteractionResult.FAIL;
        }
        BlockPos clickedPos = updatePlacementContext.getClickedPos();
        Level level = updatePlacementContext.getLevel();
        Player player = updatePlacementContext.getPlayer();
        ItemStack itemInHand = updatePlacementContext.getItemInHand();
        BlockState blockState = level.getBlockState(clickedPos);
        Block block = blockState.getBlock();
        if (block == placementState.getBlock()) {
            blockState = updateBlockStateFromTag(clickedPos, level, itemInHand, blockState);
            updateCustomBlockEntityTag(clickedPos, level, player, itemInHand, blockState);
            block.setPlacedBy(level, clickedPos, blockState, player, itemInHand);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, clickedPos, itemInHand);
            }
        }
        SoundType soundType = blockState.getSoundType();
        level.playSound(player, clickedPos, getPlaceSound(blockState), SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);
        if (player == null || !player.abilities.instabuild) {
            itemInHand.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    protected SoundEvent getPlaceSound(BlockState blockState) {
        return blockState.getSoundType().getPlaceSound();
    }

    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext blockPlaceContext) {
        return blockPlaceContext;
    }

    protected boolean updateCustomBlockEntityTag(BlockPos blockPos, Level level, @Nullable Player player, ItemStack itemStack, BlockState blockState) {
        return updateCustomBlockEntityTag(level, player, blockPos, itemStack);
    }

    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
        BlockState stateForPlacement = getBlock().getStateForPlacement(blockPlaceContext);
        if (stateForPlacement == null || !canPlace(blockPlaceContext, stateForPlacement)) {
            return null;
        }
        return stateForPlacement;
    }

    private BlockState updateBlockStateFromTag(BlockPos blockPos, Level level, ItemStack itemStack, BlockState blockState) {
        BlockState blockState2 = blockState;
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            CompoundTag compound = tag.getCompound("BlockStateTag");
            StateDefinition<Block, BlockState> stateDefinition = blockState2.getBlock().getStateDefinition();
            for (String str : compound.getAllKeys()) {
                Property<?> property = stateDefinition.getProperty(str);
                if (property != null) {
                    blockState2 = updateState(blockState2, property, compound.get(str).getAsString());
                }
            }
        }
        if (blockState2 != blockState) {
            level.setBlock(blockPos, blockState2, 2);
        }
        return blockState2;
    }

    private static <T extends Comparable<T>> BlockState updateState(BlockState blockState, Property<T> property, String str) {
        return (BlockState) property.getValue(str).map(comparable -> {
            return (BlockState) blockState.setValue(property, comparable);
        }).orElse(blockState);
    }

    protected boolean canPlace(BlockPlaceContext blockPlaceContext, BlockState blockState) {
        Player player = blockPlaceContext.getPlayer();
        return (!mustSurvive() || blockState.canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) && blockPlaceContext.getLevel().isUnobstructed(blockState, blockPlaceContext.getClickedPos(), player == null ? CollisionContext.empty() : CollisionContext.of(player));
    }

    protected boolean mustSurvive() {
        return true;
    }

    protected boolean placeBlock(BlockPlaceContext blockPlaceContext, BlockState blockState) {
        return blockPlaceContext.getLevel().setBlock(blockPlaceContext.getClickedPos(), blockState, 11);
    }

    public static boolean updateCustomBlockEntityTag(Level level, @Nullable Player player, BlockPos blockPos, ItemStack itemStack) {
        CompoundTag tagElement;
        BlockEntity blockEntity;
        if (level.getServer() != null && (tagElement = itemStack.getTagElement("BlockEntityTag")) != null && (blockEntity = level.getBlockEntity(blockPos)) != null) {
            if (!level.isClientSide && blockEntity.onlyOpCanSetNbt() && (player == null || !player.canUseGameMasterBlocks())) {
                return false;
            }
            CompoundTag save = blockEntity.save(new CompoundTag());
            CompoundTag copy = save.copy();
            save.merge(tagElement);
            save.putInt("x", blockPos.getX());
            save.putInt("y", blockPos.getY());
            save.putInt("z", blockPos.getZ());
            if (!save.equals(copy)) {
                blockEntity.load(level.getBlockState(blockPos), save);
                blockEntity.setChanged();
                return true;
            }
            return false;
        }
        return false;
    }

    @Override // net.minecraft.world.item.Item
    public String getDescriptionId() {
        return getBlock().getDescriptionId();
    }

    @Override // net.minecraft.world.item.Item
    public void fillItemCategory(CreativeModeTab creativeModeTab, NonNullList<ItemStack> nonNullList) {
        if (allowdedIn(creativeModeTab)) {
            getBlock().fillItemCategory(creativeModeTab, nonNullList);
        }
    }

    @Override // net.minecraft.world.item.Item
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        getBlock().appendHoverText(itemStack, level, list, tooltipFlag);
    }

    public Block getBlock() {
        return this.block;
    }

    public void registerBlocks(Map<Block, Item> map, Item item) {
        map.put(getBlock(), item);
    }
}
