package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/CopyNbtFunction.class */
public class CopyNbtFunction extends LootItemConditionalFunction {
    private final DataSource source;
    private final List<CopyOperation> operations;
    private static final Function<Entity, Tag> ENTITY_GETTER = NbtPredicate::getEntityTagToCompare;
    private static final Function<BlockEntity, Tag> BLOCK_ENTITY_GETTER = blockEntity -> {
        return blockEntity.save(new CompoundTag());
    };

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/CopyNbtFunction$CopyOperation.class */
    static class CopyOperation {
        private final String sourcePathText;
        private final NbtPathArgument.NbtPath sourcePath;
        private final String targetPathText;
        private final NbtPathArgument.NbtPath targetPath;

        /* renamed from: op */
        private final MergeStrategy op;

        private CopyOperation(String str, String str2, MergeStrategy mergeStrategy) {
            this.sourcePathText = str;
            this.sourcePath = CopyNbtFunction.compileNbtPath(str);
            this.targetPathText = str2;
            this.targetPath = CopyNbtFunction.compileNbtPath(str2);
            this.op = mergeStrategy;
        }

        public void apply(Supplier<Tag> supplier, Tag tag)  {
            try {
                List<Tag> list = this.sourcePath.get(tag);
                if (!list.isEmpty()) {
                    this.op.merge(supplier.get(), this.targetPath, list);
                }
            } catch (CommandSyntaxException var4) {
            }
        }

        public JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("source", this.sourcePathText);
            jsonObject.addProperty("target", this.targetPathText);
            jsonObject.addProperty("op", this.op.name);
            return jsonObject;
        }

        public static CopyOperation fromJson(JsonObject jsonObject) {
            return new CopyOperation(GsonHelper.getAsString(jsonObject, "source"), GsonHelper.getAsString(jsonObject, "target"), MergeStrategy.getByName(GsonHelper.getAsString(jsonObject, "op")));
        }
    }

    private CopyNbtFunction(LootItemCondition[] lootItemConditionArr, DataSource dataSource, List<CopyOperation> list) {
        super(lootItemConditionArr);
        this.source = dataSource;
        this.operations = ImmutableList.copyOf(list);
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction
    public LootItemFunctionType getType() {
        return LootItemFunctions.COPY_NBT;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static NbtPathArgument.NbtPath compileNbtPath(String str) {
        try {
            return new NbtPathArgument().parse(new StringReader(str));
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException("Failed to parse path " + str, e);
        }
    }

    @Override // net.minecraft.world.level.storage.loot.LootContextUser
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.source.param);
    }

    @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {

        Tag apply = this.source.getter.apply(lootContext);
        if (apply != null) {this.operations.forEach(var2x -> var2x.apply(itemStack::getOrCreateTag, apply));
        }

        return itemStack;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/CopyNbtFunction$Builder.class */
    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private final DataSource source;
        private final List<CopyOperation> ops;

        private Builder(DataSource dataSource) {
            this.ops = Lists.newArrayList();
            this.source = dataSource;
        }

        public Builder copy(String str, String str2, MergeStrategy mergeStrategy) {
            this.ops.add(new CopyOperation(str, str2, mergeStrategy));
            return this;
        }

        public Builder copy(String str, String str2) {
            return copy(str, str2, MergeStrategy.REPLACE);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Builder
        public Builder getThis() {
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder
        public LootItemFunction build() {
            return new CopyNbtFunction(getConditions(), this.source, this.ops);
        }
    }

    public static Builder copyData(DataSource dataSource) {
        return new Builder(dataSource);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/CopyNbtFunction$MergeStrategy.class */
    public enum MergeStrategy {
        REPLACE("replace") { // from class: net.minecraft.world.level.storage.loot.functions.CopyNbtFunction.MergeStrategy.1
            @Override // net.minecraft.world.level.storage.loot.functions.CopyNbtFunction.MergeStrategy
            public void merge(Tag tag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException {
                Tag tag2 = (Tag) Iterables.getLast(list);
                tag2.getClass();
                nbtPath.set(tag, tag2::copy);
            }
        },
        APPEND("append") { // from class: net.minecraft.world.level.storage.loot.functions.CopyNbtFunction.MergeStrategy.2
            @Override // net.minecraft.world.level.storage.loot.functions.CopyNbtFunction.MergeStrategy
            public void merge(Tag tag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException {
                nbtPath.getOrCreate(tag, ListTag::new).forEach(tag2 -> {
                    if (tag2 instanceof ListTag) {
                        list.forEach(tag2_ -> {
                            ((ListTag) tag2).add(tag2_.copy());
                        });
                    }
                });
            }
        },
        MERGE("merge") { // from class: net.minecraft.world.level.storage.loot.functions.CopyNbtFunction.MergeStrategy.3
            @Override // net.minecraft.world.level.storage.loot.functions.CopyNbtFunction.MergeStrategy
            public void merge(Tag tag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException {
                nbtPath.getOrCreate(tag, CompoundTag::new).forEach(tag2 -> {
                    if (tag2 instanceof CompoundTag) {
                        list.forEach(tag2_ -> {
                            if (tag2_ instanceof CompoundTag) {
                                ((CompoundTag) tag2).merge((CompoundTag) tag2_);
                            }
                        });
                    }
                });
            }
        };

        private final String name;

        public abstract void merge(Tag tag, NbtPathArgument.NbtPath nbtPath, List<Tag> list) throws CommandSyntaxException;

        MergeStrategy(String str) {
            this.name = str;
        }

        public static MergeStrategy getByName(String str) {
            for (MergeStrategy mergeStrategy : values()) {
                if (mergeStrategy.name.equals(str)) {
                    return mergeStrategy;
                }
            }
            throw new IllegalArgumentException("Invalid merge strategy" + str);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/CopyNbtFunction$DataSource.class */
    public enum DataSource {
        THIS("this", LootContextParams.THIS_ENTITY, CopyNbtFunction.ENTITY_GETTER),
        KILLER("killer", LootContextParams.KILLER_ENTITY, CopyNbtFunction.ENTITY_GETTER),
        KILLER_PLAYER("killer_player", LootContextParams.LAST_DAMAGE_PLAYER, CopyNbtFunction.ENTITY_GETTER),
        BLOCK_ENTITY("block_entity", LootContextParams.BLOCK_ENTITY, CopyNbtFunction.BLOCK_ENTITY_GETTER);

        public final String name;
        public final LootContextParam<?> param;
        public final Function<LootContext, Tag> getter;

        DataSource(String str, LootContextParam lootContextParam, Function function) {
            this.name = str;
            this.param = lootContextParam;
            this.getter = lootContext -> {
                Object paramOrNull = lootContext.getParamOrNull(lootContextParam);
                if (paramOrNull != null) {
                    return (Tag) function.apply(paramOrNull);
                }
                return null;
            };
        }

        public static DataSource getByName(String str) {
            for (DataSource dataSource : values()) {
                if (dataSource.name.equals(str)) {
                    return dataSource;
                }
            }
            throw new IllegalArgumentException("Invalid tag source " + str);
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/functions/CopyNbtFunction$Serializer.class */
    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyNbtFunction> {
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer, net.minecraft.world.level.storage.loot.Serializer
        public void serialize(JsonObject jsonObject, CopyNbtFunction copyNbtFunction, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, copyNbtFunction, jsonSerializationContext);
            jsonObject.addProperty("source", copyNbtFunction.source.name);
            JsonArray var4 = new JsonArray();
            copyNbtFunction.operations.stream().map(CopyOperation::toJson).forEach(var4::add);
            jsonObject.add("ops", var4);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction.Serializer
        public CopyNbtFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditionArr) {
            DataSource byName = DataSource.getByName(GsonHelper.getAsString(jsonObject, "source"));
            List<CopyOperation> newArrayList = Lists.newArrayList();
            Iterator it = GsonHelper.getAsJsonArray(jsonObject, "ops").iterator();
            while (it.hasNext()) {
                newArrayList.add(CopyOperation.fromJson(GsonHelper.convertToJsonObject((JsonElement) it.next(), "op")));
            }
            return new CopyNbtFunction(lootItemConditionArr, byName, newArrayList);
        }
    }
}
