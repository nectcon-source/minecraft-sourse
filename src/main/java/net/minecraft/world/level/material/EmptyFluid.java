package net.minecraft.world.level.material;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/material/EmptyFluid.class */
public class EmptyFluid extends Fluid {
    @Override // net.minecraft.world.level.material.Fluid
    public Item getBucket() {
        return Items.AIR;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
        return true;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos, FluidState fluidState) {
        return Vec3.ZERO;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public int getTickDelay(LevelReader levelReader) {
        return 0;
    }

    @Override // net.minecraft.world.level.material.Fluid
    protected boolean isEmpty() {
        return true;
    }

    @Override // net.minecraft.world.level.material.Fluid
    protected float getExplosionResistance() {
        return 0.0f;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public float getHeight(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        return 0.0f;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public float getOwnHeight(FluidState fluidState) {
        return 0.0f;
    }

    @Override // net.minecraft.world.level.material.Fluid
    protected BlockState createLegacyBlock(FluidState fluidState) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override // net.minecraft.world.level.material.Fluid
    public boolean isSource(FluidState fluidState) {
        return false;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public int getAmount(FluidState fluidState) {
        return 0;
    }

    @Override // net.minecraft.world.level.material.Fluid
    public VoxelShape getShape(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.empty();
    }
}
