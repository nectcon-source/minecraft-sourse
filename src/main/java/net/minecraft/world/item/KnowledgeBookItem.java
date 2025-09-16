package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/KnowledgeBookItem.class */
public class KnowledgeBookItem extends Item {
    private static final Logger LOGGER = LogManager.getLogger();

    public KnowledgeBookItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        CompoundTag tag = itemInHand.getTag();
        if (!player.abilities.instabuild) {
            player.setItemInHand(interactionHand, ItemStack.EMPTY);
        }
        if (tag == null || !tag.contains("Recipes", 9)) {
            LOGGER.error("Tag not valid: {}", tag);
            return InteractionResultHolder.fail(itemInHand);
        }
        if (!level.isClientSide) {
            ListTag list = tag.getList("Recipes", 8);
            List<Recipe<?>> newArrayList = Lists.newArrayList();
            RecipeManager recipeManager = level.getServer().getRecipeManager();
            for (int i = 0; i < list.size(); i++) {
                String string = list.getString(i);
                Optional<? extends Recipe<?>> byKey = recipeManager.byKey(new ResourceLocation(string));
                if (byKey.isPresent()) {
                    newArrayList.add(byKey.get());
                } else {
                    LOGGER.error("Invalid recipe: {}", string);
                    return InteractionResultHolder.fail(itemInHand);
                }
            }
            player.awardRecipes(newArrayList);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResultHolder.sidedSuccess(itemInHand, level.isClientSide());
    }
}
