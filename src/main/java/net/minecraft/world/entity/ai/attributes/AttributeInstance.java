package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/attributes/AttributeInstance.class */
public class AttributeInstance {
    private final Attribute attribute;
    private double baseValue;
    private double cachedValue;
    private final Consumer<AttributeInstance> onDirty;
    private final Map<AttributeModifier.Operation, Set<AttributeModifier>> modifiersByOperation = Maps.newEnumMap(AttributeModifier.Operation.class);
    private final Map<UUID, AttributeModifier> modifierById = new Object2ObjectArrayMap();
    private final Set<AttributeModifier> permanentModifiers = new ObjectArraySet();
    private boolean dirty = true;

    public AttributeInstance(Attribute attribute, Consumer<AttributeInstance> consumer) {
        this.attribute = attribute;
        this.onDirty = consumer;
        this.baseValue = attribute.getDefaultValue();
    }

    public Attribute getAttribute() {
        return this.attribute;
    }

    public double getBaseValue() {
        return this.baseValue;
    }

    public void setBaseValue(double d) {
        if (d == this.baseValue) {
            return;
        }
        this.baseValue = d;
        setDirty();
    }

    public Set<AttributeModifier> getModifiers(AttributeModifier.Operation operation) {
        return this.modifiersByOperation.computeIfAbsent(operation, operation2 -> {
            return Sets.newHashSet();
        });
    }

    public Set<AttributeModifier> getModifiers() {
        return ImmutableSet.copyOf(this.modifierById.values());
    }

    @Nullable
    public AttributeModifier getModifier(UUID uuid) {
        return this.modifierById.get(uuid);
    }

    public boolean hasModifier(AttributeModifier attributeModifier) {
        return this.modifierById.get(attributeModifier.getId()) != null;
    }

    private void addModifier(AttributeModifier attributeModifier) {
        if (this.modifierById.putIfAbsent(attributeModifier.getId(), attributeModifier) != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        }
        getModifiers(attributeModifier.getOperation()).add(attributeModifier);
        setDirty();
    }

    public void addTransientModifier(AttributeModifier attributeModifier) {
        addModifier(attributeModifier);
    }

    public void addPermanentModifier(AttributeModifier attributeModifier) {
        addModifier(attributeModifier);
        this.permanentModifiers.add(attributeModifier);
    }

    protected void setDirty() {
        this.dirty = true;
        this.onDirty.accept(this);
    }

    public void removeModifier(AttributeModifier attributeModifier) {
        getModifiers(attributeModifier.getOperation()).remove(attributeModifier);
        this.modifierById.remove(attributeModifier.getId());
        this.permanentModifiers.remove(attributeModifier);
        setDirty();
    }

    public void removeModifier(UUID uuid) {
        AttributeModifier modifier = getModifier(uuid);
        if (modifier != null) {
            removeModifier(modifier);
        }
    }

    public boolean removePermanentModifier(UUID uuid) {
        AttributeModifier modifier = getModifier(uuid);
        if (modifier != null && this.permanentModifiers.contains(modifier)) {
            removeModifier(modifier);
            return true;
        }
        return false;
    }

    public void removeModifiers() {
        Iterator<AttributeModifier> it = getModifiers().iterator();
        while (it.hasNext()) {
            removeModifier(it.next());
        }
    }

    public double getValue() {
        if (this.dirty) {
            this.cachedValue = calculateValue();
            this.dirty = false;
        }
        return this.cachedValue;
    }

    private double calculateValue() {
        double baseValue = getBaseValue();
        Iterator<AttributeModifier> it = getModifiersOrEmpty(AttributeModifier.Operation.ADDITION).iterator();
        while (it.hasNext()) {
            baseValue += it.next().getAmount();
        }
        double d = baseValue;
        Iterator<AttributeModifier> it2 = getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_BASE).iterator();
        while (it2.hasNext()) {
            d += baseValue * it2.next().getAmount();
        }
        Iterator<AttributeModifier> it3 = getModifiersOrEmpty(AttributeModifier.Operation.MULTIPLY_TOTAL).iterator();
        while (it3.hasNext()) {
            d *= 1.0d + it3.next().getAmount();
        }
        return this.attribute.sanitizeValue(d);
    }

    private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation operation) {
        return this.modifiersByOperation.getOrDefault(operation, Collections.emptySet());
    }

    public void replaceFrom(AttributeInstance attributeInstance) {
        this.baseValue = attributeInstance.baseValue;
        this.modifierById.clear();
        this.modifierById.putAll(attributeInstance.modifierById);
        this.permanentModifiers.clear();
        this.permanentModifiers.addAll(attributeInstance.permanentModifiers);
        this.modifiersByOperation.clear();
        attributeInstance.modifiersByOperation.forEach((operation, set) -> {
            getModifiers(operation).addAll(set);
        });
        setDirty();
    }

    public CompoundTag save() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Name", Registry.ATTRIBUTE.getKey(this.attribute).toString());
        compoundTag.putDouble("Base", this.baseValue);
        if (!this.permanentModifiers.isEmpty()) {
            ListTag listTag = new ListTag();
            Iterator<AttributeModifier> it = this.permanentModifiers.iterator();
            while (it.hasNext()) {
                listTag.add(it.next().save());
            }
            compoundTag.put("Modifiers", listTag);
        }
        return compoundTag;
    }

    public void load(CompoundTag compoundTag) {
        this.baseValue = compoundTag.getDouble("Base");
        if (compoundTag.contains("Modifiers", 9)) {
            ListTag list = compoundTag.getList("Modifiers", 10);
            for (int i = 0; i < list.size(); i++) {
                AttributeModifier load = AttributeModifier.load(list.getCompound(i));
                if (load != null) {
                    this.modifierById.put(load.getId(), load);
                    getModifiers(load.getOperation()).add(load);
                    this.permanentModifiers.add(load);
                }
            }
        }
        setDirty();
    }
}
