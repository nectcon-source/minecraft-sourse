package net.minecraft.world.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/HangingEntityItem.class */
public class HangingEntityItem extends Item {
    private final EntityType<? extends HangingEntity> type;

    public HangingEntityItem(EntityType<? extends HangingEntity> entityType, Item.Properties properties) {
        super(properties);
        this.type = entityType;
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        HangingEntity itemFrame;
        BlockPos clickedPos = useOnContext.getClickedPos();
        Direction clickedFace = useOnContext.getClickedFace();
        BlockPos relative = clickedPos.relative(clickedFace);
        Player player = useOnContext.getPlayer();
        ItemStack itemInHand = useOnContext.getItemInHand();
        if (player != null && !mayPlace(player, clickedFace, itemInHand, relative)) {
            return InteractionResult.FAIL;
        }
        Level level = useOnContext.getLevel();
        if (this.type == EntityType.PAINTING) {
            itemFrame = new Painting(level, relative, clickedFace);
        } else if (this.type == EntityType.ITEM_FRAME) {
            itemFrame = new ItemFrame(level, relative, clickedFace);
        } else {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        CompoundTag tag = itemInHand.getTag();
        if (tag != null) {
            EntityType.updateCustomEntityTag(level, player, itemFrame, tag);
        }
        if (itemFrame.survives()) {
            if (!level.isClientSide) {
                itemFrame.playPlacementSound();
                level.addFreshEntity(itemFrame);
            }
            itemInHand.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.CONSUME;
    }

    protected boolean mayPlace(Player player, Direction direction, ItemStack itemStack, BlockPos blockPos){
        return !direction.getAxis().isVertical() && player.mayUseItemAt(blockPos, direction, itemStack);
    }
}
