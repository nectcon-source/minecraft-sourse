package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/FlowerPotBlock.class */
public class FlowerPotBlock extends Block {
    private static final Map<Block, Block> POTTED_BY_CONTENT = Maps.newHashMap();
    protected static final VoxelShape SHAPE = Block.box(5.0d, 0.0d, 5.0d, 11.0d, 6.0d, 11.0d);
    private final Block content;

    public FlowerPotBlock(Block block, BlockBehaviour.Properties properties) {
        super(properties);
        this.content = block;
        POTTED_BY_CONTENT.put(block, this);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        Item item = itemInHand.getItem();
        Block orDefault = item instanceof BlockItem ? POTTED_BY_CONTENT.getOrDefault(((BlockItem) item).getBlock(), Blocks.AIR) : Blocks.AIR;
        boolean z = orDefault == Blocks.AIR;
        boolean z2 = this.content == Blocks.AIR;
        if (z != z2) {
            if (z2) {
                level.setBlock(blockPos, orDefault.defaultBlockState(), 3);
                player.awardStat(Stats.POT_FLOWER);
                if (!player.abilities.instabuild) {
                    itemInHand.shrink(1);
                }
            } else {
                ItemStack itemStack = new ItemStack(this.content);
                if (itemInHand.isEmpty()) {
                    player.setItemInHand(interactionHand, itemStack);
                } else if (!player.addItem(itemStack)) {
                    player.drop(itemStack, false);
                }
                level.setBlock(blockPos, Blocks.FLOWER_POT.defaultBlockState(), 3);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.CONSUME;
    }

    @Override // net.minecraft.world.level.block.Block
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        if (this.content == Blocks.AIR) {
            return super.getCloneItemStack(blockGetter, blockPos, blockState);
        }
        return new ItemStack(this.content);
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    public Block getContent() {
        return this.content;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
