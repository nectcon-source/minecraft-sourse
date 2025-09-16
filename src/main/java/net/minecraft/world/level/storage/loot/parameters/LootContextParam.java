package net.minecraft.world.level.storage.loot.parameters;

import net.minecraft.resources.ResourceLocation;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/storage/loot/parameters/LootContextParam.class */
public class LootContextParam<T> {
    private final ResourceLocation name;

    public LootContextParam(ResourceLocation resourceLocation) {
        this.name = resourceLocation;
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public String toString() {
        return "<parameter " + this.name + ">";
    }
}
