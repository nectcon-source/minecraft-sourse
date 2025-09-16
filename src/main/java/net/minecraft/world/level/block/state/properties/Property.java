package net.minecraft.world.level.block.state.properties;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.StateHolder;

public abstract class Property<T extends Comparable<T>> {
    private final Class<T> clazz;
    private final String name;
    private Integer hashCode;
    private final Codec<T> codec = Codec.STRING
            .comapFlatMap(
                    var1x -> this.getValue(var1x)
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error("Unable to read property: " + this + " with value: " + var1x)),
                    this::getName
            );
    private final Codec<Property.Value<T>> valueCodec = this.codec.xmap(this::value, Property.Value::value);

    protected Property(String var1, Class<T> var2) {
        this.clazz = var2;
        this.name = var1;
    }

    public Property.Value<T> value(T var1) {
        return new Property.Value<>(this, var1);
    }

    public Property.Value<T> value(StateHolder<?, ?> var1) {
        return new Property.Value<>(this, var1.getValue(this));
    }

    public Stream<Property.Value<T>> getAllValues() {
        return this.getPossibleValues().stream().map(this::value);
    }

    public Codec<Property.Value<T>> valueCodec() {
        return this.valueCodec;
    }

    public String getName() {
        return this.name;
    }

    public Class<T> getValueClass() {
        return this.clazz;
    }

    public abstract Collection<T> getPossibleValues();

    public abstract String getName(T var1);

    public abstract Optional<T> getValue(String var1);

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.clazz).add("values", this.getPossibleValues()).toString();
    }

    @Override
    public boolean equals(Object var1) {
        if (this == var1) {
            return true;
        } else if (!(var1 instanceof Property)) {
            return false;
        } else {
            Property<?> var2 = (Property)var1;
            return this.clazz.equals(var2.clazz) && this.name.equals(var2.name);
        }
    }

    @Override
    public final int hashCode() {
        if (this.hashCode == null) {
            this.hashCode = this.generateHashCode();
        }

        return this.hashCode;
    }

    public int generateHashCode() {
        return 31 * this.clazz.hashCode() + this.name.hashCode();
    }

    public static final class Value<T extends Comparable<T>> {
        private final Property<T> property;
        private final T value;

        private Value(Property<T> var1, T var2) {
            if (!var1.getPossibleValues().contains(var2)) {
                throw new IllegalArgumentException("Value " + var2 + " does not belong to property " + var1);
            } else {
                this.property = var1;
                this.value = var2;
            }
        }

        public Property<T> getProperty() {
            return this.property;
        }

        public T value() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.property.getName() + "=" + this.property.getName(this.value);
        }

        @Override
        public boolean equals(Object var1) {
            if (this == var1) {
                return true;
            } else if (!(var1 instanceof Property.Value)) {
                return false;
            } else {
                Property.Value<?> var2 = (Property.Value)var1;
                return this.property == var2.property && this.value.equals(var2.value);
            }
        }

        @Override
        public int hashCode() {
            int var1 = this.property.hashCode();
            return 31 * var1 + this.value.hashCode();
        }
    }
}
