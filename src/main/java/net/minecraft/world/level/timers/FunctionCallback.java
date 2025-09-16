package net.minecraft.world.level.timers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.world.level.timers.TimerCallback;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/timers/FunctionCallback.class */
public class FunctionCallback implements TimerCallback<MinecraftServer> {
    private final ResourceLocation functionId;

    public FunctionCallback(ResourceLocation resourceLocation) {
        this.functionId = resourceLocation;
    }

    @Override // net.minecraft.world.level.timers.TimerCallback
    public void handle(MinecraftServer minecraftServer, TimerQueue<MinecraftServer> timerQueue, long j) {
        ServerFunctionManager functions = minecraftServer.getFunctions();
        functions.get(this.functionId).ifPresent(commandFunction -> {
            functions.execute(commandFunction, functions.getGameLoopSender());
        });
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/timers/FunctionCallback$Serializer.class */
    public static class Serializer extends TimerCallback.Serializer<MinecraftServer, FunctionCallback> {
        public Serializer() {
            super(new ResourceLocation("function"), FunctionCallback.class);
        }

        @Override // net.minecraft.world.level.timers.TimerCallback.Serializer
        public void serialize(CompoundTag compoundTag, FunctionCallback functionCallback) {
            compoundTag.putString("Name", functionCallback.functionId.toString());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.timers.TimerCallback.Serializer
        public FunctionCallback deserialize(CompoundTag compoundTag) {
            return new FunctionCallback(new ResourceLocation(compoundTag.getString("Name")));
        }
    }
}
