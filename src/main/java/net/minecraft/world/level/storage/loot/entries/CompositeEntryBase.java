package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/CompositeEntryBase.class */
public abstract class CompositeEntryBase extends LootPoolEntryContainer {
    protected final LootPoolEntryContainer[] children;
    private final ComposableEntryContainer composedChildren;

    @FunctionalInterface
    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/CompositeEntryBase$CompositeEntryConstructor.class */
    public interface CompositeEntryConstructor<T extends CompositeEntryBase> {
        T create(LootPoolEntryContainer[] lootPoolEntryContainerArr, LootItemCondition[] lootItemConditionArr);
    }

    protected abstract ComposableEntryContainer compose(ComposableEntryContainer[] composableEntryContainerArr);

    protected CompositeEntryBase(LootPoolEntryContainer[] lootPoolEntryContainerArr, LootItemCondition[] lootItemConditionArr) {
        super(lootItemConditionArr);
        this.children = lootPoolEntryContainerArr;
        this.composedChildren = compose(lootPoolEntryContainerArr);
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        if (this.children.length == 0) {
            validationContext.reportProblem("Empty children list");
        }
        for (int i = 0; i < this.children.length; i++) {
            this.children[i].validate(validationContext.forChild(".entry[" + i + "]"));
        }
    }

    @Override // net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer
    public final boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
        if (!canRun(lootContext)) {
            return false;
        }
        return this.composedChildren.expand(lootContext, consumer);
    }

//    public static <T extends CompositeEntryBase> LootPoolEntryContainer.Serializer<T> createSerializer(final CompositeEntryConstructor<T> compositeEntryConstructor) {
//        return (LootPoolEntryContainer.Serializer<T>) new LootPoolEntryContainer.Serializer<T>() { // from class: net.minecraft.world.level.storage.loot.entries.CompositeEntryBase.1
//            /* JADX WARN: Incorrect types in method signature: (Lcom/google/gson/JsonObject;TT;Lcom/google/gson/JsonSerializationContext;)V */
//            @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Serializer
//            public void serializeCustom(JsonObject jsonObject, CompositeEntryBase compositeEntryBase, JsonSerializationContext jsonSerializationContext) {
//                jsonObject.add("children", jsonSerializationContext.serialize(compositeEntryBase.children));
//            }
//
//            /* JADX WARN: Incorrect return type in method signature: (Lcom/google/gson/JsonObject;Lcom/google/gson/JsonDeserializationContext;[Lnet/minecraft/world/level/storage/loot/predicates/LootItemCondition;)TT; */
//            @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Serializer
//            public final CompositeEntryBase deserializeCustom(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
//                return CompositeEntryConstructor.this.create(GsonHelper.getAsObject(jsonObject, "children", jsonDeserializationContext, LootPoolEntryContainer[].class), lootItemConditionArr);
//            }
//        };
//    }
public static <T extends CompositeEntryBase> LootPoolEntryContainer.Serializer<T> createSerializer(final CompositeEntryConstructor<T> compositeEntryConstructor) {
    return new LootPoolEntryContainer.Serializer<T>() {
        @Override
        public void serializeCustom(JsonObject jsonObject, T compositeEntryBase, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("children", jsonSerializationContext.serialize(compositeEntryBase.children));
        }

        @Override
        public T deserializeCustom(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            return compositeEntryConstructor.create(
                    GsonHelper.getAsObject(jsonObject, "children", jsonDeserializationContext, LootPoolEntryContainer[].class),
                    lootItemConditions
            );
        }
    };
}
}
