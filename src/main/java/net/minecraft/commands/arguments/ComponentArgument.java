package net.minecraft.commands.arguments;

import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class ComponentArgument implements ArgumentType<Component> {
   private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "\"\"", "\"{\"text\":\"hello world\"}", "[\"\"]");
   public static final DynamicCommandExceptionType ERROR_INVALID_JSON = new DynamicCommandExceptionType(
      var0 -> new TranslatableComponent("argument.component.invalid", var0)
   );

   private ComponentArgument() {
   }

   public static Component getComponent(CommandContext<CommandSourceStack> var0, String var1) {
      return (Component)var0.getArgument(var1, Component.class);
   }

   public static ComponentArgument textComponent() {
      return new ComponentArgument();
   }

   public Component parse(StringReader var1) throws CommandSyntaxException {
      try {
         Component var2 = Component.Serializer.fromJson(var1);
         if (var2 == null) {
            throw ERROR_INVALID_JSON.createWithContext(var1, "empty");
         } else {
            return var2;
         }
      } catch (JsonParseException var2_1) {
         String var3 = var2_1.getCause() != null ? var2_1.getCause().getMessage() : var2_1.getMessage();
         throw ERROR_INVALID_JSON.createWithContext(var1, var3);
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
