package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

public class BlockEntityBlockStateFix extends NamedEntityFix {
   public BlockEntityBlockStateFix(Schema var1, boolean var2) {
      super(var1, var2, "BlockEntityBlockStateFix", References.BLOCK_ENTITY, "minecraft:piston");
   }

   @Override
   protected Typed<?> fix(Typed<?> var1) {
      Type<?> var2 = this.getOutputSchema().getChoiceType(References.BLOCK_ENTITY, "minecraft:piston");
      Type<?> var3x = var2.findFieldType("blockState");
      OpticFinder<?> var4xx = DSL.fieldFinder("blockState", var3x);
      Dynamic<?> var5xxx = (Dynamic)var1.get(DSL.remainderFinder());
      int var6xxxx = var5xxx.get("blockId").asInt(0);
      var5xxx = var5xxx.remove("blockId");
      int var7xxxxx = var5xxx.get("blockData").asInt(0) & 15;
      var5xxx = var5xxx.remove("blockData");
      Dynamic<?> var8xxxxxx = BlockStateData.getTag(var6xxxx << 4 | var7xxxxx);
      Typed<?> var9xxxxxxx = (Typed)var2.pointTyped(var1.getOps()).orElseThrow(() -> new IllegalStateException("Could not create new piston block entity."));
      return var9xxxxxxx.set(DSL.remainderFinder(), var5xxx)
         .set(
                 var4xx,
            (var3x.readTyped(var8xxxxxx).result().orElseThrow(() -> new IllegalStateException("Could not parse newly created block state tag.")))
               .getFirst()
         );
   }
}
