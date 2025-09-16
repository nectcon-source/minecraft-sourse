package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/AlternativesEntry.class */
public class AlternativesEntry extends CompositeEntryBase {
    AlternativesEntry(LootPoolEntryContainer[] lootPoolEntryContainerArr, LootItemCondition[] lootItemConditionArr) {
        super(lootPoolEntryContainerArr, lootItemConditionArr);
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
    public LootPoolEntryType getType() {
        return LootPoolEntries.ALTERNATIVES;
    }

    @Override // net.minecraft.world.level.storage.loot.entries.CompositeEntryBase
    protected ComposableEntryContainer compose(ComposableEntryContainer[] composableEntryContainerArr) {
        switch (composableEntryContainerArr.length) {
            case 0:
                return ALWAYS_FALSE;
            case 1:
                return composableEntryContainerArr[0];
            case 2:
                return composableEntryContainerArr[0].or(composableEntryContainerArr[1]);
            default:
                return (lootContext, consumer) -> {
                    for (ComposableEntryContainer composableEntryContainer : composableEntryContainerArr) {
                        if (composableEntryContainer.expand(lootContext, consumer)) {
                            return true;
                        }
                    }
                    return false;
                };
        }
    }

    @Override // net.minecraft.world.level.storage.loot.entries.CompositeEntryBase, net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        for (int i = 0; i < this.children.length - 1; i++) {
            if (ArrayUtils.isEmpty(this.children[i].conditions)) {
                validationContext.reportProblem("Unreachable entry!");
            }
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/AlternativesEntry$Builder.class */
    public static class Builder extends LootPoolEntryContainer.Builder<Builder> {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

        public Builder(LootPoolEntryContainer.Builder<?>... builderArr) {
            for (LootPoolEntryContainer.Builder<?> builder : builderArr) {
                this.entries.add(builder.build());
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder
        public Builder getThis() {
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder
        public Builder otherwise(LootPoolEntryContainer.Builder<?> builder) {
            this.entries.add(builder.build());
            return this;
        }

        @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder
        public LootPoolEntryContainer build() {
            return new AlternativesEntry((LootPoolEntryContainer[]) this.entries.toArray(new LootPoolEntryContainer[0]), getConditions());
        }
    }

    public static Builder alternatives(LootPoolEntryContainer.Builder<?>... builderArr) {
        return new Builder(builderArr);
    }
}
