package net.minecraft.data.models.blockstates;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public interface Condition extends Supplier<JsonElement> {
   void validate(StateDefinition<?, ?> var1);

   static Condition.TerminalCondition condition() {
      return new Condition.TerminalCondition();
   }

   static Condition or(Condition... var0) {
      return new Condition.CompositeCondition(Condition.Operation.OR, Arrays.asList(var0));
   }

   public static class CompositeCondition implements Condition {
      private final Condition.Operation operation;
      private final List<Condition> subconditions;

      private CompositeCondition(Operation operation, List<Condition> list) {
         this.operation = operation;
         this.subconditions = list;
      }

      @Override
      public void validate(StateDefinition<?, ?> var1) {
         this.subconditions.forEach(var1x -> var1x.validate(var1));
      }

      public JsonElement get() {
         JsonArray var1 = new JsonArray();
         this.subconditions.stream().map(Supplier::get).forEach(var1::add);
         JsonObject var2x = new JsonObject();
         var2x.add(this.operation.id, var1);
         return var2x;
      }
   }

   public static enum Operation {
      AND("AND"),
      OR("OR");

      private final String id;

      private Operation(String var3) {
         this.id = var3;
      }
   }

   public static class TerminalCondition implements Condition {
      private final Map<Property<?>, String> terms = Maps.newHashMap();

      private static <T extends Comparable<T>> String joinValues(Property<T> var0, Stream<T> var1) {
         return var1.<String>map(var0::getName).collect(Collectors.joining("|"));
      }

      private static <T extends Comparable<T>> String getTerm(Property<T> var0, T var1, T[] var2) {
         return joinValues(var0, Stream.concat(Stream.of(var1), Stream.of(var2)));
      }

      private <T extends Comparable<T>> void putValue(Property<T> property, String str) {
         String put = this.terms.put(property, str);
         if (put != null) {
            throw new IllegalStateException("Tried to replace " + property + " value from " + put + " to " + str);
         }
      }

      public final <T extends Comparable<T>> Condition.TerminalCondition term(Property<T> var1, T var2) {
         this.putValue(var1, var1.getName(var2));
         return this;
      }

      @SafeVarargs
      public final <T extends Comparable<T>> Condition.TerminalCondition term(Property<T> var1, T var2, T... var3) {
         this.putValue(var1, getTerm(var1, var2, var3));
         return this;
      }

      public JsonElement get() {
         JsonObject var1 = new JsonObject();
         this.terms.forEach((var1x, var2) -> var1.addProperty(var1x.getName(), var2));
         return var1;
      }

      @Override
      public void validate(StateDefinition<?, ?> var1) {
         List<Property<?>> var2 = this.terms.keySet().stream().filter((var1x) -> var1.getProperty(var1x.getName()) != var1x).collect(Collectors.toList());
         if (!var2.isEmpty()) {
            throw new IllegalStateException("Properties " + var2 + " are missing from " + var1);
         }
      }
   }
}
