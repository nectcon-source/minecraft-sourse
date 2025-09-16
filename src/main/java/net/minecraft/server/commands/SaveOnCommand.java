package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;

public class SaveOnCommand {
   private static final SimpleCommandExceptionType ERROR_ALREADY_ON = new SimpleCommandExceptionType(new TranslatableComponent("commands.save.alreadyOn"));

   public static void register(CommandDispatcher<CommandSourceStack> var0) {
      var0.register(Commands.literal("save-on").requires(var0x -> var0x.hasPermission(4)).executes(var0x -> {
         CommandSourceStack var1 = var0x.getSource();
         boolean var2 = false;

         for(ServerLevel var4 : var1.getServer().getAllLevels()) {
            if (var4 != null && var4.noSave) {
               var4.noSave = false;
               var2 = true;
            }
         }

         if (!var2) {
            throw ERROR_ALREADY_ON.create();
         } else {
            var1.sendSuccess(new TranslatableComponent("commands.save.enabled"), true);
            return 1;
         }
      }));
   }
}
