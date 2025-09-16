package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class GiveCommand {
   public static void register(CommandDispatcher<CommandSourceStack> var0) {
      var0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("give").requires(var0x -> var0x.hasPermission(2)))
            .then(
               Commands.argument("targets", EntityArgument.players())
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item())
                           .executes(
                              var0x -> giveItem(
                                    (CommandSourceStack)var0x.getSource(), ItemArgument.getItem(var0x, "item"), EntityArgument.getPlayers(var0x, "targets"), 1
                                 )
                           ))
                        .then(
                           Commands.argument("count", IntegerArgumentType.integer(1))
                              .executes(
                                 var0x -> giveItem(
                                       (CommandSourceStack)var0x.getSource(),
                                       ItemArgument.getItem(var0x, "item"),
                                       EntityArgument.getPlayers(var0x, "targets"),
                                       IntegerArgumentType.getInteger(var0x, "count")
                                    )
                              )
                        )
                  )
            )
      );
   }

   private static int giveItem(CommandSourceStack var0, ItemInput var1, Collection<ServerPlayer> var2, int var3) throws CommandSyntaxException {
      for(ServerPlayer var5 : var2) {
         int var6 = var3;

         while(var6 > 0) {
            int var7 = Math.min(var1.getItem().getMaxStackSize(), var6);
            var6 -= var7;
            ItemStack var8 = var1.createItemStack(var7, false);
            boolean var9 = var5.inventory.add(var8);
            if (var9 && var8.isEmpty()) {
               var8.setCount(1);
               ItemEntity var11 = var5.drop(var8, false);
               if (var11 != null) {
                  var11.makeFakeItem();
               }

               var5.level.playSound(null, var5.getX(), var5.getY(), var5.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((var5.getRandom().nextFloat() - var5.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
               var5.inventoryMenu.broadcastChanges();
            } else {
               ItemEntity var10 = var5.drop(var8, false);
               if (var10 != null) {
                  var10.setNoPickUpDelay();
                  var10.setOwner(var5.getUUID());
               }
            }
         }
      }

      if (var2.size() == 1) {
         var0.sendSuccess(new TranslatableComponent("commands.give.success.single", new Object[]{var3, var1.createItemStack(var3, false).getDisplayName(), ((ServerPlayer)var2.iterator().next()).getDisplayName()}), true);
      } else {
         var0.sendSuccess(new TranslatableComponent("commands.give.success.single", new Object[]{var3, var1.createItemStack(var3, false).getDisplayName(), var2.size()}), true);
      }

      return var2.size();
   }
}
