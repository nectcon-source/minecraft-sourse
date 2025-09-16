package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ItemCustomNameToComponentFix extends DataFix {
   public ItemCustomNameToComponentFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   private Dynamic<?> fixTag(Dynamic<?> var1) {
      Optional<? extends Dynamic<?>> var2 = var1.get("display").result();
      if (var2.isPresent()) {
         Dynamic<?> var3x = (Dynamic)var2.get();
         Optional<String> var4xx = var3x.get("Name").asString().result();
         if (var4xx.isPresent()) {
            var3x = var3x.set("Name", var3x.createString(Component.Serializer.toJson(new TextComponent(var4xx.get()))));
         } else {
            Optional<String> var5x = var3x.get("LocName").asString().result();
            if (var5x.isPresent()) {
               var3x = var3x.set("Name", var3x.createString(Component.Serializer.toJson(new TranslatableComponent(var5x.get()))));
               var3x = var3x.remove("LocName");
            }
         }

         return var1.set("display", var3x);
      } else {
         return var1;
      }
   }

   public TypeRewriteRule makeRule() {
      Type<?> var1 = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<?> var2x_ = var1.findField("tag");
      return this.fixTypeEverywhereTyped(
         "ItemCustomNameToComponentFix", var1, var2x -> var2x.updateTyped(var2x_, var1xx -> var1xx.update(DSL.remainderFinder(), this::fixTag))
      );
   }
}
