package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

public class BlockEntityJukeboxFix extends NamedEntityFix {
   public BlockEntityJukeboxFix(Schema var1, boolean var2) {
      super(var1, var2, "BlockEntityJukeboxFix", References.BLOCK_ENTITY, "minecraft:jukebox");
   }

   @Override
   protected Typed<?> fix(Typed<?> var1) {
      Type<?> var2 = this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:jukebox");
      Type<?> var3x = var2.findFieldType("RecordItem");
      OpticFinder<?> var4xx = DSL.fieldFinder("RecordItem", var3x);
      Dynamic<?> var5xxx = (Dynamic)var1.get(DSL.remainderFinder());
      int var6xxxx = var5xxx.get("Record").asInt(0);
      if (var6xxxx > 0) {
         var5xxx.remove("Record");
         String var7xxxxx = ItemStackTheFlatteningFix.updateItem(ItemIdFix.getItem(var6xxxx), 0);
         if (var7xxxxx != null) {
            Dynamic<?> var8xxxxxx = var5xxx.emptyMap();
            var8xxxxxx = var8xxxxxx.set("id", var8xxxxxx.createString(var7xxxxx));
            var8xxxxxx = var8xxxxxx.set("Count", var8xxxxxx.createByte((byte)1));
            return var1.set(
                    var4xx,
                  (Typed)((Pair)var3x.readTyped(var8xxxxxx).result().orElseThrow(() -> new IllegalStateException("Could not create record item stack."))).getFirst()
               )
               .set(DSL.remainderFinder(), var5xxx);
         }
      }

      return var1;
   }
}
