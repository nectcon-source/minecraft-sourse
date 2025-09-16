package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/FishBucketItem.class */
public class FishBucketItem extends BucketItem {
    private final EntityType<?> type;

    public FishBucketItem(EntityType<?> entityType, Fluid fluid, Item.Properties properties) {
        super(fluid, properties);
        this.type = entityType;
    }

    @Override // net.minecraft.world.item.BucketItem
    public void checkExtraContent(Level level, ItemStack itemStack, BlockPos blockPos) {
        if (level instanceof ServerLevel) {
            spawn((ServerLevel) level, itemStack, blockPos);
        }
    }

    @Override // net.minecraft.world.item.BucketItem
    protected void playEmptySound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos) {
        levelAccessor.playSound(player, blockPos, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.NEUTRAL, 1.0f, 1.0f);
    }

    private void spawn(ServerLevel serverLevel, ItemStack itemStack, BlockPos blockPos) {
        Entity spawn = this.type.spawn(serverLevel, itemStack, null, blockPos, MobSpawnType.BUCKET, true, false);
        if (spawn != null) {
            ((AbstractFish) spawn).setFromBucket(true);
        }
    }

    @Override // net.minecraft.world.item.Item
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag tag;
        if (this.type == EntityType.TROPICAL_FISH && (tag = itemStack.getTag()) != null && tag.contains("BucketVariantTag", 3)) {
            int i = tag.getInt("BucketVariantTag");
            ChatFormatting[] chatFormattingArr = {ChatFormatting.ITALIC, ChatFormatting.GRAY};
            String str = "color.minecraft." + TropicalFish.getBaseColor(i);
            String str2 = "color.minecraft." + TropicalFish.getPatternColor(i);
            for (int i2 = 0; i2 < TropicalFish.COMMON_VARIANTS.length; i2++) {
                if (i == TropicalFish.COMMON_VARIANTS[i2]) {
                    list.add(new TranslatableComponent(TropicalFish.getPredefinedName(i2)).withStyle(chatFormattingArr));
                    return;
                }
            }
            list.add(new TranslatableComponent(TropicalFish.getFishTypeName(i)).withStyle(chatFormattingArr));
            MutableComponent translatableComponent = new TranslatableComponent(str);
            if (!str.equals(str2)) {
                translatableComponent.append(", ").append(new TranslatableComponent(str2));
            }
            translatableComponent.withStyle(chatFormattingArr);
            list.add(translatableComponent);
        }
    }
}
