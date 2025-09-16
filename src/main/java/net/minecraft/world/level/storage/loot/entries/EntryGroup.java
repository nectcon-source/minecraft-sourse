package net.minecraft.world.level.storage.loot.entries;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/EntryGroup.class */
public class EntryGroup extends CompositeEntryBase {
    EntryGroup(LootPoolEntryContainer[] lootPoolEntryContainerArr, LootItemCondition[] lootItemConditionArr) {
        super(lootPoolEntryContainerArr, lootItemConditionArr);
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
    public LootPoolEntryType getType() {
        return LootPoolEntries.GROUP;
    }

    @Override // net.minecraft.world.level.storage.loot.entries.CompositeEntryBase
    protected ComposableEntryContainer compose(ComposableEntryContainer[] composableEntryContainerArr) {
        switch (composableEntryContainerArr.length) {
            case 0:
                return ALWAYS_TRUE;
            case 1:
                return composableEntryContainerArr[0];
            case 2:
                ComposableEntryContainer composableEntryContainer = composableEntryContainerArr[0];
                ComposableEntryContainer composableEntryContainer2 = composableEntryContainerArr[1];
                return (lootContext, consumer) -> {
                    composableEntryContainer.expand(lootContext, consumer);
                    composableEntryContainer2.expand(lootContext, consumer);
                    return true;
                };
            default:
                return (lootContext2, consumer2) -> {
                    for (ComposableEntryContainer composableEntryContainer3 : composableEntryContainerArr) {
                        composableEntryContainer3.expand(lootContext2, consumer2);
                    }
                    return true;
                };
        }
    }
}
