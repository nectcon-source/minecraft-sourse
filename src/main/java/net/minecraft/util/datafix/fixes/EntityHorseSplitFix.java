package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityHorseSplitFix extends EntityRenameFix {
   public EntityHorseSplitFix(Schema var1, boolean var2) {
      super("EntityHorseSplitFix",var1, var2);
   }

   @Override
   protected Pair<String, Typed<?>> fix(String var1, Typed<?> var2) {
      Dynamic<?> var3 = (Dynamic)var2.get(DSL.remainderFinder());
      if (Objects.equals("EntityHorse", var1)) {
         int var5xx = var3.get("Type").asInt(0);
         String var4x;
         switch(var5xx) {
            case 0:
            default:
               var4x = "Horse";
               break;
            case 1:
               var4x = "Donkey";
               break;
            case 2:
               var4x = "Mule";
               break;
            case 3:
               var4x = "ZombieHorse";
               break;
            case 4:
               var4x = "SkeletonHorse";
         }

         var3.remove("Type");
         Type<?> var6x = (Type)this.getOutputSchema().findChoiceType(References.ENTITY).types().get(var4x);
         return Pair.of(
                 var4x, (var2.write().flatMap(var6x::readTyped).result().orElseThrow(() -> new IllegalStateException("Could not parse the new horse"))).getFirst()
         );
      } else {
         return Pair.of(var1, var2);
      }
   }
}
