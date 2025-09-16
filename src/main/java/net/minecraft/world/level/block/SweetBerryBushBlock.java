package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/SweetBerryBushBlock.class */
public class SweetBerryBushBlock extends BushBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape SAPLING_SHAPE = Block.box(3.0d, 0.0d, 3.0d, 13.0d, 8.0d, 13.0d);
    private static final VoxelShape MID_GROWTH_SHAPE = Block.box(1.0d, 0.0d, 1.0d, 15.0d, 16.0d, 15.0d);

    public SweetBerryBushBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override // net.minecraft.world.level.block.Block
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return new ItemStack(Items.SWEET_BERRIES);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (((Integer) blockState.getValue(AGE)).intValue() == 0) {
            return SAPLING_SHAPE;
        }
        if (((Integer) blockState.getValue(AGE)).intValue() < 3) {
            return MID_GROWTH_SHAPE;
        }
        return super.getShape(blockState, blockGetter, blockPos, collisionContext);
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean isRandomlyTicking(BlockState blockState) {
        return ((Integer) blockState.getValue(AGE)).intValue() < 3;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        int intValue = ((Integer) blockState.getValue(AGE)).intValue();
        if (intValue < 3 && random.nextInt(5) == 0 && serverLevel.getRawBrightness(blockPos.above(), 0) >= 9) {
            serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(AGE, Integer.valueOf(intValue + 1)), 2);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (!(entity instanceof LivingEntity) || entity.getType() == EntityType.FOX || entity.getType() == EntityType.BEE) {
            return;
        }
        entity.makeStuckInBlock(blockState, new Vec3(0.800000011920929d, 0.75d, 0.800000011920929d));
        if (level.isClientSide || ((Integer) blockState.getValue(AGE)).intValue() <= 0) {
            return;
        }
        if (entity.xOld != entity.getX() || entity.zOld != entity.getZ()) {
            double abs = Math.abs(entity.getX() - entity.xOld);
            double abs2 = Math.abs(entity.getZ() - entity.zOld);
            if (abs >= 0.003000000026077032d || abs2 >= 0.003000000026077032d) {
                entity.hurt(DamageSource.SWEET_BERRY_BUSH, 1.0f);
            }
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        int intValue = ((Integer) blockState.getValue(AGE)).intValue();
        boolean z = intValue == 3;
        if (!z && player.getItemInHand(interactionHand).getItem() == Items.BONE_MEAL) {
            return InteractionResult.PASS;
        }
        if (intValue > 1) {
            popResource(level, blockPos, new ItemStack(Items.SWEET_BERRIES, 1 + level.random.nextInt(2) + (z ? 1 : 0)));
            level.playSound((Player) null, blockPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 0.8f + (level.random.nextFloat() * 0.4f));
            level.setBlock(blockPos, (BlockState) blockState.setValue(AGE, 1), 2);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean z) {
        return ((Integer) blockState.getValue(AGE)).intValue() < 3;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.BonemealableBlock
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(AGE, Integer.valueOf(Math.min(3, ((Integer) blockState.getValue(AGE)).intValue() + 1))), 2);
    }
}
