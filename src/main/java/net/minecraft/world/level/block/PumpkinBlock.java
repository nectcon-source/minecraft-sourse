package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/PumpkinBlock.class */
public class PumpkinBlock extends StemGrownBlock {
    protected PumpkinBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (itemInHand.getItem() == Items.SHEARS) {
            if (!level.isClientSide) {
                Direction direction = blockHitResult.getDirection();
                Direction opposite = direction.getAxis() == Direction.Axis.Y ? player.getDirection().getOpposite() : direction;
                level.playSound((Player) null, blockPos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.setBlock(blockPos, (BlockState) Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, opposite), 11);
                ItemEntity itemEntity = new ItemEntity(level, blockPos.getX() + 0.5d + (opposite.getStepX() * 0.65d), blockPos.getY() + 0.1d, blockPos.getZ() + 0.5d + (opposite.getStepZ() * 0.65d), new ItemStack(Items.PUMPKIN_SEEDS, 4));
                itemEntity.setDeltaMovement((0.05d * opposite.getStepX()) + (level.random.nextDouble() * 0.02d), 0.05d, (0.05d * opposite.getStepZ()) + (level.random.nextDouble() * 0.02d));
                level.addFreshEntity(itemEntity);
                itemInHand.hurtAndBreak(1, player, player2 -> {
                    player2.broadcastBreakEvent(interactionHand);
                });
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override // net.minecraft.world.level.block.StemGrownBlock
    public StemBlock getStem() {
        return (StemBlock) Blocks.PUMPKIN_STEM;
    }

    @Override // net.minecraft.world.level.block.StemGrownBlock
    public AttachedStemBlock getAttachedStem() {
        return (AttachedStemBlock) Blocks.ATTACHED_PUMPKIN_STEM;
    }
}
