package net.minecraft.commands.arguments.selector.options;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.WrappedMinMaxBounds;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class EntitySelectorOptions {
   private static final Map<String, EntitySelectorOptions.Option> OPTIONS = Maps.newHashMap();
   public static final DynamicCommandExceptionType ERROR_UNKNOWN_OPTION = new DynamicCommandExceptionType(
      var0 -> new TranslatableComponent("argument.entity.options.unknown", var0)
   );
   public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION = new DynamicCommandExceptionType(
      var0 -> new TranslatableComponent("argument.entity.options.inapplicable", var0)
   );
   public static final SimpleCommandExceptionType ERROR_RANGE_NEGATIVE = new SimpleCommandExceptionType(
      new TranslatableComponent("argument.entity.options.distance.negative")
   );
   public static final SimpleCommandExceptionType ERROR_LEVEL_NEGATIVE = new SimpleCommandExceptionType(
      new TranslatableComponent("argument.entity.options.level.negative")
   );
   public static final SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL = new SimpleCommandExceptionType(
      new TranslatableComponent("argument.entity.options.limit.toosmall")
   );
   public static final DynamicCommandExceptionType ERROR_SORT_UNKNOWN = new DynamicCommandExceptionType(
      var0 -> new TranslatableComponent("argument.entity.options.sort.irreversible", var0)
   );
   public static final DynamicCommandExceptionType ERROR_GAME_MODE_INVALID = new DynamicCommandExceptionType(
      var0 -> new TranslatableComponent("argument.entity.options.mode.invalid", var0)
   );
   public static final DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID = new DynamicCommandExceptionType(
      var0 -> new TranslatableComponent("argument.entity.options.type.invalid", var0)
   );

   private static void register(String var0, EntitySelectorOptions.Modifier var1, Predicate<EntitySelectorParser> var2, Component var3) {
      OPTIONS.put(var0, new Option(var1, var2, var3));
   }

   public static void bootStrap() {
      if (OPTIONS.isEmpty()) {
         register("name", (var0) -> {
            int var1 = var0.getReader().getCursor();
            boolean var2 = var0.shouldInvertValue();
            String var3 = var0.getReader().readString();
            if (var0.hasNameNotEquals() && !var2) {
               var0.getReader().setCursor(var1);
               throw ERROR_INAPPLICABLE_OPTION.createWithContext(var0.getReader(), "name");
            } else {
               if (var2) {
                  var0.setHasNameNotEquals(true);
               } else {
                  var0.setHasNameEquals(true);
               }

               var0.addPredicate((var2x) -> var2x.getName().getString().equals(var3) != var2);
            }
         }, (var0) -> !var0.hasNameEquals(), new TranslatableComponent("argument.entity.options.name.description"));
         register("distance", (var0) -> {
            int var1 = var0.getReader().getCursor();
            MinMaxBounds.Floats var2 = MinMaxBounds.Floats.fromReader(var0.getReader());
            if ((var2.getMin() == null || !((Float)var2.getMin() < 0.0F)) && (var2.getMax() == null || !((Float)var2.getMax() < 0.0F))) {
               var0.setDistance(var2);
               var0.setWorldLimited();
            } else {
               var0.getReader().setCursor(var1);
               throw ERROR_RANGE_NEGATIVE.createWithContext(var0.getReader());
            }
         }, (var0) -> var0.getDistance().isAny(), new TranslatableComponent("argument.entity.options.distance.description"));
         register("level", (var0) -> {
            int var1 = var0.getReader().getCursor();
            MinMaxBounds.Ints var2 = MinMaxBounds.Ints.fromReader(var0.getReader());
            if ((var2.getMin() == null || (Integer)var2.getMin() >= 0) && (var2.getMax() == null || (Integer)var2.getMax() >= 0)) {
               var0.setLevel(var2);
               var0.setIncludesEntities(false);
            } else {
               var0.getReader().setCursor(var1);
               throw ERROR_LEVEL_NEGATIVE.createWithContext(var0.getReader());
            }
         }, (var0) -> var0.getLevel().isAny(), new TranslatableComponent("argument.entity.options.level.description"));
         register("x", (var0) -> {
            var0.setWorldLimited();
            var0.setX(var0.getReader().readDouble());
         }, (var0) -> var0.getX() == null, new TranslatableComponent("argument.entity.options.x.description"));
         register("y", (var0) -> {
            var0.setWorldLimited();
            var0.setY(var0.getReader().readDouble());
         }, (var0) -> var0.getY() == null, new TranslatableComponent("argument.entity.options.y.description"));
         register("z", (var0) -> {
            var0.setWorldLimited();
            var0.setZ(var0.getReader().readDouble());
         }, (var0) -> var0.getZ() == null, new TranslatableComponent("argument.entity.options.z.description"));
         register("dx", (var0) -> {
            var0.setWorldLimited();
            var0.setDeltaX(var0.getReader().readDouble());
         }, (var0) -> var0.getDeltaX() == null, new TranslatableComponent("argument.entity.options.dx.description"));
         register("dy", (var0) -> {
            var0.setWorldLimited();
            var0.setDeltaY(var0.getReader().readDouble());
         }, (var0) -> var0.getDeltaY() == null, new TranslatableComponent("argument.entity.options.dy.description"));
         register("dz", (var0) -> {
            var0.setWorldLimited();
            var0.setDeltaZ(var0.getReader().readDouble());
         }, (var0) -> var0.getDeltaZ() == null, new TranslatableComponent("argument.entity.options.dz.description"));
         register("x_rotation", (var0) -> var0.setRotX(WrappedMinMaxBounds.fromReader(var0.getReader(), true, Mth::wrapDegrees)), (var0) -> var0.getRotX() == WrappedMinMaxBounds.ANY, new TranslatableComponent("argument.entity.options.x_rotation.description"));
         register("y_rotation", (var0) -> var0.setRotY(WrappedMinMaxBounds.fromReader(var0.getReader(), true, Mth::wrapDegrees)), (var0) -> var0.getRotY() == WrappedMinMaxBounds.ANY, new TranslatableComponent("argument.entity.options.y_rotation.description"));
         register("limit", (var0) -> {
            int var1 = var0.getReader().getCursor();
            int var2 = var0.getReader().readInt();
            if (var2 < 1) {
               var0.getReader().setCursor(var1);
               throw ERROR_LIMIT_TOO_SMALL.createWithContext(var0.getReader());
            } else {
               var0.setMaxResults(var2);
               var0.setLimited(true);
            }
         }, (var0) -> !var0.isCurrentEntity() && !var0.isLimited(), new TranslatableComponent("argument.entity.options.limit.description"));
         register("sort", (var0) -> {
            int var1 = var0.getReader().getCursor();
            String var2 = var0.getReader().readUnquotedString();
            var0.setSuggestions((var0x, var1x) -> SharedSuggestionProvider.suggest(Arrays.asList("nearest", "furthest", "random", "arbitrary"), var0x));
            BiConsumer<Vec3, List<? extends Entity>> var3;
            switch (var2) {
               case "nearest":
                  var3 = EntitySelectorParser.ORDER_NEAREST;
                  break;
               case "furthest":
                  var3 = EntitySelectorParser.ORDER_FURTHEST;
                  break;
               case "random":
                  var3 = EntitySelectorParser.ORDER_RANDOM;
                  break;
               case "arbitrary":
                  var3 = EntitySelectorParser.ORDER_ARBITRARY;
                  break;
               default:
                  var0.getReader().setCursor(var1);
                  throw ERROR_SORT_UNKNOWN.createWithContext(var0.getReader(), var2);
            }

            var0.setOrder(var3);
            var0.setSorted(true);
         }, (var0) -> !var0.isCurrentEntity() && !var0.isSorted(), new TranslatableComponent("argument.entity.options.sort.description"));
         register("gamemode", (var0) -> {
            var0.setSuggestions((var1x, var2x) -> {
               String var3 = var1x.getRemaining().toLowerCase(Locale.ROOT);
               boolean var4 = !var0.hasGamemodeNotEquals();
               boolean var5 = true;
               if (!var3.isEmpty()) {
                  if (var3.charAt(0) == '!') {
                     var4 = false;
                     var3 = var3.substring(1);
                  } else {
                     var5 = false;
                  }
               }

               for(GameType var9 : GameType.values()) {
                  if (var9 != GameType.NOT_SET && var9.getName().toLowerCase(Locale.ROOT).startsWith(var3)) {
                     if (var5) {
                        var1x.suggest('!' + var9.getName());
                     }

                     if (var4) {
                        var1x.suggest(var9.getName());
                     }
                  }
               }

               return var1x.buildFuture();
            });
            int var1 = var0.getReader().getCursor();
            boolean var2 = var0.shouldInvertValue();
            if (var0.hasGamemodeNotEquals() && !var2) {
               var0.getReader().setCursor(var1);
               throw ERROR_INAPPLICABLE_OPTION.createWithContext(var0.getReader(), "gamemode");
            } else {
               String var3 = var0.getReader().readUnquotedString();
               GameType var4 = GameType.byName(var3, GameType.NOT_SET);
               if (var4 == GameType.NOT_SET) {
                  var0.getReader().setCursor(var1);
                  throw ERROR_GAME_MODE_INVALID.createWithContext(var0.getReader(), var3);
               } else {
                  var0.setIncludesEntities(false);
                  var0.addPredicate((var2x) -> {
                     if (!(var2x instanceof ServerPlayer)) {
                        return false;
                     } else {
                        GameType var3_ = ((ServerPlayer)var2x).gameMode.getGameModeForPlayer();
                        return var2 ? var3_ != var4 : var3_ == var4;
                     }
                  });
                  if (var2) {
                     var0.setHasGamemodeNotEquals(true);
                  } else {
                     var0.setHasGamemodeEquals(true);
                  }

               }
            }
         }, (var0) -> !var0.hasGamemodeEquals(), new TranslatableComponent("argument.entity.options.gamemode.description"));
         register("team", (var0) -> {
            boolean var1 = var0.shouldInvertValue();
            String var2 = var0.getReader().readUnquotedString();
            var0.addPredicate((var2x) -> {
               if (!(var2x instanceof LivingEntity)) {
                  return false;
               } else {
                  Team var3 = var2x.getTeam();
                  String var4 = var3 == null ? "" : var3.getName();
                  return var4.equals(var2) != var1;
               }
            });
            if (var1) {
               var0.setHasTeamNotEquals(true);
            } else {
               var0.setHasTeamEquals(true);
            }

         }, (var0) -> !var0.hasTeamEquals(), new TranslatableComponent("argument.entity.options.team.description"));
         register("type", (var0) -> {
            var0.setSuggestions((var1x, var2x) -> {
               SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.keySet(), var1x, String.valueOf('!'));
               SharedSuggestionProvider.suggestResource(EntityTypeTags.getAllTags().getAvailableTags(), var1x, "!#");
               if (!var0.isTypeLimitedInversely()) {
                  SharedSuggestionProvider.suggestResource(Registry.ENTITY_TYPE.keySet(), var1x);
                  SharedSuggestionProvider.suggestResource(EntityTypeTags.getAllTags().getAvailableTags(), var1x, String.valueOf('#'));
               }

               return var1x.buildFuture();
            });
            int var1 = var0.getReader().getCursor();
            boolean var2 = var0.shouldInvertValue();
            if (var0.isTypeLimitedInversely() && !var2) {
               var0.getReader().setCursor(var1);
               throw ERROR_INAPPLICABLE_OPTION.createWithContext(var0.getReader(), "type");
            } else {
               if (var2) {
                  var0.setTypeLimitedInversely();
               }

               if (var0.isTag()) {
                  ResourceLocation var3 = ResourceLocation.read(var0.getReader());
                  var0.addPredicate((var2x) -> var2x.getServer().getTags().getEntityTypes().getTagOrEmpty(var3).contains(var2x.getType()) != var2);
               } else {
                  ResourceLocation var5 = ResourceLocation.read(var0.getReader());
                  EntityType<?> var4 = (EntityType)Registry.ENTITY_TYPE.getOptional(var5).orElseThrow(() -> {
                     var0.getReader().setCursor(var1);
                     return ERROR_ENTITY_TYPE_INVALID.createWithContext(var0.getReader(), var5.toString());
                  });
                  if (Objects.equals(EntityType.PLAYER, var4) && !var2) {
                     var0.setIncludesEntities(false);
                  }

                  var0.addPredicate((var2x) -> Objects.equals(var4, var2x.getType()) != var2);
                  if (!var2) {
                     var0.limitToType(var4);
                  }
               }

            }
         }, (var0) -> !var0.isTypeLimited(), new TranslatableComponent("argument.entity.options.type.description"));
         register("tag", (var0) -> {
            boolean var1 = var0.shouldInvertValue();
            String var2 = var0.getReader().readUnquotedString();
            var0.addPredicate((var2x) -> {
               if ("".equals(var2)) {
                  return var2x.getTags().isEmpty() != var1;
               } else {
                  return var2x.getTags().contains(var2) != var1;
               }
            });
         }, (var0) -> true, new TranslatableComponent("argument.entity.options.tag.description"));
         register("nbt", (var0) -> {
            boolean var1 = var0.shouldInvertValue();
            CompoundTag var2 = (new TagParser(var0.getReader())).readStruct();
            var0.addPredicate((var2x) -> {
               CompoundTag var3 = var2x.saveWithoutId(new CompoundTag());
               if (var2x instanceof ServerPlayer) {
                  ItemStack var4 = ((ServerPlayer)var2x).inventory.getSelected();
                  if (!var4.isEmpty()) {
                     var3.put("SelectedItem", var4.save(new CompoundTag()));
                  }
               }

               return NbtUtils.compareNbt(var2, var3, true) != var1;
            });
         }, (var0) -> true, new TranslatableComponent("argument.entity.options.nbt.description"));
         register("scores", (var0) -> {
            StringReader var1 = var0.getReader();
            Map<String, MinMaxBounds.Ints> var2 = Maps.newHashMap();
            var1.expect('{');
            var1.skipWhitespace();

            while(var1.canRead() && var1.peek() != '}') {
               var1.skipWhitespace();
               String var3 = var1.readUnquotedString();
               var1.skipWhitespace();
               var1.expect('=');
               var1.skipWhitespace();
               MinMaxBounds.Ints var4 = MinMaxBounds.Ints.fromReader(var1);
               var2.put(var3, var4);
               var1.skipWhitespace();
               if (var1.canRead() && var1.peek() == ',') {
                  var1.skip();
               }
            }

            var1.expect('}');
            if (!var2.isEmpty()) {
               var0.addPredicate((var1x) -> {
                  Scoreboard var2x = var1x.getServer().getScoreboard();
                  String var3 = var1x.getScoreboardName();

                  for(Map.Entry<String, MinMaxBounds.Ints> var5 : var2.entrySet()) {
                     Objective var6 = var2x.getObjective((String)var5.getKey());
                     if (var6 == null) {
                        return false;
                     }

                     if (!var2x.hasPlayerScore(var3, var6)) {
                        return false;
                     }

                     Score var7 = var2x.getOrCreatePlayerScore(var3, var6);
                     int var8 = var7.getScore();
                     if (!((MinMaxBounds.Ints)var5.getValue()).matches(var8)) {
                        return false;
                     }
                  }

                  return true;
               });
            }

            var0.setHasScores(true);
         }, (var0) -> !var0.hasScores(), new TranslatableComponent("argument.entity.options.scores.description"));
         register("advancements", (var0) -> {
            StringReader var1 = var0.getReader();
            Map<ResourceLocation, Predicate<AdvancementProgress>> var2 = Maps.newHashMap();
            var1.expect('{');
            var1.skipWhitespace();

            while(var1.canRead() && var1.peek() != '}') {
               var1.skipWhitespace();
               ResourceLocation var3 = ResourceLocation.read(var1);
               var1.skipWhitespace();
               var1.expect('=');
               var1.skipWhitespace();
               if (var1.canRead() && var1.peek() == '{') {
                  Map<String, Predicate<CriterionProgress>> var7 = Maps.newHashMap();
                  var1.skipWhitespace();
                  var1.expect('{');
                  var1.skipWhitespace();

                  while(var1.canRead() && var1.peek() != '}') {
                     var1.skipWhitespace();
                     String var5 = var1.readUnquotedString();
                     var1.skipWhitespace();
                     var1.expect('=');
                     var1.skipWhitespace();
                     boolean var6 = var1.readBoolean();
                     var7.put(var5, (var1x_) -> var1x_.isDone() == var6);
                     var1.skipWhitespace();
                     if (var1.canRead() && var1.peek() == ',') {
                        var1.skip();
                     }
                  }

                  var1.skipWhitespace();
                  var1.expect('}');
                  var1.skipWhitespace();
                  var2.put(var3, (var1x) -> {
                     for(Map.Entry<String, Predicate<CriterionProgress>> var3__ : var7.entrySet()) {
                        CriterionProgress var4 = var1x.getCriterion(var3__.getKey());
                        if (var4 == null || !(var3__.getValue()).test(var4)) {
                           return false;
                        }
                     }

                     return true;
                  });
               } else {
                  boolean var4 = var1.readBoolean();
                  var2.put(var3, (var1x) -> var1x.isDone() == var4);
               }

               var1.skipWhitespace();
               if (var1.canRead() && var1.peek() == ',') {
                  var1.skip();
               }
            }

            var1.expect('}');
            if (!var2.isEmpty()) {
               var0.addPredicate((var1x) -> {
                  if (!(var1x instanceof ServerPlayer)) {
                     return false;
                  } else {
                     ServerPlayer var2x = (ServerPlayer)var1x;
                     PlayerAdvancements var3 = var2x.getAdvancements();
                     ServerAdvancementManager var4 = var2x.getServer().getAdvancements();

                     for(Map.Entry<ResourceLocation, Predicate<AdvancementProgress>> var6 : var2.entrySet()) {
                        Advancement var7 = var4.getAdvancement(var6.getKey());
                        if (var7 == null || !(var6.getValue()).test(var3.getOrStartProgress(var7))) {
                           return false;
                        }
                     }

                     return true;
                  }
               });
               var0.setIncludesEntities(false);
            }

            var0.setHasAdvancements(true);
         }, (var0) -> !var0.hasAdvancements(), new TranslatableComponent("argument.entity.options.advancements.description"));
         register("predicate", (var0) -> {
            boolean var1 = var0.shouldInvertValue();
            ResourceLocation var2 = ResourceLocation.read(var0.getReader());
            var0.addPredicate((var2x) -> {
               if (!(var2x.level instanceof ServerLevel)) {
                  return false;
               } else {
                  ServerLevel var3 = (ServerLevel)var2x.level;
                  LootItemCondition var4 = var3.getServer().getPredicateManager().get(var2);
                  if (var4 == null) {
                     return false;
                  } else {
                     LootContext var5 = (new LootContext.Builder(var3)).withParameter(LootContextParams.THIS_ENTITY, var2x).withParameter(LootContextParams.ORIGIN, var2x.position()).create(LootContextParamSets.SELECTOR);
                     return var1 ^ var4.test(var5);
                  }
               }
            });
         }, (var0) -> true, new TranslatableComponent("argument.entity.options.predicate.description"));
      }
   }

   public static EntitySelectorOptions.Modifier get(EntitySelectorParser var0, String var1, int var2) throws CommandSyntaxException {
      Option var3 = (Option)OPTIONS.get(var1);
      if (var3 != null) {
         if (var3.predicate.test(var0)) {
            return var3.modifier;
         } else {
            throw ERROR_INAPPLICABLE_OPTION.createWithContext(var0.getReader(), var1);
         }
      } else {
         var0.getReader().setCursor(var2);
         throw ERROR_UNKNOWN_OPTION.createWithContext(var0.getReader(), var1);
      }
   }

   public static void suggestNames(EntitySelectorParser var0, SuggestionsBuilder var1) {
      String var2 = var1.getRemaining().toLowerCase(Locale.ROOT);

      for(Map.Entry<String, Option> var4 : OPTIONS.entrySet()) {
         if (((Option)var4.getValue()).predicate.test(var0) && ((String)var4.getKey()).toLowerCase(Locale.ROOT).startsWith(var2)) {
            var1.suggest((String)var4.getKey() + '=', ((Option)var4.getValue()).description);
         }
      }
   }

   public interface Modifier {
      void handle(EntitySelectorParser var1) throws CommandSyntaxException;
   }

   static class Option {
      public final EntitySelectorOptions.Modifier modifier;
      public final Predicate<EntitySelectorParser> predicate;
      public final Component description;

      private Option(EntitySelectorOptions.Modifier var1, Predicate<EntitySelectorParser> var2, Component var3) {
         this.modifier = var1;
         this.predicate = var2;
         this.description = var3;
      }
   }
}
