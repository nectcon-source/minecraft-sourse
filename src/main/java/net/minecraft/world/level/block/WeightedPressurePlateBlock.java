package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/WeightedPressurePlateBlock.class */
public class WeightedPressurePlateBlock extends BasePressurePlateBlock {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    private final int maxWeight;

    protected WeightedPressurePlateBlock(int i, BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(POWER, 0));
        this.maxWeight = i;
    }

    @Override // net.minecraft.world.level.block.BasePressurePlateBlock
    protected int getSignalStrength(Level level, BlockPos blockPos) {
        int var3 = Math.min(level.getEntitiesOfClass(Entity.class, TOUCH_AABB.move(blockPos)).size(), this.maxWeight);
        if (var3 > 0) {
            float var4 = (float)Math.min(this.maxWeight, var3) / (float)this.maxWeight;
            return Mth.ceil(var4 * 15.0F);
        } else {
            return 0;
        }
    }

    @Override // net.minecraft.world.level.block.BasePressurePlateBlock
    protected void playOnSound(LevelAccessor levelAccessor, BlockPos blockPos) {
        levelAccessor.playSound(null, blockPos, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.3f, 0.90000004f);
    }

    @Override // net.minecraft.world.level.block.BasePressurePlateBlock
    protected void playOffSound(LevelAccessor levelAccessor, BlockPos blockPos) {
        levelAccessor.playSound(null, blockPos, SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.3f, 0.75f);
    }

    @Override // net.minecraft.world.level.block.BasePressurePlateBlock
    protected int getSignalForState(BlockState blockState) {
        return ((Integer) blockState.getValue(POWER)).intValue();
    }

    @Override // net.minecraft.world.level.block.BasePressurePlateBlock
    protected BlockState setSignalForState(BlockState blockState, int i) {
        return (BlockState) blockState.setValue(POWER, Integer.valueOf(i));
    }

    @Override // net.minecraft.world.level.block.BasePressurePlateBlock
    protected int getPressedTime() {
        return 10;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }
}
