package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/CauldronBlock.class */
public class CauldronBlock extends Block {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
    private static final VoxelShape INSIDE = box(2.0d, 4.0d, 2.0d, 14.0d, 16.0d, 14.0d);
    protected static final VoxelShape SHAPE = Shapes.join(Shapes.block(), Shapes.or(box(0.0d, 0.0d, 4.0d, 16.0d, 3.0d, 12.0d), box(4.0d, 0.0d, 0.0d, 12.0d, 3.0d, 16.0d), box(2.0d, 0.0d, 2.0d, 14.0d, 3.0d, 14.0d), INSIDE), BooleanOp.ONLY_FIRST);

    public CauldronBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState( this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return INSIDE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        int intValue = ((Integer) blockState.getValue(LEVEL)).intValue();
        float y = blockPos.getY() + ((6.0f + (3 * intValue)) / 16.0f);
        if (!level.isClientSide && entity.isOnFire() && intValue > 0 && entity.getY() <= y) {
            entity.clearFire();
            setWaterLevel(level, blockPos, blockState, intValue - 1);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (itemInHand.isEmpty()) {
            return InteractionResult.PASS;
        }
        int intValue = ((Integer) blockState.getValue(LEVEL)).intValue();
        ItemLike item = itemInHand.getItem();
        if (item == Items.WATER_BUCKET) {
            if (intValue < 3 && !level.isClientSide) {
                if (!player.abilities.instabuild) {
                    player.setItemInHand(interactionHand, new ItemStack(Items.BUCKET));
                }
                player.awardStat(Stats.FILL_CAULDRON);
                setWaterLevel(level, blockPos, blockState, 3);
                level.playSound((Player) null, blockPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (item == Items.BUCKET) {
            if (intValue == 3 && !level.isClientSide) {
                if (!player.abilities.instabuild) {
                    itemInHand.shrink(1);
                    if (itemInHand.isEmpty()) {
                        player.setItemInHand(interactionHand, new ItemStack(Items.WATER_BUCKET));
                    } else if (!player.inventory.add(new ItemStack(Items.WATER_BUCKET))) {
                        player.drop(new ItemStack(Items.WATER_BUCKET), false);
                    }
                }
                player.awardStat(Stats.USE_CAULDRON);
                setWaterLevel(level, blockPos, blockState, 0);
                level.playSound((Player) null, blockPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (item == Items.GLASS_BOTTLE) {
            if (intValue > 0 && !level.isClientSide) {
                if (!player.abilities.instabuild) {
                    ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                    player.awardStat(Stats.USE_CAULDRON);
                    itemInHand.shrink(1);
                    if (itemInHand.isEmpty()) {
                        player.setItemInHand(interactionHand, potion);
                    } else if (!player.inventory.add(potion)) {
                        player.drop(potion, false);
                    } else if (player instanceof ServerPlayer) {
                        ((ServerPlayer) player).refreshContainer(player.inventoryMenu);
                    }
                }
                level.playSound((Player) null, blockPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                setWaterLevel(level, blockPos, blockState, intValue - 1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (item == Items.POTION && PotionUtils.getPotion(itemInHand) == Potions.WATER) {
            if (intValue < 3 && !level.isClientSide) {
                if (!player.abilities.instabuild) {
                    ItemStack itemStack = new ItemStack(Items.GLASS_BOTTLE);
                    player.awardStat(Stats.USE_CAULDRON);
                    player.setItemInHand(interactionHand, itemStack);
                    if (player instanceof ServerPlayer) {
                        ((ServerPlayer) player).refreshContainer(player.inventoryMenu);
                    }
                }
                level.playSound((Player) null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                setWaterLevel(level, blockPos, blockState, intValue + 1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (intValue > 0 && (item instanceof DyeableLeatherItem)) {
            DyeableLeatherItem dyeableLeatherItem = (DyeableLeatherItem) item;
            if (dyeableLeatherItem.hasCustomColor(itemInHand) && !level.isClientSide) {
                dyeableLeatherItem.clearColor(itemInHand);
                setWaterLevel(level, blockPos, blockState, intValue - 1);
                player.awardStat(Stats.CLEAN_ARMOR);
                return InteractionResult.SUCCESS;
            }
        }
        if (intValue > 0 && (item instanceof BannerItem)) {
            if (BannerBlockEntity.getPatternCount(itemInHand) > 0 && !level.isClientSide) {
                ItemStack copy = itemInHand.copy();
                copy.setCount(1);
                BannerBlockEntity.removeLastPattern(copy);
                player.awardStat(Stats.CLEAN_BANNER);
                if (!player.abilities.instabuild) {
                    itemInHand.shrink(1);
                    setWaterLevel(level, blockPos, blockState, intValue - 1);
                }
                if (itemInHand.isEmpty()) {
                    player.setItemInHand(interactionHand, copy);
                } else if (!player.inventory.add(copy)) {
                    player.drop(copy, false);
                } else if (player instanceof ServerPlayer) {
                    ((ServerPlayer) player).refreshContainer(player.inventoryMenu);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (intValue > 0 && (item instanceof BlockItem)) {
            if ((((BlockItem) item).getBlock() instanceof ShulkerBoxBlock) && !level.isClientSide()) {
                ItemStack itemStack2 = new ItemStack(Blocks.SHULKER_BOX, 1);
                if (itemInHand.hasTag()) {
                    itemStack2.setTag(itemInHand.getTag().copy());
                }
                player.setItemInHand(interactionHand, itemStack2);
                setWaterLevel(level, blockPos, blockState, intValue - 1);
                player.awardStat(Stats.CLEAN_SHULKER_BOX);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public void setWaterLevel(Level level, BlockPos blockPos, BlockState blockState, int i) {
        level.setBlock(blockPos, (BlockState) blockState.setValue(LEVEL, Integer.valueOf(Mth.clamp(i, 0, 3))), 2);
        level.updateNeighbourForOutputSignal(blockPos, this);
    }

    @Override // net.minecraft.world.level.block.Block
    public void handleRain(Level level, BlockPos blockPos) {
        if (level.random.nextInt(20) != 1 || level.getBiome(blockPos).getTemperature(blockPos) < 0.15f) {
            return;
        }
        BlockState blockState = level.getBlockState(blockPos);
        if (((Integer) blockState.getValue(LEVEL)).intValue() < 3) {
            level.setBlock(blockPos, blockState.cycle(LEVEL), 2);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return ((Integer) blockState.getValue(LEVEL)).intValue();
    }

    @Override // net.minecraft.world.level.block.Block
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
