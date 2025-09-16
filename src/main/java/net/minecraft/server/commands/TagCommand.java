package net.minecraft.server.commands;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

public class TagCommand {
   private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.tag.add.failed"));
   private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.tag.remove.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> var0) {
      var0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tag").requires(var0x -> var0x.hasPermission(2)))
            .then(
               ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities())
                        .then(
                           Commands.literal("add")
                              .then(
                                 Commands.argument("name", StringArgumentType.word())
                                    .executes(
                                       var0x -> addTag(
                                             (CommandSourceStack)var0x.getSource(),
                                             EntityArgument.getEntities(var0x, "targets"),
                                             StringArgumentType.getString(var0x, "name")
                                          )
                                    )
                              )
                        ))
                     .then(
                        Commands.literal("remove")
                           .then(
                              Commands.argument("name", StringArgumentType.word())
                                 .suggests((var0x, var1) -> SharedSuggestionProvider.suggest(getTags(EntityArgument.getEntities(var0x, "targets")), var1))
                                 .executes(
                                    var0x -> removeTag(
                                          (CommandSourceStack)var0x.getSource(),
                                          EntityArgument.getEntities(var0x, "targets"),
                                          StringArgumentType.getString(var0x, "name")
                                       )
                                 )
                           )
                     ))
                  .then(
                     Commands.literal("list").executes(var0x -> listTags((CommandSourceStack)var0x.getSource(), EntityArgument.getEntities(var0x, "targets")))
                  )
            )
      );
   }

   private static Collection<String> getTags(Collection<? extends Entity> var0) {
      Set<String> var1 = Sets.newHashSet();

      for(Entity var3 : var0) {
         var1.addAll(var3.getTags());
      }

      return var1;
   }

   private static int addTag(CommandSourceStack var0, Collection<? extends Entity> var1, String var2) throws CommandSyntaxException {
      int var3 = 0;

      for(Entity var5 : var1) {
         if (var5.addTag(var2)) {
            ++var3;
         }
      }

      if (var3 == 0) {
         throw ERROR_ADD_FAILED.create();
      } else {
         if (var1.size() == 1) {
            var0.sendSuccess(new TranslatableComponent("commands.tag.add.success.single", new Object[]{var2, ((Entity)var1.iterator().next()).getDisplayName()}), true);
         } else {
            var0.sendSuccess(new TranslatableComponent("commands.tag.add.success.multiple", new Object[]{var2, var1.size()}), true);
         }

         return var3;
      }
   }

   private static int removeTag(CommandSourceStack var0, Collection<? extends Entity> var1, String var2) throws CommandSyntaxException {
      int var3 = 0;

      for(Entity var5 : var1) {
         if (var5.removeTag(var2)) {
            ++var3;
         }
      }

      if (var3 == 0) {
         throw ERROR_REMOVE_FAILED.create();
      } else {
         if (var1.size() == 1) {
            var0.sendSuccess(new TranslatableComponent("commands.tag.remove.success.single", new Object[]{var2, ((Entity)var1.iterator().next()).getDisplayName()}), true);
         } else {
            var0.sendSuccess(new TranslatableComponent("commands.tag.remove.success.multiple", new Object[]{var2, var1.size()}), true);
         }

         return var3;
      }
   }

   private static int listTags(CommandSourceStack var0, Collection<? extends Entity> var1) {
      Set<String> var2 = Sets.newHashSet();

      for(Entity var4 : var1) {
         var2.addAll(var4.getTags());
      }

      if (var1.size() == 1) {
         Entity var5 = (Entity)var1.iterator().next();
         if (var2.isEmpty()) {
            var0.sendSuccess(new TranslatableComponent("commands.tag.list.single.empty", new Object[]{var5.getDisplayName()}), false);
         } else {
            var0.sendSuccess(new TranslatableComponent("commands.tag.list.single.success", new Object[]{var5.getDisplayName(), var2.size(), ComponentUtils.formatList(var2)}), false);
         }
      } else if (var2.isEmpty()) {
         var0.sendSuccess(new TranslatableComponent("commands.tag.list.multiple.empty", new Object[]{var1.size()}), false);
      } else {
         var0.sendSuccess(new TranslatableComponent("commands.tag.list.multiple.success", new Object[]{var1.size(), var2.size(), ComponentUtils.formatList(var2)}), false);
      }

      return var2.size();
   }
}
