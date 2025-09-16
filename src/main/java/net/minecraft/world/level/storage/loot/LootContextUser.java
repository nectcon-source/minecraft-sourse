package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/LootContextUser.class */
public interface LootContextUser {
    default Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of();
    }

    default void validate(ValidationContext validationContext) {
        validationContext.validateUser(this);
    }
}
