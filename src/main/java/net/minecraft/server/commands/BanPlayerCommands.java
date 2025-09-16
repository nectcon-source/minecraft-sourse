package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;

public class BanPlayerCommands {
   private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(new TranslatableComponent("commands.ban.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> var0) {
      var0.register(
         (Commands.literal("ban").requires(var0x -> var0x.hasPermission(3)))
            .then(
               (Commands.argument("targets", GameProfileArgument.gameProfile())
                     .executes(var0x -> banPlayers(var0x.getSource(), GameProfileArgument.getGameProfiles(var0x, "targets"), null)))
                  .then(
                     Commands.argument("reason", MessageArgument.message())
                        .executes(
                           var0x -> banPlayers(
                                 var0x.getSource(),
                                 GameProfileArgument.getGameProfiles(var0x, "targets"),
                                 MessageArgument.getMessage(var0x, "reason")
                              )
                        )
                  )
            )
      );
   }

   private static int banPlayers(CommandSourceStack var0, Collection<GameProfile> var1, @Nullable Component var2) throws CommandSyntaxException {
      UserBanList var3 = var0.getServer().getPlayerList().getBans();
      int var4 = 0;

      for(GameProfile var6 : var1) {
         if (!var3.isBanned(var6)) {
            UserBanListEntry var7 = new UserBanListEntry(var6, null, var0.getTextName(), null, var2 == null ? null : var2.getString());
            var3.add(var7);
            ++var4;
            var0.sendSuccess(new TranslatableComponent("commands.ban.success", new Object[]{ComponentUtils.getDisplayName(var6), var7.getReason()}), true);
            ServerPlayer var8 = var0.getServer().getPlayerList().getPlayer(var6.getId());
            if (var8 != null) {
               var8.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.banned"));
            }
         }
      }

      if (var4 == 0) {
         throw ERROR_ALREADY_BANNED.create();
      } else {
         return var4;
      }
   }
}
