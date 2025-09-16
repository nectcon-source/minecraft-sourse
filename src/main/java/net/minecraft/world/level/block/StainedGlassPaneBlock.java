package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/StainedGlassPaneBlock.class */
public class StainedGlassPaneBlock extends IronBarsBlock implements BeaconBeamBlock {
    private final DyeColor color;

    public StainedGlassPaneBlock(DyeColor dyeColor, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = dyeColor;
        registerDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any().setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false)).setValue(WATERLOGGED, false));
    }

    @Override // net.minecraft.world.level.block.BeaconBeamBlock
    public DyeColor getColor() {
        return this.color;
    }
}
