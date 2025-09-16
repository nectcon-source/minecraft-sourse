package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.ValidationContext;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/parameters/LootContextParamSet.class */
public class LootContextParamSet {
    private final Set<LootContextParam<?>> required;
    private final Set<LootContextParam<?>> all;

    private LootContextParamSet(Set<LootContextParam<?>> set, Set<LootContextParam<?>> set2) {
        this.required = ImmutableSet.copyOf(set);
        this.all = ImmutableSet.copyOf(Sets.union(set, set2));
    }

    public Set<LootContextParam<?>> getRequired() {
        return this.required;
    }

    public Set<LootContextParam<?>> getAllowed() {
        return this.all;
    }

    public String toString() {
        return "[" + Joiner.on(", ").join(this.all.stream().map(lootContextParam -> {
            return (this.required.contains(lootContextParam) ? "!" : "") + lootContextParam.getName();
        }).iterator()) + "]";
    }

    public void validateUser(ValidationContext validationContext, LootContextUser lootContextUser) {
        Sets.SetView difference = Sets.difference(lootContextUser.getReferencedContextParams(), this.all);
        if (!difference.isEmpty()) {
            validationContext.reportProblem("Parameters " + difference + " are not provided in this context");
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/parameters/LootContextParamSet$Builder.class */
    public static class Builder {
        private final Set<LootContextParam<?>> required = Sets.newIdentityHashSet();
        private final Set<LootContextParam<?>> optional = Sets.newIdentityHashSet();

        public Builder required(LootContextParam<?> lootContextParam) {
            if (this.optional.contains(lootContextParam)) {
                throw new IllegalArgumentException("Parameter " + lootContextParam.getName() + " is already optional");
            }
            this.required.add(lootContextParam);
            return this;
        }

        public Builder optional(LootContextParam<?> lootContextParam) {
            if (this.required.contains(lootContextParam)) {
                throw new IllegalArgumentException("Parameter " + lootContextParam.getName() + " is already required");
            }
            this.optional.add(lootContextParam);
            return this;
        }

        public LootContextParamSet build() {
            return new LootContextParamSet(this.required, this.optional);
        }
    }
}
