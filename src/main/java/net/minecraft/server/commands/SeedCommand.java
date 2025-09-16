package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class SeedCommand {
   public static void register(CommandDispatcher<CommandSourceStack> var0, boolean var1) {
      var0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("seed").requires(var1x -> !var1 || var1x.hasPermission(2)))
            .executes(
               var0x -> {
                  long var1x = ((CommandSourceStack)var0x.getSource()).getLevel().getSeed();
                  Component var5x = ComponentUtils.wrapInSquareBrackets(
                     new TextComponent(String.valueOf(var1x))
                        .withStyle(
                           var2 -> var2.withColor(ChatFormatting.GREEN)
                                 .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(var1x)))
                                 .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.copy.click")))
                                 .withInsertion(String.valueOf(var1x))
                        )
                  );
                  ((CommandSourceStack)var0x.getSource()).sendSuccess(new TranslatableComponent("commands.seed.success", var5x), false);
                  return (int)var1x;
               }
            )
      );
   }
}
