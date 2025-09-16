package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/InfestedBlock.class */
public class InfestedBlock extends Block {
    private final Block hostBlock;
    private static final Map<Block, Block> BLOCK_BY_HOST_BLOCK = Maps.newIdentityHashMap();

    public InfestedBlock(Block block, BlockBehaviour.Properties properties) {
        super(properties);
        this.hostBlock = block;
        BLOCK_BY_HOST_BLOCK.put(block, this);
    }

    public Block getHostBlock() {
        return this.hostBlock;
    }

    public static boolean isCompatibleHostBlock(BlockState blockState) {
        return BLOCK_BY_HOST_BLOCK.containsKey(blockState.getBlock());
    }

    private void spawnInfestation(ServerLevel serverLevel, BlockPos blockPos) {
        Silverfish create = EntityType.SILVERFISH.create(serverLevel);
        create.moveTo(blockPos.getX() + 0.5d, blockPos.getY(), blockPos.getZ() + 0.5d, 0.0f, 0.0f);
        serverLevel.addFreshEntity(create);
        create.spawnAnim();
    }

    @Override // net.minecraft.world.level.block.state.BlockBehaviour
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack);
        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) == 0) {
            spawnInfestation(serverLevel, blockPos);
        }
    }

    @Override // net.minecraft.world.level.block.Block
    public void wasExploded(Level level, BlockPos blockPos, Explosion explosion) {
        if (level instanceof ServerLevel) {
            spawnInfestation((ServerLevel) level, blockPos);
        }
    }

    public static BlockState stateByHostBlock(Block block) {
        return BLOCK_BY_HOST_BLOCK.get(block).defaultBlockState();
    }
}
