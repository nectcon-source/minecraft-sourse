package net.minecraft.world;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/Containers.class */
public class Containers {
    private static final Random RANDOM = new Random();

    public static void dropContents(Level level, BlockPos blockPos, Container container) {
        dropContents(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), container);
    }

    public static void dropContents(Level level, Entity entity, Container container) {
        dropContents(level, entity.getX(), entity.getY(), entity.getZ(), container);
    }

    private static void dropContents(Level level, double d, double d2, double d3, Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            dropItemStack(level, d, d2, d3, container.getItem(i));
        }
    }

    public static void dropContents(Level level, BlockPos blockPos, NonNullList<ItemStack> nonNullList) {
        nonNullList.forEach(itemStack -> {
            dropItemStack(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack);
        });
    }

    public static void dropItemStack(Level level, double d, double d2, double d3, ItemStack itemStack) {
        double width = EntityType.ITEM.getWidth();
        double d4 = 1.0d - width;
        double d5 = width / 2.0d;
        double floor = Math.floor(d) + (RANDOM.nextDouble() * d4) + d5;
        double floor2 = Math.floor(d2) + (RANDOM.nextDouble() * d4);
        double floor3 = Math.floor(d3) + (RANDOM.nextDouble() * d4) + d5;
        while (!itemStack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level, floor, floor2, floor3, itemStack.split(RANDOM.nextInt(21) + 10));
            itemEntity.setDeltaMovement(RANDOM.nextGaussian() * 0.05000000074505806d, (RANDOM.nextGaussian() * 0.05000000074505806d) + 0.20000000298023224d, RANDOM.nextGaussian() * 0.05000000074505806d);
            level.addFreshEntity(itemEntity);
        }
    }
}
