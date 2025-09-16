package net.minecraft.world.level.storage.loot.functions;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/LootItemFunction.class */
public interface LootItemFunction extends LootContextUser, BiFunction<ItemStack, LootContext, ItemStack> {

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/LootItemFunction$Builder.class */
    public interface Builder {
        LootItemFunction build();
    }

    LootItemFunctionType getType();

    static Consumer<ItemStack> decorate(BiFunction<ItemStack, LootContext, ItemStack> biFunction, Consumer<ItemStack> consumer, LootContext lootContext) {
        return itemStack -> {
            consumer.accept(biFunction.apply(itemStack, lootContext));
        };
    }
}
