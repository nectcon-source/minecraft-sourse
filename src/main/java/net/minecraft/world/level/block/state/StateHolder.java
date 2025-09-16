package net.minecraft.world.level.block.state;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.Property;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/StateHolder.class */
public abstract class StateHolder<O, S> {
    private static final Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Map.Entry<Property<?>, Comparable<?>>, String>() { // from class: net.minecraft.world.level.block.state.StateHolder.1
        @Override // java.util.function.Function
        public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> entry) {
            if (entry == null) {
                return "<NULL>";
            }
            Property<?> key = entry.getKey();
            return key.getName() + "=" + getName(key, entry.getValue());
        }

        /* JADX WARN: Multi-variable type inference failed */
        private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
            return property.getName((T) comparable);
        }
    };
    protected final O owner;
    private final ImmutableMap<Property<?>, Comparable<?>> values;
    private Table<Property<?>, Comparable<?>, S> neighbours;
    protected final MapCodec<S> propertiesCodec;

    protected StateHolder(O o, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<S> mapCodec) {
        this.owner = o;
        this.values = immutableMap;
        this.propertiesCodec = mapCodec;
    }

    public <T extends Comparable<T>> S cycle(Property<T> property) {
        return (S) setValue(property,  findNextInCollection(property.getPossibleValues(), getValue(property)));
    }

    protected static <T> T findNextInCollection(Collection<T> collection, T t) {
        Iterator<T> it = collection.iterator();
        while (it.hasNext()) {
            if (it.next().equals(t)) {
                if (it.hasNext()) {
                    return it.next();
                }
                return collection.iterator().next();
            }
        }
        return it.next();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.owner);
        if (!getValues().isEmpty()) {
            sb.append('[');
            sb.append( getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
            sb.append(']');
        }
        return sb.toString();
    }

    public Collection<Property<?>> getProperties() {
        return Collections.unmodifiableCollection(this.values.keySet());
    }

    public <T extends Comparable<T>> boolean hasProperty(Property<T> property) {
        return this.values.containsKey(property);
    }

    public <T extends Comparable<T>> T getValue(Property<T> property) {
        Comparable<?> comparable =  this.values.get(property);
        if (comparable == null) {
            throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + this.owner);
        }
        return property.getValueClass().cast(comparable);
    }

    public <T extends Comparable<T>> Optional<T> getOptionalValue(Property<T> property) {
        Comparable<?> comparable =  this.values.get(property);
        if (comparable == null) {
            return Optional.empty();
        }
        return Optional.of(property.getValueClass().cast(comparable));
    }

    public <T extends Comparable<T>, V extends T> S setValue(Property<T> var1, V var2) {
        Comparable<?> var3 = this.values.get(var1);
        if (var3 == null) {
            throw new IllegalArgumentException("Cannot set property " + var1 + " as it does not exist in " + this.owner);
        } else if (var3 == var2) {
            return (S)this;
        } else {
            S var4 = this.neighbours.get(var1, var2);
            if (var4 == null) {
                throw new IllegalArgumentException("Cannot set property " + var1 + " to " + var2 + " on " + this.owner + ", it is not an allowed value");
            } else {
                return var4;
            }
        }
    }

    public void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> map) {
        if (this.neighbours != null) {
            throw new IllegalStateException();
        }
        Table<Property<?>, Comparable<?>, S> create = HashBasedTable.create();
        UnmodifiableIterator it = this.values.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Property<?>, Comparable<?>> entry = (Map.Entry) it.next();
            Property<?> key = entry.getKey();
            Iterator<?> it2 = key.getPossibleValues().iterator();
            while (it2.hasNext()) {
                Comparable<?> comparable = (Comparable) it2.next();
                if (comparable != entry.getValue()) {
                    create.put(key, comparable, map.get(makeNeighbourValues(key, comparable)));
                }
            }
        }
        this.neighbours = create.isEmpty() ? create : ArrayTable.create(create);
    }

    private Map<Property<?>, Comparable<?>> makeNeighbourValues(Property<?> property, Comparable<?> comparable) {
        Map<Property<?>, Comparable<?>> newHashMap = Maps.newHashMap(this.values);
        newHashMap.put(property, comparable);
        return newHashMap;
    }

    public ImmutableMap<Property<?>, Comparable<?>> getValues() {
        return this.values;
    }

    protected static <O, S extends StateHolder<O, S>> Codec<S> codec(Codec<O> var0, Function<O, S> var1) {
        return var0.dispatch("Name", var0x -> var0x.owner, var1x -> {
            S var2 = var1.apply(var1x);
            return var2.getValues().isEmpty() ? Codec.unit(var2) : var2.propertiesCodec.fieldOf("Properties").codec();
        });
    }
}
