package net.minecraft.world.entity.vehicle;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/MinecartCommandBlock.class */
public class MinecartCommandBlock extends AbstractMinecart {
    private static final EntityDataAccessor<String> DATA_ID_COMMAND_NAME = SynchedEntityData.defineId(MinecartCommandBlock.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Component> DATA_ID_LAST_OUTPUT = SynchedEntityData.defineId(MinecartCommandBlock.class, EntityDataSerializers.COMPONENT);
    private final BaseCommandBlock commandBlock;
    private int lastActivated;

    public MinecartCommandBlock(EntityType<? extends MinecartCommandBlock> entityType, Level level) {
        super(entityType, level);
        this.commandBlock = new MinecartCommandBase();
    }

    public MinecartCommandBlock(Level level, double d, double d2, double d3) {
        super(EntityType.COMMAND_BLOCK_MINECART, level, d, d2, d3);
        this.commandBlock = new MinecartCommandBase();
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void defineSynchedData() {
        super.defineSynchedData();
        getEntityData().define(DATA_ID_COMMAND_NAME, "");
        getEntityData().define(DATA_ID_LAST_OUTPUT, TextComponent.EMPTY);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.commandBlock.load(compoundTag);
        getEntityData().set(DATA_ID_COMMAND_NAME, getCommandBlock().getCommand());
        getEntityData().set(DATA_ID_LAST_OUTPUT, getCommandBlock().getLastOutput());
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart, net.minecraft.world.entity.Entity
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        this.commandBlock.save(compoundTag);
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.COMMAND_BLOCK;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.COMMAND_BLOCK.defaultBlockState();
    }

    public BaseCommandBlock getCommandBlock() {
        return this.commandBlock;
    }

    @Override // net.minecraft.world.entity.vehicle.AbstractMinecart
    public void activateMinecart(int i, int i2, int i3, boolean z) {
        if (z && this.tickCount - this.lastActivated >= 4) {
            getCommandBlock().performCommand(this.level);
            this.lastActivated = this.tickCount;
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        return this.commandBlock.usedBy(player);
    }

    @Override // net.minecraft.world.entity.Entity
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_ID_LAST_OUTPUT.equals(entityDataAccessor)) {
            try {
                this.commandBlock.setLastOutput((Component) getEntityData().get(DATA_ID_LAST_OUTPUT));
            } catch (Throwable th) {
            }
        } else if (DATA_ID_COMMAND_NAME.equals(entityDataAccessor)) {
            this.commandBlock.setCommand((String) getEntityData().get(DATA_ID_COMMAND_NAME));
        }
    }

    @Override // net.minecraft.world.entity.Entity
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/entity/vehicle/MinecartCommandBlock$MinecartCommandBase.class */
    public class MinecartCommandBase extends BaseCommandBlock {
        public MinecartCommandBase() {
        }

        @Override // net.minecraft.world.level.BaseCommandBlock
        public ServerLevel getLevel() {
            return (ServerLevel) MinecartCommandBlock.this.level;
        }

        @Override // net.minecraft.world.level.BaseCommandBlock
        public void onUpdated() {
            MinecartCommandBlock.this.getEntityData().set(MinecartCommandBlock.DATA_ID_COMMAND_NAME, getCommand());
            MinecartCommandBlock.this.getEntityData().set(MinecartCommandBlock.DATA_ID_LAST_OUTPUT, getLastOutput());
        }

        @Override // net.minecraft.world.level.BaseCommandBlock
        public Vec3 getPosition() {
            return MinecartCommandBlock.this.position();
        }

        public MinecartCommandBlock getMinecart() {
            return MinecartCommandBlock.this;
        }

        @Override // net.minecraft.world.level.BaseCommandBlock
        public CommandSourceStack createCommandSourceStack() {
            return new CommandSourceStack(this, MinecartCommandBlock.this.position(), MinecartCommandBlock.this.getRotationVector(), getLevel(), 2, getName().getString(), MinecartCommandBlock.this.getDisplayName(), getLevel().getServer(), MinecartCommandBlock.this);
        }
    }
}
