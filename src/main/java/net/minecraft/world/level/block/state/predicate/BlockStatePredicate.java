package net.minecraft.world.level.block.state.predicate;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/predicate/BlockStatePredicate.class */
public class BlockStatePredicate implements Predicate<BlockState> {
    public static final Predicate<BlockState> ANY = blockState -> {
        return true;
    };
    private final StateDefinition<Block, BlockState> definition;
    private final Map<Property<?>, Predicate<Object>> properties = Maps.newHashMap();

    private BlockStatePredicate(StateDefinition<Block, BlockState> stateDefinition) {
        this.definition = stateDefinition;
    }

    public static BlockStatePredicate forBlock(Block block) {
        return new BlockStatePredicate(block.getStateDefinition());
    }

    @Override // java.util.function.Predicate
    public boolean test(@Nullable BlockState blockState) {
        if (blockState == null || !blockState.getBlock().equals(this.definition.getOwner())) {
            return false;
        }
        if (this.properties.isEmpty()) {
            return true;
        }
        for (Map.Entry<Property<?>, Predicate<Object>> entry : this.properties.entrySet()) {
            if (!applies(blockState, entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    protected <T extends Comparable<T>> boolean applies(BlockState blockState, Property<T> property, Predicate<Object> predicate) {
        return predicate.test(blockState.getValue(property));
    }

    public <V extends Comparable<V>> BlockStatePredicate where(Property<V> property, Predicate<Object> predicate) {
        if (!this.definition.getProperties().contains(property)) {
            throw new IllegalArgumentException(this.definition + " cannot support property " + property);
        }
        this.properties.put(property, predicate);
        return this;
    }
}
