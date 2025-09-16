package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/properties/IntegerProperty.class */
public class IntegerProperty extends Property<Integer> {
    private final ImmutableSet<Integer> values;

    protected IntegerProperty(String str, int i, int i2) {
        super(str, Integer.class);
        if (i < 0) {
            throw new IllegalArgumentException("Min value of " + str + " must be 0 or greater");
        }
        if (i2 <= i) {
            throw new IllegalArgumentException("Max value of " + str + " must be greater than min (" + i + ")");
        }
        Set<Integer> newHashSet = Sets.newHashSet();
        for (int i3 = i; i3 <= i2; i3++) {
            newHashSet.add(Integer.valueOf(i3));
        }
        this.values = ImmutableSet.copyOf(newHashSet);
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public Collection<Integer> getPossibleValues() {
        return this.values;
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj instanceof IntegerProperty) && super.equals(obj)) {
            return this.values.equals(((IntegerProperty) obj).values);
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public int generateHashCode() {
        return (31 * super.generateHashCode()) + this.values.hashCode();
    }

    public static IntegerProperty create(String str, int i, int i2) {
        return new IntegerProperty(str, i, i2);
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public Optional<Integer> getValue(String str) {
        try {
            Integer valueOf = Integer.valueOf(str);
            return this.values.contains(valueOf) ? Optional.of(valueOf) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public String getName(Integer num) {
        return num.toString();
    }
}
