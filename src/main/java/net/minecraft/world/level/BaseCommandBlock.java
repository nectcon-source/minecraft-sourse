package net.minecraft.world.level;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/BaseCommandBlock.class */
public abstract class BaseCommandBlock implements CommandSource {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final Component DEFAULT_NAME = new TextComponent("@");
    private int successCount;

    @Nullable
    private Component lastOutput;
    private long lastExecution = -1;
    private boolean updateLastExecution = true;
    private boolean trackOutput = true;
    private String command = "";
    private Component name = DEFAULT_NAME;

    public abstract ServerLevel getLevel();

    public abstract void onUpdated();

    public abstract Vec3 getPosition();

    public abstract CommandSourceStack createCommandSourceStack();

    public int getSuccessCount() {
        return this.successCount;
    }

    public void setSuccessCount(int i) {
        this.successCount = i;
    }

    public Component getLastOutput() {
        return this.lastOutput == null ? TextComponent.EMPTY : this.lastOutput;
    }

    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putString("Command", this.command);
        compoundTag.putInt("SuccessCount", this.successCount);
        compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
        compoundTag.putBoolean("TrackOutput", this.trackOutput);
        if (this.lastOutput != null && this.trackOutput) {
            compoundTag.putString("LastOutput", Component.Serializer.toJson(this.lastOutput));
        }
        compoundTag.putBoolean("UpdateLastExecution", this.updateLastExecution);
        if (this.updateLastExecution && this.lastExecution > 0) {
            compoundTag.putLong("LastExecution", this.lastExecution);
        }
        return compoundTag;
    }

    public void load(CompoundTag compoundTag) {
        this.command = compoundTag.getString("Command");
        this.successCount = compoundTag.getInt("SuccessCount");
        if (compoundTag.contains("CustomName", 8)) {
            setName(Component.Serializer.fromJson(compoundTag.getString("CustomName")));
        }
        if (compoundTag.contains("TrackOutput", 1)) {
            this.trackOutput = compoundTag.getBoolean("TrackOutput");
        }
        if (compoundTag.contains("LastOutput", 8) && this.trackOutput) {
            try {
                this.lastOutput = Component.Serializer.fromJson(compoundTag.getString("LastOutput"));
            } catch (Throwable th) {
                this.lastOutput = new TextComponent(th.getMessage());
            }
        } else {
            this.lastOutput = null;
        }
        if (compoundTag.contains("UpdateLastExecution")) {
            this.updateLastExecution = compoundTag.getBoolean("UpdateLastExecution");
        }
        if (this.updateLastExecution && compoundTag.contains("LastExecution")) {
            this.lastExecution = compoundTag.getLong("LastExecution");
        } else {
            this.lastExecution = -1L;
        }
    }

    public void setCommand(String str) {
        this.command = str;
        this.successCount = 0;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean performCommand(Level level) {
        if (level.isClientSide || level.getGameTime() == this.lastExecution) {
            return false;
        }
        if ("Searge".equalsIgnoreCase(this.command)) {
            this.lastOutput = new TextComponent("#itzlipofutzli");
            this.successCount = 1;
            return true;
        }
        this.successCount = 0;
        MinecraftServer server = getLevel().getServer();
        if (server.isCommandBlockEnabled() && !StringUtil.isNullOrEmpty(this.command)) {
            try {
                this.lastOutput = null;
                server.getCommands().performCommand(createCommandSourceStack().withCallback((commandContext, z, i) -> {
                    if (z) {
                        this.successCount++;
                    }
                }), this.command);
            } catch (Throwable th) {
                CrashReport forThrowable = CrashReport.forThrowable(th, "Executing command block");
                CrashReportCategory addCategory = forThrowable.addCategory("Command to be executed");
                addCategory.setDetail("Command", this::getCommand);
                addCategory.setDetail("Name", () -> {
                    return getName().getString();
                });
                throw new ReportedException(forThrowable);
            }
        }
        if (this.updateLastExecution) {
            this.lastExecution = level.getGameTime();
            return true;
        }
        this.lastExecution = -1L;
        return true;
    }

    public Component getName() {
        return this.name;
    }

    public void setName(@Nullable Component component) {
        if (component != null) {
            this.name = component;
        } else {
            this.name = DEFAULT_NAME;
        }
    }

    @Override // net.minecraft.commands.CommandSource
    public void sendMessage(Component component, UUID uuid) {
        if (this.trackOutput) {
            this.lastOutput = new TextComponent("[" + TIME_FORMAT.format(new Date()) + "] ").append(component);
            onUpdated();
        }
    }

    public void setLastOutput(@Nullable Component component) {
        this.lastOutput = component;
    }

    public void setTrackOutput(boolean z) {
        this.trackOutput = z;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }

    public InteractionResult usedBy(Player player) {
        if (!player.canUseGameMasterBlocks()) {
            return InteractionResult.PASS;
        }
        if (player.getCommandSenderWorld().isClientSide) {
            player.openMinecartCommandBlock(this);
        }
        return InteractionResult.sidedSuccess(player.level.isClientSide);
    }

    @Override // net.minecraft.commands.CommandSource
    public boolean acceptsSuccess() {
        return getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK) && this.trackOutput;
    }

    @Override // net.minecraft.commands.CommandSource
    public boolean acceptsFailure() {
        return this.trackOutput;
    }

    @Override // net.minecraft.commands.CommandSource
    public boolean shouldInformAdmins() {
        return getLevel().getGameRules().getBoolean(GameRules.RULE_COMMANDBLOCKOUTPUT);
    }
}
