package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/EndCrystalItem.class */
public class EndCrystalItem extends Item {
    public EndCrystalItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos clickedPos = useOnContext.getClickedPos();
        BlockState blockState = level.getBlockState(clickedPos);
        if (!blockState.is(Blocks.OBSIDIAN) && !blockState.is(Blocks.BEDROCK)) {
            return InteractionResult.FAIL;
        }
        BlockPos above = clickedPos.above();
        if (!level.isEmptyBlock(above)) {
            return InteractionResult.FAIL;
        }
        double x = above.getX();
        double y = above.getY();
        double z = above.getZ();
        if (!level.getEntities(null, new AABB(x, y, z, x + 1.0d, y + 2.0d, z + 1.0d)).isEmpty()) {
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel) {
            EndCrystal endCrystal = new EndCrystal(level, x + 0.5d, y, z + 0.5d);
            endCrystal.setShowBottom(false);
            level.addFreshEntity(endCrystal);
            EndDragonFight dragonFight = ((ServerLevel) level).dragonFight();
            if (dragonFight != null) {
                dragonFight.tryRespawn();
            }
        }
        useOnContext.getItemInHand().shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override // net.minecraft.world.item.Item
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }
}
