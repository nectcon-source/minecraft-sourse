package net.minecraft.world.item;

import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.server.level.ServerPlayer;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/item/ServerItemCooldowns.class */
public class ServerItemCooldowns extends ItemCooldowns {
    private final ServerPlayer player;

    public ServerItemCooldowns(ServerPlayer serverPlayer) {
        this.player = serverPlayer;
    }

    @Override // net.minecraft.world.item.ItemCooldowns
    protected void onCooldownStarted(Item item, int i) {
        super.onCooldownStarted(item, i);
        this.player.connection.send(new ClientboundCooldownPacket(item, i));
    }

    @Override // net.minecraft.world.item.ItemCooldowns
    protected void onCooldownEnded(Item item) {
        super.onCooldownEnded(item);
        this.player.connection.send(new ClientboundCooldownPacket(item, 0));
    }
}
