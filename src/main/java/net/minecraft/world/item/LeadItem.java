package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/LeadItem.class */
public class LeadItem extends Item {
    public LeadItem(Item.Properties properties) {
        super(properties);
    }

    @Override // net.minecraft.world.item.Item
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos clickedPos = useOnContext.getClickedPos();
        if (level.getBlockState(clickedPos).getBlock().is(BlockTags.FENCES)) {
            Player player = useOnContext.getPlayer();
            if (!level.isClientSide && player != null) {
                bindPlayerMobs(player, level, clickedPos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult bindPlayerMobs(Player player, Level level, BlockPos blockPos) {
        LeashFenceKnotEntity leashFenceKnotEntity = null;
        boolean z = false;
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z2 = blockPos.getZ();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(x - 7.0d, y - 7.0d, z2 - 7.0d, x + 7.0d, y + 7.0d, z2 + 7.0d))) {
            if (mob.getLeashHolder() == player) {
                if (leashFenceKnotEntity == null) {
                    leashFenceKnotEntity = LeashFenceKnotEntity.getOrCreateKnot(level, blockPos);
                }
                mob.setLeashedTo(leashFenceKnotEntity, true);
                z = true;
            }
        }
        return z ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
