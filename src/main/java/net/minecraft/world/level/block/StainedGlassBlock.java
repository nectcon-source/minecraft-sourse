package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/StainedGlassBlock.class */
public class StainedGlassBlock extends AbstractGlassBlock implements BeaconBeamBlock {
    private final DyeColor color;

    public StainedGlassBlock(DyeColor dyeColor, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = dyeColor;
    }

    @Override // net.minecraft.world.level.block.BeaconBeamBlock
    public DyeColor getColor() {
        return this.color;
    }
}
