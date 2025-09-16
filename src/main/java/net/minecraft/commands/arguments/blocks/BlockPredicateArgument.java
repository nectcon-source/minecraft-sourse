package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockPredicateArgument implements ArgumentType<BlockPredicateArgument.Result> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
      var0 -> new TranslatableComponent("arguments.block.tag.unknown", var0)
   );

   public static BlockPredicateArgument blockPredicate() {
      return new BlockPredicateArgument();
   }

   public BlockPredicateArgument.Result parse(StringReader var1) throws CommandSyntaxException {
      BlockStateParser var2 = (new BlockStateParser(var1, true)).parse(true);
      if (var2.getState() != null) {
         BlockPredicate var4 = new BlockPredicate(var2.getState(), var2.getProperties().keySet(), var2.getNbt());
         return (var1x) -> var4;
      } else {
         ResourceLocation var3 = var2.getTag();
         return (var2x) -> {
            Tag<Block> var3x = var2x.getBlocks().getTag(var3);
            if (var3x == null) {
               throw ERROR_UNKNOWN_TAG.create(var3.toString());
            } else {
               return new TagPredicate(var3x, var2.getVagueProperties(), var2.getNbt());
            }
         };
      }
   }


   public static Predicate<BlockInWorld> getBlockPredicate(CommandContext<CommandSourceStack> var0, String var1) throws CommandSyntaxException {
      return ((Result)var0.getArgument(var1, Result.class)).create(((CommandSourceStack)var0.getSource()).getServer().getTags());
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> var1, SuggestionsBuilder var2) {
      StringReader var3 = new StringReader(var2.getInput());
      var3.setCursor(var2.getStart());
      BlockStateParser var4 = new BlockStateParser(var3, true);

      try {
         var4.parse(true);
      } catch (CommandSyntaxException var6) {
      }

      return var4.fillSuggestions(var2, BlockTags.getAllTags());
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   static class BlockPredicate implements Predicate<BlockInWorld> {
      private final BlockState state;
      private final Set<Property<?>> properties;
      @Nullable
      private final CompoundTag nbt;

      public BlockPredicate(BlockState var1, Set<Property<?>> var2, @Nullable CompoundTag var3) {
         this.state = var1;
         this.properties = var2;
         this.nbt = var3;
      }

      public boolean test(BlockInWorld var1) {
         BlockState var2 = var1.getState();
         if (!var2.is(this.state.getBlock())) {
            return false;
         } else {
            for(Property<?> var4 : this.properties) {
               if (var2.getValue(var4) != this.state.getValue(var4)) {
                  return false;
               }
            }

            if (this.nbt == null) {
               return true;
            } else {
               BlockEntity var5 = var1.getEntity();
               return var5 != null && NbtUtils.compareNbt(this.nbt, var5.save(new CompoundTag()), true);
            }
         }
      }
   }

   public interface Result {
      Predicate<BlockInWorld> create(TagContainer var1) throws CommandSyntaxException;
   }

   static class TagPredicate implements Predicate<BlockInWorld> {
      private final Tag<Block> tag;
      @Nullable
      private final CompoundTag nbt;
      private final Map<String, String> vagueProperties;

      private TagPredicate(Tag<Block> var1, Map<String, String> var2, @Nullable CompoundTag var3) {
         this.tag = var1;
         this.vagueProperties = var2;
         this.nbt = var3;
      }

      public boolean test(BlockInWorld var1) {
         BlockState var2 = var1.getState();
         if (!var2.is(this.tag)) {
            return false;
         } else {
            for(Map.Entry<String, String> var4 : this.vagueProperties.entrySet()) {
               Property<?> var5 = var2.getBlock().getStateDefinition().getProperty(var4.getKey());
               if (var5 == null) {
                  return false;
               }

               Comparable<?> var6 = var5.getValue(var4.getValue()).orElse(null);
               if (var6 == null) {
                  return false;
               }

               if (var2.getValue(var5) != var6) {
                  return false;
               }
            }

            if (this.nbt == null) {
               return true;
            } else {
               BlockEntity var7 = var1.getEntity();
               return var7 != null && NbtUtils.compareNbt(this.nbt, var7.save(new CompoundTag()), true);
            }
         }
      }
   }
}
