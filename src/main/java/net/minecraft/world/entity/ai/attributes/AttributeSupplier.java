package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/attributes/AttributeSupplier.class */
public class AttributeSupplier {
    private final Map<Attribute, AttributeInstance> instances;

    public AttributeSupplier(Map<Attribute, AttributeInstance> map) {
        this.instances = ImmutableMap.copyOf(map);
    }

    private AttributeInstance getAttributeInstance(Attribute attribute) {
        AttributeInstance attributeInstance = this.instances.get(attribute);
        if (attributeInstance == null) {
            throw new IllegalArgumentException("Can't find attribute " + Registry.ATTRIBUTE.getKey(attribute));
        }
        return attributeInstance;
    }

    public double getValue(Attribute attribute) {
        return getAttributeInstance(attribute).getValue();
    }

    public double getBaseValue(Attribute attribute) {
        return getAttributeInstance(attribute).getBaseValue();
    }

    public double getModifierValue(Attribute attribute, UUID uuid) {
        AttributeModifier modifier = getAttributeInstance(attribute).getModifier(uuid);
        if (modifier == null) {
            throw new IllegalArgumentException("Can't find modifier " + uuid + " on attribute " + Registry.ATTRIBUTE.getKey(attribute));
        }
        return modifier.getAmount();
    }

    @Nullable
    public AttributeInstance createInstance(Consumer<AttributeInstance> consumer, Attribute attribute) {
        AttributeInstance attributeInstance = this.instances.get(attribute);
        if (attributeInstance == null) {
            return null;
        }
        AttributeInstance attributeInstance2 = new AttributeInstance(attribute, consumer);
        attributeInstance2.replaceFrom(attributeInstance);
        return attributeInstance2;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean hasAttribute(Attribute attribute) {
        return this.instances.containsKey(attribute);
    }

    public boolean hasModifier(Attribute attribute, UUID uuid) {
        AttributeInstance attributeInstance = this.instances.get(attribute);
        return (attributeInstance == null || attributeInstance.getModifier(uuid) == null) ? false : true;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder.class */
    public static class Builder {
        private final Map<Attribute, AttributeInstance> builder = Maps.newHashMap();
        private boolean instanceFrozen;

        private AttributeInstance create(Attribute attribute) {
            AttributeInstance attributeInstance = new AttributeInstance(attribute, attributeInstance2 -> {
                if (this.instanceFrozen) {
                    throw new UnsupportedOperationException("Tried to change value for default attribute instance: " + Registry.ATTRIBUTE.getKey(attribute));
                }
            });
            this.builder.put(attribute, attributeInstance);
            return attributeInstance;
        }

        public Builder add(Attribute attribute) {
            create(attribute);
            return this;
        }

        public Builder add(Attribute attribute, double d) {
            create(attribute).setBaseValue(d);
            return this;
        }

        public AttributeSupplier build() {
            this.instanceFrozen = true;
            return new AttributeSupplier(this.builder);
        }
    }
}
