package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.TextFilter;
import net.minecraft.world.entity.Entity;

public class EmoteCommands {
   public static void register(CommandDispatcher<CommandSourceStack> var0) {
      var0.register(
         (LiteralArgumentBuilder)Commands.literal("me")
            .then(
               Commands.argument("action", StringArgumentType.greedyString())
                  .executes(
                     var0x -> {
                        String var1 = StringArgumentType.getString(var0x, "action");
                        Entity var2x = ((CommandSourceStack)var0x.getSource()).getEntity();
                        MinecraftServer var3xx_ = ((CommandSourceStack)var0x.getSource()).getServer();
                        if (var2x != null) {
                           if (var2x instanceof ServerPlayer) {
                              TextFilter var4xxx = ((ServerPlayer)var2x).getTextFilter();
                              if (var4xxx != null) {
                                 var4xxx.processStreamMessage(var1)
                                    .thenAcceptAsync(
                                       var3x -> var3x.ifPresent(
                                             var3xx -> var3xx_.getPlayerList().broadcastMessage(createMessage(var0x, var3xx), ChatType.CHAT, var2x.getUUID())
                                          ),
                                       var3xx_
                                    );
                                 return 1;
                              }
                           }
               
                           var3xx_.getPlayerList().broadcastMessage(createMessage(var0x, var1), ChatType.CHAT, var2x.getUUID());
                        } else {
                           var3xx_.getPlayerList().broadcastMessage(createMessage(var0x, var1), ChatType.SYSTEM, Util.NIL_UUID);
                        }
               
                        return 1;
                     }
                  )
            )
      );
   }

   private static Component createMessage(CommandContext<CommandSourceStack> var0, String var1) {
       return new TranslatableComponent("chat.type.emote", new Object[]{((CommandSourceStack)var0.getSource()).getDisplayName(), var1});
   }
}
