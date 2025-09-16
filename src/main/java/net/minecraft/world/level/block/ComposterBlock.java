package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/ComposterBlock.class */
public class ComposterBlock extends Block implements WorldlyContainerHolder {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_COMPOSTER;
    public static final Object2FloatMap<ItemLike> COMPOSTABLES = new Object2FloatOpenHashMap();
    private static final VoxelShape OUTER_SHAPE = Shapes.block();
    private static final VoxelShape[] SHAPES = (VoxelShape[]) Util.make(new VoxelShape[9], voxelShapeArr -> {
        for (int i = 0; i < 8; i++) {
            voxelShapeArr[i] = Shapes.join(OUTER_SHAPE, Block.box(2.0d, Math.max(2, 1 + (i * 2)), 2.0d, 14.0d, 16.0d, 14.0d), BooleanOp.ONLY_FIRST);
        }
        voxelShapeArr[8] = voxelShapeArr[7];
    });

    public static void bootStrap() {
        COMPOSTABLES.defaultReturnValue(-1.0f);
        add(0.3f, Items.JUNGLE_LEAVES);
        add(0.3f, Items.OAK_LEAVES);
        add(0.3f, Items.SPRUCE_LEAVES);
        add(0.3f, Items.DARK_OAK_LEAVES);
        add(0.3f, Items.ACACIA_LEAVES);
        add(0.3f, Items.BIRCH_LEAVES);
        add(0.3f, Items.OAK_SAPLING);
        add(0.3f, Items.SPRUCE_SAPLING);
        add(0.3f, Items.BIRCH_SAPLING);
        add(0.3f, Items.JUNGLE_SAPLING);
        add(0.3f, Items.ACACIA_SAPLING);
        add(0.3f, Items.DARK_OAK_SAPLING);
        add(0.3f, Items.BEETROOT_SEEDS);
        add(0.3f, Items.DRIED_KELP);
        add(0.3f, Items.GRASS);
        add(0.3f, Items.KELP);
        add(0.3f, Items.MELON_SEEDS);
        add(0.3f, Items.PUMPKIN_SEEDS);
        add(0.3f, Items.SEAGRASS);
        add(0.3f, Items.SWEET_BERRIES);
        add(0.3f, Items.WHEAT_SEEDS);
        add(0.5f, Items.DRIED_KELP_BLOCK);
        add(0.5f, Items.TALL_GRASS);
        add(0.5f, Items.CACTUS);
        add(0.5f, Items.SUGAR_CANE);
        add(0.5f, Items.VINE);
        add(0.5f, Items.NETHER_SPROUTS);
        add(0.5f, Items.WEEPING_VINES);
        add(0.5f, Items.TWISTING_VINES);
        add(0.5f, Items.MELON_SLICE);
        add(0.65f, Items.SEA_PICKLE);
        add(0.65f, Items.LILY_PAD);
        add(0.65f, Items.PUMPKIN);
        add(0.65f, Items.CARVED_PUMPKIN);
        add(0.65f, Items.MELON);
        add(0.65f, Items.APPLE);
        add(0.65f, Items.BEETROOT);
        add(0.65f, Items.CARROT);
        add(0.65f, Items.COCOA_BEANS);
        add(0.65f, Items.POTATO);
        add(0.65f, Items.WHEAT);
        add(0.65f, Items.BROWN_MUSHROOM);
        add(0.65f, Items.RED_MUSHROOM);
        add(0.65f, Items.MUSHROOM_STEM);
        add(0.65f, Items.CRIMSON_FUNGUS);
        add(0.65f, Items.WARPED_FUNGUS);
        add(0.65f, Items.NETHER_WART);
        add(0.65f, Items.CRIMSON_ROOTS);
        add(0.65f, Items.WARPED_ROOTS);
        add(0.65f, Items.SHROOMLIGHT);
        add(0.65f, Items.DANDELION);
        add(0.65f, Items.POPPY);
        add(0.65f, Items.BLUE_ORCHID);
        add(0.65f, Items.ALLIUM);
        add(0.65f, Items.AZURE_BLUET);
        add(0.65f, Items.RED_TULIP);
        add(0.65f, Items.ORANGE_TULIP);
        add(0.65f, Items.WHITE_TULIP);
        add(0.65f, Items.PINK_TULIP);
        add(0.65f, Items.OXEYE_DAISY);
        add(0.65f, Items.CORNFLOWER);
        add(0.65f, Items.LILY_OF_THE_VALLEY);
        add(0.65f, Items.WITHER_ROSE);
        add(0.65f, Items.FERN);
        add(0.65f, Items.SUNFLOWER);
        add(0.65f, Items.LILAC);
        add(0.65f, Items.ROSE_BUSH);
        add(0.65f, Items.PEONY);
        add(0.65f, Items.LARGE_FERN);
        add(0.85f, Items.HAY_BLOCK);
        add(0.85f, Items.BROWN_MUSHROOM_BLOCK);
        add(0.85f, Items.RED_MUSHROOM_BLOCK);
        add(0.85f, Items.NETHER_WART_BLOCK);
        add(0.85f, Items.WARPED_WART_BLOCK);
        add(0.85f, Items.BREAD);
        add(0.85f, Items.BAKED_POTATO);
        add(0.85f, Items.COOKIE);
        add(1.0f, Items.CAKE);
        add(1.0f, Items.PUMPKIN_PIE);
    }

    private static void add(float f, ItemLike itemLike) {
        COMPOSTABLES.put(itemLike.asItem(), f);
    }

    public ComposterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState((BlockState) this.stateDefinition.any().setValue(LEVEL, 0));
    }

    public static void handleFill(Level level, BlockPos blockPos, boolean z) {
        BlockState blockState = level.getBlockState(blockPos);
        level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), z ? SoundEvents.COMPOSTER_FILL_SUCCESS : SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0f, 1.0f, false);
        double max = blockState.getShape(level, blockPos).max(Direction.Axis.Y, 0.5d, 0.5d) + 0.03125d;
        Random random = level.getRandom();
        for (int i = 0; i < 10; i++) {
            level.addParticle(ParticleTypes.COMPOSTER, blockPos.getX() + 0.13124999403953552d + (0.737500011920929d * random.nextFloat()), blockPos.getY() + max + (random.nextFloat() * (1.0d - max)), blockPos.getZ() + 0.13124999403953552d + (0.737500011920929d * random.nextFloat()), random.nextGaussian() * 0.02d, random.nextGaussian() * 0.02d, random.nextGaussian() * 0.02d);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES[((Integer) blockState.getValue(LEVEL)).intValue()];
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return OUTER_SHAPE;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES[0];
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean z) {
        if (((Integer) blockState.getValue(LEVEL)).intValue() == 7) {
            level.getBlockTicks().scheduleTick(blockPos, blockState.getBlock(), 20);
        }
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        int intValue = ((Integer) blockState.getValue(LEVEL)).intValue();
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (intValue < 8 && COMPOSTABLES.containsKey(itemInHand.getItem())) {
            if (intValue < 7 && !level.isClientSide) {
                level.levelEvent(1500, blockPos, blockState != addItem(blockState, level, blockPos, itemInHand) ? 1 : 0);
                if (!player.abilities.instabuild) {
                    itemInHand.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (intValue == 8) {
            extractProduce(blockState, level, blockPos);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static BlockState insertItem(BlockState blockState, ServerLevel serverLevel, ItemStack itemStack, BlockPos blockPos) {
        if (((Integer) blockState.getValue(LEVEL)).intValue() < 7 && COMPOSTABLES.containsKey(itemStack.getItem())) {
            BlockState addItem = addItem(blockState, serverLevel, blockPos, itemStack);
            itemStack.shrink(1);
            return addItem;
        }
        return blockState;
    }

    public static BlockState extractProduce(BlockState blockState, Level level, BlockPos blockPos) {
        if (!level.isClientSide) {
            ItemEntity itemEntity = new ItemEntity(level, blockPos.getX() + (level.random.nextFloat() * 0.7f) + 0.15000000596046448d, blockPos.getY() + (level.random.nextFloat() * 0.7f) + 0.06000000238418579d + 0.6d, blockPos.getZ() + (level.random.nextFloat() * 0.7f) + 0.15000000596046448d, new ItemStack(Items.BONE_MEAL));
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
        BlockState empty = empty(blockState, level, blockPos);
        level.playSound((Player) null, blockPos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
        return empty;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static BlockState empty(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState2 = (BlockState) blockState.setValue(LEVEL, 0);
        levelAccessor.setBlock(blockPos, blockState2, 3);
        return blockState2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static BlockState addItem(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, ItemStack itemStack) {
        int intValue = ((Integer) blockState.getValue(LEVEL)).intValue();
        float f = COMPOSTABLES.getFloat(itemStack.getItem());
        if ((intValue == 0 && f > 0.0f) || levelAccessor.getRandom().nextDouble() < f) {
            int i = intValue + 1;
            BlockState blockState2 = (BlockState) blockState.setValue(LEVEL, Integer.valueOf(i));
            levelAccessor.setBlock(blockPos, blockState2, 3);
            if (i == 7) {
                levelAccessor.getBlockTicks().scheduleTick(blockPos, blockState.getBlock(), 20);
            }
            return blockState2;
        }
        return blockState;
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (((Integer) blockState.getValue(LEVEL)).intValue() == 7) {
            serverLevel.setBlock(blockPos, blockState.cycle(LEVEL), 3);
            serverLevel.playSound((Player) null, blockPos, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 1.0f, 1.0f);
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

    @Override // net.minecraft.world.WorldlyContainerHolder
    public WorldlyContainer getContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        int intValue = ((Integer) blockState.getValue(LEVEL)).intValue();
        if (intValue == 8) {
            return new OutputContainer(blockState, levelAccessor, blockPos, new ItemStack(Items.BONE_MEAL));
        }
        if (intValue < 7) {
            return new InputContainer(blockState, levelAccessor, blockPos);
        }
        return new EmptyContainer();
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/ComposterBlock$EmptyContainer.class */
    static class EmptyContainer extends SimpleContainer implements WorldlyContainer {
        public EmptyContainer() {
            super(0);
        }

        @Override // net.minecraft.world.WorldlyContainer
        public int[] getSlotsForFace(Direction direction) {
            return new int[0];
        }

        @Override // net.minecraft.world.WorldlyContainer
        public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
            return false;
        }

        @Override // net.minecraft.world.WorldlyContainer
        public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
            return false;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/ComposterBlock$OutputContainer.class */
    static class OutputContainer extends SimpleContainer implements WorldlyContainer {
        private final BlockState state;
        private final LevelAccessor level;
        private final BlockPos pos;
        private boolean changed;

        public OutputContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, ItemStack itemStack) {
            super(itemStack);
            this.state = blockState;
            this.level = levelAccessor;
            this.pos = blockPos;
        }

        @Override // net.minecraft.world.Container
        public int getMaxStackSize() {
            return 1;
        }

        @Override // net.minecraft.world.WorldlyContainer
        public int[] getSlotsForFace(Direction direction) {
            return direction == Direction.DOWN ? new int[]{0} : new int[0];
        }

        @Override // net.minecraft.world.WorldlyContainer
        public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
            return false;
        }

        @Override // net.minecraft.world.WorldlyContainer
        public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
            return !this.changed && direction == Direction.DOWN && itemStack.getItem() == Items.BONE_MEAL;
        }

        @Override // net.minecraft.world.SimpleContainer, net.minecraft.world.Container
        public void setChanged() {
            ComposterBlock.empty(this.state, this.level, this.pos);
            this.changed = true;
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/ComposterBlock$InputContainer.class */
    static class InputContainer extends SimpleContainer implements WorldlyContainer {
        private final BlockState state;
        private final LevelAccessor level;
        private final BlockPos pos;
        private boolean changed;

        public InputContainer(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
            super(1);
            this.state = blockState;
            this.level = levelAccessor;
            this.pos = blockPos;
        }

        @Override // net.minecraft.world.Container
        public int getMaxStackSize() {
            return 1;
        }

        @Override // net.minecraft.world.WorldlyContainer
        public int[] getSlotsForFace(Direction direction) {
            return direction == Direction.UP ? new int[]{0} : new int[0];
        }

        @Override // net.minecraft.world.WorldlyContainer
        public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
            return !this.changed && direction == Direction.UP && ComposterBlock.COMPOSTABLES.containsKey(itemStack.getItem());
        }

        @Override // net.minecraft.world.WorldlyContainer
        public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
            return false;
        }

        @Override // net.minecraft.world.SimpleContainer, net.minecraft.world.Container
        public void setChanged() {
            ItemStack item = getItem(0);
            if (!item.isEmpty()) {
                this.changed = true;
                this.level.levelEvent(1500, this.pos, ComposterBlock.addItem(this.state, this.level, this.pos, item) != this.state ? 1 : 0);
                removeItemNoUpdate(0);
            }
        }
    }
}
