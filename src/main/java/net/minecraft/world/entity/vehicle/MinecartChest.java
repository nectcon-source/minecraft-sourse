package net.minecraft.world.entity.vehicle;

import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/MinecartChest.class */
public class MinecartChest extends AbstractMinecartContainer {
    public MinecartChest(EntityType<? extends MinecartChest> entityType, Level level) {
        super(entityType, level);
    }

    public MinecartChest(Level level, double d, double d2, double d3) {
        super(EntityType.CHEST_MINECART, d, d2, d3, level);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecartContainer, net.minecraft.world.entity.vehicle.AbstractMinecart
    public void destroy(DamageSource damageSource) {
        super.destroy(damageSource);
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            spawnAtLocation(Blocks.CHEST);
        }
    }

    @Override // net.minecraft.world.Container
    public int getContainerSize() {
        return 27;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.CHEST;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public BlockState getDefaultDisplayBlockState() {
        return (BlockState) Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public int getDefaultDisplayOffset() {
        return 8;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecartContainer
    public AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return ChestMenu.threeRows(i, inventory, this);
    }
}
