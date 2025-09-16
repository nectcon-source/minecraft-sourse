package net.minecraft.world.level.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/block/entity/SignBlockEntity.class */
public class SignBlockEntity extends BlockEntity {
    private final Component[] messages;
    private boolean isEditable;
    private Player playerWhoMayEdit;
    private final FormattedCharSequence[] renderMessages;
    private DyeColor color;

    public SignBlockEntity() {
        super(BlockEntityType.SIGN);
        this.messages = new Component[]{TextComponent.EMPTY, TextComponent.EMPTY, TextComponent.EMPTY, TextComponent.EMPTY};
        this.isEditable = true;
        this.renderMessages = new FormattedCharSequence[4];
        this.color = DyeColor.BLACK;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        for (int i = 0; i < 4; i++) {
            compoundTag.putString("Text" + (i + 1), Component.Serializer.toJson(this.messages[i]));
        }
        compoundTag.putString("Color", this.color.getName());
        return compoundTag;
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public void load(BlockState blockState, CompoundTag compoundTag) {
        this.isEditable = false;
        super.load(blockState, compoundTag);
        this.color = DyeColor.byName(compoundTag.getString("Color"), DyeColor.BLACK);
        for (int i = 0; i < 4; i++) {
            String string = compoundTag.getString("Text" + (i + 1));
            Component fromJson = Component.Serializer.fromJson(string.isEmpty() ? "\"\"" : string);
            if (this.level instanceof ServerLevel) {
                try {
                    this.messages[i] = ComponentUtils.updateForEntity(createCommandSourceStack(null), fromJson, null, 0);
                } catch (CommandSyntaxException e) {
                    this.messages[i] = fromJson;
                }
            } else {
                this.messages[i] = fromJson;
            }
            this.renderMessages[i] = null;
        }
    }

    public Component getMessage(int i) {
        return this.messages[i];
    }

    public void setMessage(int i, Component component) {
        this.messages[i] = component;
        this.renderMessages[i] = null;
    }

    @Nullable
    public FormattedCharSequence getRenderMessage(int i, Function<Component, FormattedCharSequence> function) {
        if (this.renderMessages[i] == null && this.messages[i] != null) {
            this.renderMessages[i] = function.apply(this.messages[i]);
        }
        return this.renderMessages[i];
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 9, getUpdateTag());
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    @Override // net.minecraft.world.level.block.entity.BlockEntity
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean z) {
        this.isEditable = z;
        if (!z) {
            this.playerWhoMayEdit = null;
        }
    }

    public void setAllowedPlayerEditor(Player player) {
        this.playerWhoMayEdit = player;
    }

    public Player getPlayerWhoMayEdit() {
        return this.playerWhoMayEdit;
    }

    public boolean executeClickCommands(Player player) {
        Component[] componentArr = this.messages;
        int length = componentArr.length;
        for (int i = 0; i < length; i++) {
            Component component = componentArr[i];
            Style style = component == null ? null : component.getStyle();
            if (style != null && style.getClickEvent() != null) {
                ClickEvent clickEvent = style.getClickEvent();
                if (clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    player.getServer().getCommands().performCommand(createCommandSourceStack((ServerPlayer) player), clickEvent.getValue());
                }
            }
        }
        return true;
    }

    public CommandSourceStack createCommandSourceStack(@Nullable ServerPlayer serverPlayer) {
        return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(this.worldPosition), Vec2.ZERO, (ServerLevel) this.level, 2, serverPlayer == null ? "Sign" : serverPlayer.getName().getString(), serverPlayer == null ? new TextComponent("Sign") : serverPlayer.getDisplayName(), this.level.getServer(), serverPlayer);
    }

    public DyeColor getColor() {
        return this.color;
    }

    public boolean setColor(DyeColor dyeColor) {
        if (dyeColor != getColor()) {
            this.color = dyeColor;
            setChanged();
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            return true;
        }
        return false;
    }
}
