package net.minecraft.world.level.timers;

import java.util.Iterator;
import net.minecraft.commands.CommandFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.world.level.timers.TimerCallback;

/* loaded from: client_deobf_norm.jar:net/minecraft/world/level/timers/FunctionTagCallback.class */
public class FunctionTagCallback implements TimerCallback<MinecraftServer> {
    private final ResourceLocation tagId;

    public FunctionTagCallback(ResourceLocation resourceLocation) {
        this.tagId = resourceLocation;
    }

    @Override // net.minecraft.world.level.timers.TimerCallback
    public void handle(MinecraftServer minecraftServer, TimerQueue<MinecraftServer> timerQueue, long j) {
        ServerFunctionManager functions = minecraftServer.getFunctions();
        Iterator<CommandFunction> it = functions.getTag(this.tagId).getValues().iterator();
        while (it.hasNext()) {
            functions.execute(it.next(), functions.getGameLoopSender());
        }
    }

    /* loaded from: client_deobf_norm.jar:net/minecraft/world/level/timers/FunctionTagCallback$Serializer.class */
    public static class Serializer extends TimerCallback.Serializer<MinecraftServer, FunctionTagCallback> {
        public Serializer() {
            super(new ResourceLocation("function_tag"), FunctionTagCallback.class);
        }

        @Override // net.minecraft.world.level.timers.TimerCallback.Serializer
        public void serialize(CompoundTag compoundTag, FunctionTagCallback functionTagCallback) {
            compoundTag.putString("Name", functionTagCallback.tagId.toString());
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // net.minecraft.world.level.timers.TimerCallback.Serializer
        public FunctionTagCallback deserialize(CompoundTag compoundTag) {
            return new FunctionTagCallback(new ResourceLocation(compoundTag.getString("Name")));
        }
    }
}
