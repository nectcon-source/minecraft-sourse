package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/WrittenBookItem.class */
public class WrittenBookItem extends Item {
    public WrittenBookItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean makeSureTagIsValid(@Nullable CompoundTag compoundTag) {
        if (!WritableBookItem.makeSureTagIsValid(compoundTag) || !compoundTag.contains("title", 8) || compoundTag.getString("title").length() > 32) {
            return false;
        }
        return compoundTag.contains("author", 8);
    }

    public static int getGeneration(ItemStack itemStack) {
        return itemStack.getTag().getInt("generation");
    }

    public static int getPageCount(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            return tag.getList("pages", 8).size();
        }
        return 0;
    }

    @Override // net.minecraft.world.item.Item
    public Component getName(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            String string = itemStack.getTag().getString("title");
            if (!StringUtil.isNullOrEmpty(string)) {
                return new TextComponent(string);
            }
        }
        return super.getName(itemStack);
    }

    @Override // net.minecraft.world.item.Item
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        if (itemStack.hasTag()) {
            CompoundTag tag = itemStack.getTag();
            String string = tag.getString("author");
            if (!StringUtil.isNullOrEmpty(string)) {
                list.add(new TranslatableComponent("book.byAuthor", string).withStyle(ChatFormatting.GRAY));
            }
            list.add(new TranslatableComponent("book.generation." + tag.getInt("generation")).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos clickedPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(clickedPos);
        if (blockState.is(Blocks.LECTERN)) {
            return LecternBlock.tryPlaceBook(level, clickedPos, blockState, useOnContext.getItemInHand()) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        player.openItemGui(itemInHand, interactionHand);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemInHand, level.isClientSide());
    }

    public static boolean resolveBookComponents(ItemStack itemStack, @Nullable CommandSourceStack commandSourceStack, @Nullable Player player) {
        Component textComponent;
        CompoundTag tag = itemStack.getTag();
        if (tag == null || tag.getBoolean("resolved")) {
            return false;
        }
        tag.putBoolean("resolved", true);
        if (!makeSureTagIsValid(tag)) {
            return false;
        }
        ListTag list = tag.getList("pages", 8);
        for (int i = 0; i < list.size(); i++) {
            String string = list.getString(i);
            try {
                textComponent = ComponentUtils.updateForEntity(commandSourceStack, Component.Serializer.fromJsonLenient(string), player, 0);
            } catch (Exception e) {
                textComponent = new TextComponent(string);
            }
            list.set(i, (Tag) StringTag.valueOf(Component.Serializer.toJson(textComponent)));
        }
        tag.put("pages", list);
        return true;
    }

    @Override // net.minecraft.world.item.Item
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }
}
