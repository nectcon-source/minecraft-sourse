package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/MinecartItem.class */
public class MinecartItem extends Item {
    private static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() { // from class: net.minecraft.world.item.MinecartItem.1
        private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

        @Override // net.minecraft.core.dispenser.DefaultDispenseItemBehavior
        public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
            double d;
            Direction direction = (Direction) blockSource.getBlockState().getValue(DispenserBlock.FACING);
            Level level = blockSource.getLevel();
            double mo30x = blockSource.x() + (direction.getStepX() * 1.125d);
            double floor = Math.floor(blockSource.y()) + direction.getStepY();
            double mo32z = blockSource.z() + (direction.getStepZ() * 1.125d);
            BlockPos relative = blockSource.getPos().relative(direction);
            BlockState blockState = level.getBlockState(relative);
            RailShape railShape = blockState.getBlock() instanceof BaseRailBlock ? (RailShape) blockState.getValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            if (blockState.is(BlockTags.RAILS)) {
                if (railShape.isAscending()) {
                    d = 0.6d;
                } else {
                    d = 0.1d;
                }
            } else if (blockState.isAir() && level.getBlockState(relative.below()).is(BlockTags.RAILS)) {
                BlockState blockState2 = level.getBlockState(relative.below());
                RailShape railShape2 = blockState2.getBlock() instanceof BaseRailBlock ? (RailShape) blockState2.getValue(((BaseRailBlock) blockState2.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
                if (direction == Direction.DOWN || !railShape2.isAscending()) {
                    d = -0.9d;
                } else {
                    d = -0.4d;
                }
            } else {
                return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
            }
            AbstractMinecart createMinecart = AbstractMinecart.createMinecart(level, mo30x, floor + d, mo32z, ((MinecartItem) itemStack.getItem()).type);
            if (itemStack.hasCustomHoverName()) {
                createMinecart.setCustomName(itemStack.getHoverName());
            }
            level.addFreshEntity(createMinecart);
            itemStack.shrink(1);
            return itemStack;
        }

        @Override // net.minecraft.core.dispenser.DefaultDispenseItemBehavior
        protected void playSound(BlockSource blockSource) {
            blockSource.getLevel().levelEvent(1000, blockSource.getPos(), 0);
        }
    };
    private final AbstractMinecart.Type type;

    public MinecartItem(AbstractMinecart.Type type, Item.Properties properties) {
        super(properties);
        this.type = type;
        DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level var2 = useOnContext.getLevel();
        BlockPos var3 = useOnContext.getClickedPos();
        BlockState var4 = var2.getBlockState(var3);
        if (!var4.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        } else {
            ItemStack var5 = useOnContext.getItemInHand();
            if (!var2.isClientSide) {
                RailShape var6 = var4.getBlock() instanceof BaseRailBlock ? (RailShape)var4.getValue(((BaseRailBlock)var4.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
                double var7 = (double)0.0F;
                if (var6.isAscending()) {
                    var7 = (double)0.5F;
                }

                AbstractMinecart var9 = AbstractMinecart.createMinecart(var2, (double)var3.getX() + (double)0.5F, (double)var3.getY() + (double)0.0625F + var7, (double)var3.getZ() + (double)0.5F, this.type);
                if (var5.hasCustomHoverName()) {
                    var9.setCustomName(var5.getHoverName());
                }

                var2.addFreshEntity(var9);
            }

            var5.shrink(1);
            return InteractionResult.sidedSuccess(var2.isClientSide);
        }
    }
}
