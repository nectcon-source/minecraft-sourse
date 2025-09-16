package net.minecraft.world.item.context;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/context/BlockPlaceContext.class */
public class BlockPlaceContext extends UseOnContext {
    private final BlockPos relativePos;
    protected boolean replaceClicked;

    public BlockPlaceContext(Player player, InteractionHand interactionHand, ItemStack itemStack, BlockHitResult blockHitResult) {
        this(player.level, player, interactionHand, itemStack, blockHitResult);
    }

    public BlockPlaceContext(UseOnContext useOnContext) {
        this(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand(), useOnContext.getItemInHand(), useOnContext.getHitResult());
    }

    protected BlockPlaceContext(Level level, @Nullable Player player, InteractionHand interactionHand, ItemStack itemStack, BlockHitResult blockHitResult) {
        super(level, player, interactionHand, itemStack, blockHitResult);
        this.replaceClicked = true;
        this.relativePos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
        this.replaceClicked = level.getBlockState(blockHitResult.getBlockPos()).canBeReplaced(this);
    }

    /* renamed from: at */
    public static BlockPlaceContext at(BlockPlaceContext blockPlaceContext, BlockPos blockPos, Direction direction) {
        return new BlockPlaceContext(blockPlaceContext.getLevel(), blockPlaceContext.getPlayer(), blockPlaceContext.getHand(), blockPlaceContext.getItemInHand(), new BlockHitResult(new Vec3(blockPos.getX() + 0.5d + (direction.getStepX() * 0.5d), blockPos.getY() + 0.5d + (direction.getStepY() * 0.5d), blockPos.getZ() + 0.5d + (direction.getStepZ() * 0.5d)), direction, blockPos, false));
    }

    @Override // net.minecraft.world.item.context.UseOnContext
    public BlockPos getClickedPos() {
        return this.replaceClicked ? super.getClickedPos() : this.relativePos;
    }

    public boolean canPlace() {
        return this.replaceClicked || getLevel().getBlockState(getClickedPos()).canBeReplaced(this);
    }

    public boolean replacingClickedOnBlock() {
        return this.replaceClicked;
    }

    public Direction getNearestLookingDirection() {
        return Direction.orderedByNearest(getPlayer())[0];
    }

    public Direction[] getNearestLookingDirections() {
        Direction[] orderedByNearest = Direction.orderedByNearest(getPlayer());
        if (this.replaceClicked) {
            return orderedByNearest;
        }
        Direction clickedFace = getClickedFace();
        int i = 0;
        while (i < orderedByNearest.length && orderedByNearest[i] != clickedFace.getOpposite()) {
            i++;
        }
        if (i > 0) {
            System.arraycopy(orderedByNearest, 0, orderedByNearest, 1, i);
            orderedByNearest[0] = clickedFace.getOpposite();
        }
        return orderedByNearest;
    }
}
