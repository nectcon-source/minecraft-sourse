package net.minecraft.world.level.block.state.properties;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.Enum;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/properties/EnumProperty.class */
public class EnumProperty<T extends Enum<T> & StringRepresentable> extends Property<T> {
    private final ImmutableSet<T> values;
    private final Map<String, T> names;

    protected EnumProperty(String str, Class<T> cls, Collection<T> collection) {
        super(str, cls);
        this.names = Maps.newHashMap();
        this.values = ImmutableSet.copyOf(collection);
        for (T t : collection) {
            String serializedName = t.getSerializedName();
            if (this.names.containsKey(serializedName)) {
                throw new IllegalArgumentException("Multiple values have the same name '" + serializedName + "'");
            }
            this.names.put(serializedName, t);
        }
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public Collection<T> getPossibleValues() {
        return this.values;
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public Optional<T> getValue(String str) {
        return Optional.ofNullable(this.names.get(str));
    }

    /* JADX WARN: Incorrect types in method signature: (TT;)Ljava/lang/String; */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // net.minecraft.world.level.block.state.properties.Property
    public String getName(Enum r3) {
        return ((StringRepresentable) r3).getSerializedName();
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj instanceof EnumProperty) && super.equals(obj)) {
            EnumProperty<?> enumProperty = (EnumProperty) obj;
            return this.values.equals(enumProperty.values) && this.names.equals(enumProperty.names);
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.state.properties.Property
    public int generateHashCode() {
        return (31 * ((31 * super.generateHashCode()) + this.values.hashCode())) + this.names.hashCode();
    }

    public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String str, Class<T> cls) {
        return create(str, (Class) cls, (Predicate) Predicates.alwaysTrue());
    }

    public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String str, Class<T> cls, Predicate<T> predicate) {
        return create(str, cls,  Arrays.stream(cls.getEnumConstants()).filter(predicate).collect(Collectors.toList()));
    }

    /* JADX WARN: Incorrect types in method signature: <T:Ljava/lang/Enum<TT;>;:Lnet/minecraft/util/StringRepresentable;>(Ljava/lang/String;Ljava/lang/Class<TT;>;[TT;)Lnet/minecraft/world/level/block/state/properties/EnumProperty<TT;>; */
    public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String str, Class<T> cls, T... enumArr) {
        return create(str, cls, Lists.newArrayList(enumArr));
    }

    public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String str, Class<T> cls, Collection<T> collection) {
        return new EnumProperty<>(str, cls, collection);
    }
}
