package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class FillCommand {
   private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType(
      (var0, var1) -> new TranslatableComponent("commands.fill.toobig", var0, var1)
   );
   private static final BlockInput HOLLOW_CORE = new BlockInput(Blocks.AIR.defaultBlockState(), Collections.emptySet(), null);
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.fill.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> var0) {
      var0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("fill").requires(var0x -> var0x.hasPermission(2)))
            .then(
               Commands.argument("from", BlockPosArgument.blockPos())
                  .then(
                     Commands.argument("to", BlockPosArgument.blockPos())
                        .then(
                           ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                                "block", BlockStateArgument.block()
                                             )
                                             .executes(
                                                var0x -> fillBlocks(
                                                      (CommandSourceStack)var0x.getSource(),
                                                      new BoundingBox(
                                                         BlockPosArgument.getLoadedBlockPos(var0x, "from"), BlockPosArgument.getLoadedBlockPos(var0x, "to")
                                                      ),
                                                      BlockStateArgument.getBlock(var0x, "block"),
                                                      FillCommand.Mode.REPLACE,
                                                      null
                                                   )
                                             ))
                                          .then(
                                             ((LiteralArgumentBuilder)Commands.literal("replace")
                                                   .executes(
                                                      var0x -> fillBlocks(
                                                            (CommandSourceStack)var0x.getSource(),
                                                            new BoundingBox(
                                                               BlockPosArgument.getLoadedBlockPos(var0x, "from"),
                                                               BlockPosArgument.getLoadedBlockPos(var0x, "to")
                                                            ),
                                                            BlockStateArgument.getBlock(var0x, "block"),
                                                            FillCommand.Mode.REPLACE,
                                                            null
                                                         )
                                                   ))
                                                .then(
                                                   Commands.argument("filter", BlockPredicateArgument.blockPredicate())
                                                      .executes(
                                                         var0x -> fillBlocks(
                                                               (CommandSourceStack)var0x.getSource(),
                                                               new BoundingBox(
                                                                  BlockPosArgument.getLoadedBlockPos(var0x, "from"),
                                                                  BlockPosArgument.getLoadedBlockPos(var0x, "to")
                                                               ),
                                                               BlockStateArgument.getBlock(var0x, "block"),
                                                               FillCommand.Mode.REPLACE,
                                                               BlockPredicateArgument.getBlockPredicate(var0x, "filter")
                                                            )
                                                      )
                                                )
                                          ))
                                       .then(
                                          Commands.literal("keep")
                                             .executes(
                                                var0x -> fillBlocks(
                                                      (CommandSourceStack)var0x.getSource(),
                                                      new BoundingBox(
                                                         BlockPosArgument.getLoadedBlockPos(var0x, "from"), BlockPosArgument.getLoadedBlockPos(var0x, "to")
                                                      ),
                                                      BlockStateArgument.getBlock(var0x, "block"),
                                                      FillCommand.Mode.REPLACE,
                                                      var0xx -> var0xx.getLevel().isEmptyBlock(var0xx.getPos())
                                                   )
                                             )
                                       ))
                                    .then(
                                       Commands.literal("outline")
                                          .executes(
                                             var0x -> fillBlocks(
                                                   (CommandSourceStack)var0x.getSource(),
                                                   new BoundingBox(
                                                      BlockPosArgument.getLoadedBlockPos(var0x, "from"), BlockPosArgument.getLoadedBlockPos(var0x, "to")
                                                   ),
                                                   BlockStateArgument.getBlock(var0x, "block"),
                                                   FillCommand.Mode.OUTLINE,
                                                   null
                                                )
                                          )
                                    ))
                                 .then(
                                    Commands.literal("hollow")
                                       .executes(
                                          var0x -> fillBlocks(
                                                (CommandSourceStack)var0x.getSource(),
                                                new BoundingBox(
                                                   BlockPosArgument.getLoadedBlockPos(var0x, "from"), BlockPosArgument.getLoadedBlockPos(var0x, "to")
                                                ),
                                                BlockStateArgument.getBlock(var0x, "block"),
                                                FillCommand.Mode.HOLLOW,
                                                null
                                             )
                                       )
                                 ))
                              .then(
                                 Commands.literal("destroy")
                                    .executes(
                                       var0x -> fillBlocks(
                                             (CommandSourceStack)var0x.getSource(),
                                             new BoundingBox(BlockPosArgument.getLoadedBlockPos(var0x, "from"), BlockPosArgument.getLoadedBlockPos(var0x, "to")),
                                             BlockStateArgument.getBlock(var0x, "block"),
                                             FillCommand.Mode.DESTROY,
                                             null
                                          )
                                    )
                              )
                        )
                  )
            )
      );
   }

   private static int fillBlocks(CommandSourceStack var0, BoundingBox var1, BlockInput var2, FillCommand.Mode var3, @Nullable Predicate<BlockInWorld> var4) throws CommandSyntaxException {
       int var5 = var1.getXSpan() * var1.getYSpan() * var1.getZSpan();
       if (var5 > 32768) {
           throw ERROR_AREA_TOO_LARGE.create(32768, var5);
       } else {
           List<BlockPos> var6 = Lists.newArrayList();
           ServerLevel var7 = var0.getLevel();
           int var8 = 0;

           for(BlockPos var10 : BlockPos.betweenClosed(var1.x0, var1.y0, var1.z0, var1.x1, var1.y1, var1.z1)) {
               if (var4 == null || var4.test(new BlockInWorld(var7, var10, true))) {
                   BlockInput var11 = var3.filter.filter(var1, var10, var2, var7);
                   if (var11 != null) {
                       BlockEntity var12 = var7.getBlockEntity(var10);
                       Clearable.tryClear(var12);
                       if (var11.place(var7, var10, 2)) {
                           var6.add(var10.immutable());
                           ++var8;
                       }
                   }
               }
           }

           for(BlockPos var14 : var6) {
               Block var15 = var7.getBlockState(var14).getBlock();
               var7.blockUpdated(var14, var15);
           }

           if (var8 == 0) {
               throw ERROR_FAILED.create();
           } else {
               var0.sendSuccess(new TranslatableComponent("commands.fill.success", new Object[]{var8}), true);
               return var8;
           }
      }
   }

   static enum Mode {
      REPLACE((var0, var1, var2, var3) -> var2),
      OUTLINE(
         (var0, var1, var2, var3) -> var1.getX() != var0.x0
                  && var1.getX() != var0.x1
                  && var1.getY() != var0.y0
                  && var1.getY() != var0.y1
                  && var1.getZ() != var0.z0
                  && var1.getZ() != var0.z1
               ? null
               : var2
      ),
      HOLLOW(
         (var0, var1, var2, var3) -> var1.getX() != var0.x0
                  && var1.getX() != var0.x1
                  && var1.getY() != var0.y0
                  && var1.getY() != var0.y1
                  && var1.getZ() != var0.z0
                  && var1.getZ() != var0.z1
               ? FillCommand.HOLLOW_CORE
               : var2
      ),
      DESTROY((var0, var1, var2, var3) -> {
         var3.destroyBlock(var1, true);
         return var2;
      });

      public final SetBlockCommand.Filter filter;

      private Mode(SetBlockCommand.Filter var3) {
         this.filter = var3;
      }
   }
}
