package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/properties/BooleanProperty.class */
public class BooleanProperty extends Property<Boolean> {
    private final ImmutableSet<Boolean> values;

    protected BooleanProperty(String str) {
        super(str, Boolean.class);
        this.values = ImmutableSet.of(true, false);
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public Collection<Boolean> getPossibleValues() {
        return this.values;
    }

    public static BooleanProperty create(String str) {
        return new BooleanProperty(str);
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public Optional<Boolean> getValue(String str) {
        if ("true".equals(str) || "false".equals(str)) {
            return Optional.of(Boolean.valueOf(str));
        }
        return Optional.empty();
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public String getName(Boolean bool) {
        return bool.toString();
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj instanceof BooleanProperty) && super.equals(obj)) {
            return this.values.equals(((BooleanProperty) obj).values);
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public int generateHashCode() {
        return (31 * super.generateHashCode()) + this.values.hashCode();
    }
}
