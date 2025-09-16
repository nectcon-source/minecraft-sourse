package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.Direction;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/properties/DirectionProperty.class */
public class DirectionProperty extends EnumProperty<Direction> {
    protected DirectionProperty(String str, Collection<Direction> collection) {
        super(str, Direction.class, collection);
    }

    public static DirectionProperty create(String str, Predicate<Direction> predicate) {
        return create(str, (Collection<Direction>) Arrays.stream(Direction.values()).filter(predicate).collect(Collectors.toList()));
    }

    public static DirectionProperty create(String str, Direction... directionArr) {
        return create(str, Lists.newArrayList(directionArr));
    }

    public static DirectionProperty create(String str, Collection<Direction> collection) {
        return new DirectionProperty(str, collection);
    }
}
