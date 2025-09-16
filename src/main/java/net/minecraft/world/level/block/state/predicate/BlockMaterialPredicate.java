package net.minecraft.world.level.block.state.predicate;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/state/predicate/BlockMaterialPredicate.class */
public class BlockMaterialPredicate implements Predicate<BlockState> {
    private static final BlockMaterialPredicate AIR = new BlockMaterialPredicate(Material.AIR) { // from class: net.minecraft.world.level.block.state.predicate.BlockMaterialPredicate.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.block.state.predicate.BlockMaterialPredicate, java.util.function.Predicate
        public boolean test(@Nullable BlockState blockState) {
            return blockState != null && blockState.isAir();
        }
    };
    private final Material material;

    private BlockMaterialPredicate(Material material) {
        this.material = material;
    }

    public static BlockMaterialPredicate forMaterial(Material material) {
        return material == Material.AIR ? AIR : new BlockMaterialPredicate(material);
    }

    @Override // java.util.function.Predicate
    public boolean test(@Nullable BlockState blockState) {
        return blockState != null && blockState.getMaterial() == this.material;
    }
}
