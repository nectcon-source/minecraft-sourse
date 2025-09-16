package net.minecraft.world.level.block;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/PressurePlateBlock.class */
public class PressurePlateBlock extends BasePressurePlateBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private final Sensitivity sensitivity;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/PressurePlateBlock$Sensitivity.class */
    public enum Sensitivity {
        EVERYTHING,
        MOBS
    }

    protected PressurePlateBlock(Sensitivity sensitivity, BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(POWERED, false));
        this.sensitivity = sensitivity;
    }

    @Override // net.minecraft.world.level.block.BasePressurePlateBlock
    protected int getSignalForState(BlockState blockState) {
        return ((Boolean) blockState.getValue(POWERED)).booleanValue() ? 15 : 0;
    }

    @Override // net.minecraft.world.level.block.BasePressurePlateBlock
    protected BlockState setSignalForState(BlockState blockState, int i) {
        return (BlockState) blockState.setValue(POWERED, Boolean.valueOf(i > 0));
    }

    @Override // net.minecraft.world.level.block.BasePressurePlateBlock
    protected void playOnSound(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (this.material == Material.WOOD || this.material == Material.NETHER_WOOD) {
            levelAccessor.playSound(null, blockPos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.3f, 0.8f);
        } else {
            levelAccessor.playSound(null, blockPos, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.3f, 0.6f);
        }
    }

    @Override // net.minecraft.world.level.block.BasePressurePlateBlock
    protected void playOffSound(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (this.material == Material.WOOD || this.material == Material.NETHER_WOOD) {
            levelAccessor.playSound(null, blockPos, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.3f, 0.7f);
        } else {
            levelAccessor.playSound(null, blockPos, SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.3f, 0.5f);
        }
    }

    @Override // net.minecraft.world.level.block.BasePressurePlateBlock
    protected int getSignalStrength(Level level, BlockPos blockPos) {
        List<? extends Entity> entitiesOfClass;
        AABB move = TOUCH_AABB.move(blockPos);
        switch (this.sensitivity) {
            case EVERYTHING:
                entitiesOfClass = level.getEntities(null, move);
                break;
            case MOBS:
                entitiesOfClass = level.getEntitiesOfClass(LivingEntity.class, move);
                break;
            default:
                return 0;
        }
        if (!entitiesOfClass.isEmpty()) {
            Iterator<? extends Entity> it = entitiesOfClass.iterator();
            while (it.hasNext()) {
                if (!it.next().isIgnoringBlockTriggers()) {
                    return 15;
                }
            }
            return 0;
        }
        return 0;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}
