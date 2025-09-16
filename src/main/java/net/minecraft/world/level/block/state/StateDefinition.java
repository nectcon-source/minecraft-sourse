package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/StateDefinition.class */
public class StateDefinition<O, S extends StateHolder<O, S>> {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
    private final O owner;
    private final ImmutableSortedMap<String, Property<?>> propertiesByName;
    private final ImmutableList<S> states;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/StateDefinition$Factory.class */
    public interface Factory<O, S> {
        S create(O o, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<S> mapCodec);
    }

    protected StateDefinition(Function<O, S> function, O o, Factory<O, S> factory, Map<String, Property<?>> map) {
        this.owner = o;
        this.propertiesByName = ImmutableSortedMap.copyOf(map);
        Supplier<S> supplier = () -> {
            return  function.apply(o);
        };
        MapCodec<S> of = MapCodec.of(Encoder.empty(), Decoder.unit(supplier));
        UnmodifiableIterator it = this.propertiesByName.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Property<?>> entry = (Map.Entry) it.next();
            of = appendPropertyCodec(of, supplier, entry.getKey(), entry.getValue());
        }
        MapCodec<S> mapCodec = of;
        LinkedHashMap newLinkedHashMap = Maps.newLinkedHashMap();
        List<S> newArrayList = Lists.newArrayList();
        Stream<List<Pair<Property<?>, Comparable<?>>>> of2 = Stream.of(Collections.emptyList());
        UnmodifiableIterator it2 = this.propertiesByName.values().iterator();
        while (it2.hasNext()) {
            Property<?> property = (Property) it2.next();
            of2 = of2.flatMap(list -> {
                return property.getPossibleValues().stream().map(comparable -> {
                    List<Pair<Property<?>, Comparable<?>>> newArrayList2 = Lists.newArrayList(list);
                    newArrayList2.add(Pair.of(property, comparable));
                    return newArrayList2;
                });
            });
        }
        of2.forEach(list2 -> {
            ImmutableMap<Property<?>, Comparable<?>> immutableMap =  list2.stream().collect(ImmutableMap.toImmutableMap((v0) -> {
                return v0.getFirst();
            }, (v0) -> {
                return v0.getSecond();
            }));
            StateHolder stateHolder =  factory.create(o, immutableMap, mapCodec);
            newLinkedHashMap.put(immutableMap, stateHolder);
            newArrayList.add((S) stateHolder);
        });
        Iterator<S> it3 = newArrayList.iterator();
        while (it3.hasNext()) {
            it3.next().populateNeighbours(newLinkedHashMap);
        }
        this.states = ImmutableList.copyOf(newArrayList);
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> MapCodec<S> appendPropertyCodec(MapCodec<S> mapCodec, Supplier<S> supplier, String str, Property<T> property) {
        return Codec.mapPair(mapCodec, property.valueCodec().fieldOf(str).setPartial(() -> {
            return property.value( supplier.get());
        })).xmap(pair -> {
            return (S) ( pair.getFirst()).setValue(property, ( pair.getSecond()).value());
        }, stateHolder -> {
            return Pair.of(stateHolder, property.value(stateHolder));
        });

    }

    public ImmutableList<S> getPossibleStates() {
        return this.states;
    }

    public S any() {
        return  this.states.get(0);
    }

    public O getOwner() {
        return this.owner;
    }

    public Collection<Property<?>> getProperties() {
        return this.propertiesByName.values();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("block", this.owner).add("properties", this.propertiesByName.values().stream().map((v0) -> v0.getName()).collect(Collectors.toList())).toString();
    }

    @Nullable
    public Property<?> getProperty(String str) {
        return this.propertiesByName.get(str);
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/StateDefinition$Builder.class */
    public static class Builder<O, S extends StateHolder<O, S>> {
        private final O owner;
        private final Map<String, Property<?>> properties = Maps.newHashMap();

        public Builder(O o) {
            this.owner = o;
        }

//        /* JADX WARN: Multi-variable type inference failed */
//        public Builder<O, S> add(Property<?>... propertyArr) {
//            for (IntegerProperty integerProperty : (IntegerProperty[]) propertyArr) {
//                validateProperty(integerProperty);
//                this.properties.put(integerProperty.getName(), integerProperty);
//            }
//            return this;
//        }

        public StateDefinition.Builder<O, S> add(Property<?>... var1) {
            for(Property<?> var2 : var1) {
                this.validateProperty(var2);
                this.properties.put(var2.getName(), var2);
            }

            return this;
        }



        private <T extends Comparable<T>> void validateProperty(Property<T> property) {
            String name = property.getName();
            if (!StateDefinition.NAME_PATTERN.matcher(name).matches()) {
                throw new IllegalArgumentException(this.owner + " has invalidly named property: " + name);
            }
            Collection<T> possibleValues = property.getPossibleValues();
            if (possibleValues.size() <= 1) {
                throw new IllegalArgumentException(this.owner + " attempted use property " + name + " with <= 1 possible values");
            }
            Iterator<T> it = possibleValues.iterator();
            while (it.hasNext()) {
                String name2 = property.getName(it.next());
                if (!StateDefinition.NAME_PATTERN.matcher(name2).matches()) {
                    throw new IllegalArgumentException(this.owner + " has property: " + name + " with invalidly named value: " + name2);
                }
            }
            if (this.properties.containsKey(name)) {
                throw new IllegalArgumentException(this.owner + " has duplicate property: " + name);
            }
        }

        public StateDefinition<O, S> create(Function<O, S> function, Factory<O, S> factory) {
            return new StateDefinition<>(function, this.owner, factory, this.properties);
        }
    }
}
