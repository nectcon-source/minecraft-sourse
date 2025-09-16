package net.minecraft.world.inventory;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/inventory/ContainerLevelAccess.class */
public interface ContainerLevelAccess {
    public static final ContainerLevelAccess NULL = new ContainerLevelAccess() { // from class: net.minecraft.world.inventory.ContainerLevelAccess.1
        @Override // net.minecraft.world.inventory.ContainerLevelAccess
        public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> biFunction) {
            return Optional.empty();
        }
    };

    <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> biFunction);

    static ContainerLevelAccess create(final Level level, final BlockPos blockPos) {
        return new ContainerLevelAccess() { // from class: net.minecraft.world.inventory.ContainerLevelAccess.2
            @Override // net.minecraft.world.inventory.ContainerLevelAccess
            public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> biFunction) {
                return Optional.of(biFunction.apply(level, blockPos));
            }
        };
    }

    default <T> T evaluate(BiFunction<Level, BlockPos, T> biFunction, T t) {
        return evaluate(biFunction).orElse(t);
    }

    default void execute(BiConsumer<Level, BlockPos> biConsumer) {
        evaluate((level, blockPos) -> {
            biConsumer.accept(level, blockPos);
            return Optional.empty();
        });
    }
}
