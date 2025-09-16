package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/CommandBlockEntity.class */
public class CommandBlockEntity extends BlockEntity {
    private boolean powered;
    private boolean auto;
    private boolean conditionMet;
    private boolean sendToClient;
    private final BaseCommandBlock commandBlock;

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/CommandBlockEntity$Mode.class */
    public enum Mode {
        SEQUENCE,
        AUTO,
        REDSTONE
    }

    public CommandBlockEntity() {
        super(BlockEntityType.COMMAND_BLOCK);
        this.commandBlock = new BaseCommandBlock() { // from class: net.minecraft.world.level.block.entity.CommandBlockEntity.1
            @Override // net.minecraft.world.level.BaseCommandBlock
            public void setCommand(String str) {
                super.setCommand(str);
                CommandBlockEntity.this.setChanged();
            }

            @Override // net.minecraft.world.level.BaseCommandBlock
            public ServerLevel getLevel() {
                return (ServerLevel) CommandBlockEntity.this.level;
            }

            @Override // net.minecraft.world.level.BaseCommandBlock
            public void onUpdated() {
                BlockState blockState = CommandBlockEntity.this.level.getBlockState(CommandBlockEntity.this.worldPosition);
                getLevel().sendBlockUpdated(CommandBlockEntity.this.worldPosition, blockState, blockState, 3);
            }

            @Override // net.minecraft.world.level.BaseCommandBlock
            public Vec3 getPosition() {
                return Vec3.atCenterOf(CommandBlockEntity.this.worldPosition);
            }

            @Override // net.minecraft.world.level.BaseCommandBlock
            public CommandSourceStack createCommandSourceStack() {
                return new CommandSourceStack(this, Vec3.atCenterOf(CommandBlockEntity.this.worldPosition), Vec2.ZERO, getLevel(), 2, getName().getString(), getName(), getLevel().getServer(), null);
            }
        };
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        this.commandBlock.save(compoundTag);
        compoundTag.putBoolean("powered", isPowered());
        compoundTag.putBoolean("conditionMet", wasConditionMet());
        compoundTag.putBoolean("auto", isAutomatic());
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        this.commandBlock.load(compoundTag);
        this.powered = compoundTag.getBoolean("powered");
        this.conditionMet = compoundTag.getBoolean("conditionMet");
        setAutomatic(compoundTag.getBoolean("auto"));
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        if (isSendToClient()) {
            setSendToClient(false);
            return new ClientboundBlockEntityDataPacket(this.worldPosition, 2, save(new CompoundTag()));
        }
        return null;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public BaseCommandBlock getCommandBlock() {
        return this.commandBlock;
    }

    public void setPowered(boolean z) {
        this.powered = z;
    }

    public boolean isPowered() {
        return this.powered;
    }

    public boolean isAutomatic() {
        return this.auto;
    }

    public void setAutomatic(boolean z) {
        boolean z2 = this.auto;
        this.auto = z;
        if (!z2 && z && !this.powered && this.level != null && getMode() != Mode.SEQUENCE) {
            scheduleTick();
        }
    }

    public void onModeSwitch() {
        if (getMode() == Mode.AUTO) {
            if ((this.powered || this.auto) && this.level != null) {
                scheduleTick();
            }
        }
    }

    private void scheduleTick() {
        Block block = getBlockState().getBlock();
        if (block instanceof CommandBlock) {
            markConditionMet();
            this.level.getBlockTicks().scheduleTick(this.worldPosition, block, 1);
        }
    }

    public boolean wasConditionMet() {
        return this.conditionMet;
    }

    public boolean markConditionMet() {
        this.conditionMet = true;
        if (isConditional()) {
            BlockPos relative = this.worldPosition.relative(((Direction) this.level.getBlockState(this.worldPosition).getValue(CommandBlock.FACING)).getOpposite());
            if (this.level.getBlockState(relative).getBlock() instanceof CommandBlock) {
                BlockEntity blockEntity = this.level.getBlockEntity(relative);
                this.conditionMet = (blockEntity instanceof CommandBlockEntity) && ((CommandBlockEntity) blockEntity).getCommandBlock().getSuccessCount() > 0;
            } else {
                this.conditionMet = false;
            }
        }
        return this.conditionMet;
    }

    public boolean isSendToClient() {
        return this.sendToClient;
    }

    public void setSendToClient(boolean z) {
        this.sendToClient = z;
    }

    public Mode getMode() {
        BlockState blockState = getBlockState();
        if (blockState.is(Blocks.COMMAND_BLOCK)) {
            return Mode.REDSTONE;
        }
        if (blockState.is(Blocks.REPEATING_COMMAND_BLOCK)) {
            return Mode.AUTO;
        }
        if (blockState.is(Blocks.CHAIN_COMMAND_BLOCK)) {
            return Mode.SEQUENCE;
        }
        return Mode.REDSTONE;
    }

    public boolean isConditional() {
        BlockState blockState = this.level.getBlockState(getBlockPos());
        if (blockState.getBlock() instanceof CommandBlock) {
            return ((Boolean) blockState.getValue(CommandBlock.CONDITIONAL)).booleanValue();
        }
        return false;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void clearRemoved() {
        clearCache();
        super.clearRemoved();
    }
}
