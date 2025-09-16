package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReloadCommand {
   private static final Logger LOGGER = LogManager.getLogger();

   public static void reloadPacks(Collection<String> var0, CommandSourceStack var1) {
      var1.getServer().reloadResources(var0).exceptionally(var1x -> {
         LOGGER.warn("Failed to execute reload", var1x);
         var1.sendFailure(new TranslatableComponent("commands.reload.failure"));
         return null;
      });
   }

   private static Collection<String> discoverNewPacks(PackRepository var0, WorldData var1, Collection<String> var2) {
      var0.reload();
      Collection<String> var3 = Lists.newArrayList(var2);
      Collection<String> var4 = var1.getDataPackConfig().getDisabled();

      for(String var6 : var0.getAvailableIds()) {
         if (!var4.contains(var6) && !var3.contains(var6)) {
            var3.add(var6);
         }
      }

      return var3;
   }

   public static void register(CommandDispatcher<CommandSourceStack> var0) {
      var0.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("reload").requires(var0x -> var0x.hasPermission(2))).executes(var0x -> {
         CommandSourceStack var1 = (CommandSourceStack)var0x.getSource();
         MinecraftServer var2 = var1.getServer();
         PackRepository var3 = var2.getPackRepository();
         WorldData var4 = var2.getWorldData();
         Collection<String> var5 = var3.getSelectedIds();
         Collection<String> var6 = discoverNewPacks(var3, var4, var5);
         var1.sendSuccess(new TranslatableComponent("commands.reload.success"), true);
         reloadPacks(var6, var1);
         return 0;
      }));
   }
}
