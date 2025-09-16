package net.minecraft.world.level.storage.loot.entries;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/entries/SequentialEntry.class */
public class SequentialEntry extends CompositeEntryBase {
    SequentialEntry(LootPoolEntryContainer[] lootPoolEntryContainerArr, LootItemCondition[] lootItemConditionArr) {
        super(lootPoolEntryContainerArr, lootItemConditionArr);
    }

    @Override // net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer
    public LootPoolEntryType getType() {
        return LootPoolEntries.SEQUENCE;
    }

    @Override // net.minecraft.world.level.storage.loot.entries.CompositeEntryBase
    protected ComposableEntryContainer compose(ComposableEntryContainer[] composableEntryContainerArr) {
        switch (composableEntryContainerArr.length) {
            case 0:
                return ALWAYS_TRUE;
            case 1:
                return composableEntryContainerArr[0];
            case 2:
                return composableEntryContainerArr[0].and(composableEntryContainerArr[1]);
            default:
                return (lootContext, consumer) -> {
                    for (ComposableEntryContainer composableEntryContainer : composableEntryContainerArr) {
                        if (!composableEntryContainer.expand(lootContext, consumer)) {
                            return false;
                        }
                    }
                    return true;
                };
        }
    }
}
