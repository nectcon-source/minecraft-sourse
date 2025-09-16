package net.minecraft.server.commands;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.players.BanListEntry;
import net.minecraft.server.players.PlayerList;

public class BanListCommands {
   public static void register(CommandDispatcher<CommandSourceStack> var0) {
      var0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("banlist")
                     .requires(var0x -> var0x.hasPermission(3)))
                  .executes(var0x -> {
                     PlayerList var1 = ((CommandSourceStack)var0x.getSource()).getServer().getPlayerList();
                     return showList(
                        (CommandSourceStack)var0x.getSource(), Lists.newArrayList(Iterables.concat(var1.getBans().getEntries(), var1.getIpBans().getEntries()))
                     );
                  }))
               .then(
                  Commands.literal("ips")
                     .executes(
                        var0x -> showList(
                              (CommandSourceStack)var0x.getSource(),
                              ((CommandSourceStack)var0x.getSource()).getServer().getPlayerList().getIpBans().getEntries()
                           )
                     )
               ))
            .then(
               Commands.literal("players")
                  .executes(
                     var0x -> showList(
                           (CommandSourceStack)var0x.getSource(), ((CommandSourceStack)var0x.getSource()).getServer().getPlayerList().getBans().getEntries()
                        )
                  )
            )
      );
   }

   private static int showList(CommandSourceStack var0, Collection<? extends BanListEntry<?>> var1) {
       if (var1.isEmpty()) {
           var0.sendSuccess(new TranslatableComponent("commands.banlist.none"), false);
       } else {
           var0.sendSuccess(new TranslatableComponent("commands.banlist.list", new Object[]{var1.size()}), false);

           for(BanListEntry<?> var3 : var1) {
               var0.sendSuccess(new TranslatableComponent("commands.banlist.entry", new Object[]{var3.getDisplayName(), var3.getSource(), var3.getReason()}), false);
           }
       }

       return var1.size();
   }
}
