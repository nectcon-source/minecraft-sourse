package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/FireworkRocketItem.class */
public class FireworkRocketItem extends Item {
    public FireworkRocketItem(Item.Properties properties) {
        super(properties);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/item/FireworkRocketItem$Shape.class */
    public enum Shape {
        SMALL_BALL(0, "small_ball"),
        LARGE_BALL(1, "large_ball"),
        STAR(2, "star"),
        CREEPER(3, "creeper"),
        BURST(4, "burst");

        private static final Shape[] BY_ID = (Shape[]) Arrays.stream(values()).sorted(Comparator.comparingInt(shape -> {
            return shape.id;
        })).toArray(i -> {
            return new Shape[i];
        });

        /* renamed from: id */
        private final int id;
        private final String name;

        Shape(int i, String str) {
            this.id = i;
            this.name = str;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public static Shape byId(int i) {
            if (i < 0 || i >= BY_ID.length) {
                return SMALL_BALL;
            }
            return BY_ID[i];
        }
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        if (!level.isClientSide) {
            ItemStack itemInHand = useOnContext.getItemInHand();
            Vec3 clickLocation = useOnContext.getClickLocation();
            Direction clickedFace = useOnContext.getClickedFace();
            level.addFreshEntity(new FireworkRocketEntity(level, useOnContext.getPlayer(), clickLocation.x + (clickedFace.getStepX() * 0.15d), clickLocation.y + (clickedFace.getStepY() * 0.15d), clickLocation.z + (clickedFace.getStepZ() * 0.15d), itemInHand));
            itemInHand.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        if (player.isFallFlying()) {
            ItemStack itemInHand = player.getItemInHand(interactionHand);
            if (!level.isClientSide) {
                level.addFreshEntity(new FireworkRocketEntity(level, itemInHand, player));
                if (!player.abilities.instabuild) {
                    itemInHand.shrink(1);
                }
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(interactionHand), level.isClientSide());
        }
        return InteractionResultHolder.pass(player.getItemInHand(interactionHand));
    }

    @Override // net.minecraft.world.item.Item
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tagElement = itemStack.getTagElement("Fireworks");
        if (tagElement == null) {
            return;
        }
        if (tagElement.contains("Flight", 99)) {
            list.add(new TranslatableComponent("item.minecraft.firework_rocket.flight").append(" ").append(String.valueOf((int) tagElement.getByte("Flight"))).withStyle(ChatFormatting.GRAY));
        }
        ListTag list2 = tagElement.getList("Explosions", 10);
        if (!list2.isEmpty()) {
            for (int i = 0; i < list2.size(); i++) {
                CompoundTag compound = list2.getCompound(i);
                List<Component> newArrayList = Lists.newArrayList();
                FireworkStarItem.appendHoverText(compound, newArrayList);
                if (!newArrayList.isEmpty()) {
                    for (int i2 = 1; i2 < newArrayList.size(); i2++) {
                        newArrayList.set(i2, new TextComponent("  ").append(newArrayList.get(i2)).withStyle(ChatFormatting.GRAY));
                    }
                    list.addAll(newArrayList);
                }
            }
        }
    }
}
