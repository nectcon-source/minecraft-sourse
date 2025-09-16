package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/attributes/AttributeMap.class */
public class AttributeMap {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<Attribute, AttributeInstance> attributes = Maps.newHashMap();
    private final Set<AttributeInstance> dirtyAttributes = Sets.newHashSet();
    private final AttributeSupplier supplier;

    public AttributeMap(AttributeSupplier attributeSupplier) {
        this.supplier = attributeSupplier;
    }

    private void onAttributeModified(AttributeInstance attributeInstance) {
        if (attributeInstance.getAttribute().isClientSyncable()) {
            this.dirtyAttributes.add(attributeInstance);
        }
    }

    public Set<AttributeInstance> getDirtyAttributes() {
        return this.dirtyAttributes;
    }

    public Collection<AttributeInstance> getSyncableAttributes() {
        return (Collection) this.attributes.values().stream().filter(attributeInstance -> {
            return attributeInstance.getAttribute().isClientSyncable();
        }).collect(Collectors.toList());
    }

    @Nullable
    public AttributeInstance getInstance(Attribute attribute) {
        return this.attributes.computeIfAbsent(attribute, attribute2 -> {
            return this.supplier.createInstance(this::onAttributeModified, attribute2);
        });
    }

    public boolean hasAttribute(Attribute attribute) {
        return this.attributes.get(attribute) != null || this.supplier.hasAttribute(attribute);
    }

    public boolean hasModifier(Attribute attribute, UUID uuid) {
        AttributeInstance attributeInstance = this.attributes.get(attribute);
        return attributeInstance != null ? attributeInstance.getModifier(uuid) != null : this.supplier.hasModifier(attribute, uuid);
    }

    public double getValue(Attribute attribute) {
        AttributeInstance attributeInstance = this.attributes.get(attribute);
        return attributeInstance != null ? attributeInstance.getValue() : this.supplier.getValue(attribute);
    }

    public double getBaseValue(Attribute attribute) {
        AttributeInstance attributeInstance = this.attributes.get(attribute);
        return attributeInstance != null ? attributeInstance.getBaseValue() : this.supplier.getBaseValue(attribute);
    }

    public double getModifierValue(Attribute attribute, UUID uuid) {
        AttributeInstance attributeInstance = this.attributes.get(attribute);
        return attributeInstance != null ? attributeInstance.getModifier(uuid).getAmount() : this.supplier.getModifierValue(attribute, uuid);
    }

    public void removeAttributeModifiers(Multimap<Attribute, AttributeModifier> multimap) {
        multimap.asMap().forEach((attribute, collection) -> {
            AttributeInstance attributeInstance = this.attributes.get(attribute);
            if (attributeInstance != null) {
                attributeInstance.getClass();
                collection.forEach(attributeInstance::removeModifier);
            }
        });
    }

    public void addTransientAttributeModifiers(Multimap<Attribute, AttributeModifier> multimap) {
        multimap.forEach((attribute, attributeModifier) -> {
            AttributeInstance attributeMap = getInstance(attribute);
            if (attributeMap != null) {
                attributeMap.removeModifier(attributeModifier);
                attributeMap.addTransientModifier(attributeModifier);
            }
        });
    }

    public void assignValues(AttributeMap attributeMap) {
        attributeMap.attributes.values().forEach(attributeInstance -> {
            AttributeInstance attributeMap2 = getInstance(attributeInstance.getAttribute());
            if (attributeMap2 != null) {
                attributeMap2.replaceFrom(attributeInstance);
            }
        });
    }

    public ListTag save() {
        ListTag listTag = new ListTag();
        Iterator<AttributeInstance> it = this.attributes.values().iterator();
        while (it.hasNext()) {
            listTag.add(it.next().save());
        }
        return listTag;
    }

    public void load(ListTag listTag) {
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compound = listTag.getCompound(i);
            String string = compound.getString("Name");
            Util.ifElse(Registry.ATTRIBUTE.getOptional(ResourceLocation.tryParse(string)), attribute -> {
                AttributeInstance attributeMap = getInstance(attribute);
                if (attributeMap != null) {
                    attributeMap.load(compound);
                }
            }, () -> {
                LOGGER.warn("Ignoring unknown attribute '{}'", string);
            });
        }
    }
}
