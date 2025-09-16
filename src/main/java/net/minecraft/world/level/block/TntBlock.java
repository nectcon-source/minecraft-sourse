package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/TntBlock.class */
public class TntBlock extends Block {
    public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

    public TntBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) defaultBlockState().setValue(UNSTABLE, false));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (!blockState2.is(blockState.getBlock()) && level.hasNeighborSignal(blockPos)) {
            explode(level, blockPos);
            level.removeBlock(blockPos, false);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean z) {
        if (level.hasNeighborSignal(blockPos)) {
            explode(level, blockPos);
            level.removeBlock(blockPos, false);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide() && !player.isCreative() && ((Boolean) blockState.getValue(UNSTABLE)).booleanValue()) {
            explode(level, blockPos);
        }
        super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override // net.minecraft.world.level.block.Block
    public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
        if (level.isClientSide) {
            return;
        }
        PrimedTnt primedTnt = new PrimedTnt(level, blockPos.getX() + 0.5d, blockPos.getY(), blockPos.getZ() + 0.5d, explosion.getSourceMob());
        primedTnt.setFuse((short) (level.random.nextInt(primedTnt.getLife() / 4) + (primedTnt.getLife() / 8)));
        level.addFreshEntity(primedTnt);
    }

    public static void explode(Level level, BlockPos blockPos) {
        explode(level, blockPos, null);
    }

    private static void explode(Level level, BlockPos blockPos, @Nullable LivingEntity livingEntity) {
        if (level.isClientSide) {
            return;
        }
        PrimedTnt primedTnt = new PrimedTnt(level, blockPos.getX() + 0.5d, blockPos.getY(), blockPos.getZ() + 0.5d, livingEntity);
        level.addFreshEntity(primedTnt);
        level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        Item item = itemInHand.getItem();
        if (item == Items.FLINT_AND_STEEL || item == Items.FIRE_CHARGE) {
            explode(level, blockPos, player);
            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
            if (!player.isCreative()) {
                if (item == Items.FLINT_AND_STEEL) {
                    itemInHand.hurtAndBreak(1, player, player2 -> {
                        player2.broadcastBreakEvent(interactionHand);
                    });
                } else {
                    itemInHand.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        if (!level.isClientSide) {
            Entity owner = projectile.getOwner();
            if (projectile.isOnFire()) {
                BlockPos blockPos = blockHitResult.getBlockPos();
                explode(level, blockPos, owner instanceof LivingEntity ? (LivingEntity) owner : null);
                level.removeBlock(blockPos, false);
            }
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UNSTABLE);
    }
}
