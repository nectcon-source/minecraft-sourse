package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class BedBlockEntityInjecter extends DataFix {
   public BedBlockEntityInjecter(Schema var1, boolean var2) {
      super(var1, var2);
   }

   public TypeRewriteRule makeRule() {
      Type<?> var1 = this.getOutputSchema().getType(References.CHUNK);
      Type<?> var2 = var1.findFieldType("Level");
      Type<?> var3 = var2.findFieldType("TileEntities");
      if (!(var3 instanceof ListType)) {
         throw new IllegalStateException("Tile entity type is not a list type.");
      } else {
         ListType<?> var4 = (ListType)var3;
         return this.cap(var2, var4);
      }
   }

//   private <TE> TypeRewriteRule cap(Type<?> var1, ListType<TE> var2) {
//      Type<TE> var3 = var2.getElement();
//      OpticFinder<?> var4 = DSL.fieldFinder("Level", var1);
//      OpticFinder<List<TE>> var5xx = DSL.fieldFinder("TileEntities", var2);
//      int var6xxx = 416;
//      return TypeRewriteRule.seq(
//         this.fixTypeEverywhere(
//            "InjectBedBlockEntityType",
//            this.getInputSchema().findChoiceType(References.BLOCK_ENTITY),
//            this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY),
//            (var0) -> (var0x) -> var0x
//         ),
//         this.fixTypeEverywhereTyped(
//            "BedBlockEntityInjecter",
//            this.getOutputSchema().getType(References.CHUNK),
//            var3x -> {
//               Typed<?> var4x = var3x.getTyped(var4);
//               Dynamic<?> var5x = (Dynamic)var4x.get(DSL.remainderFinder());
//               int var6xx = var5x.get("xPos").asInt(0);
//               int var7xxx = var5x.get("zPos").asInt(0);
//               List<TE> var8xxxx = Lists.newArrayList((Iterable)var4x.getOrCreate(var5xx));
//               List<? extends Dynamic<?>> var9xxxxx = var5x.get("Sections").asList(Function.identity());
//
//               for(int var10xxxxxx = 0; var10xxxxxx < var9xxxxx.size(); ++var10xxxxxx) {
//                  Dynamic<?> var11xxxxxxx = (Dynamic)var9xxxxx.get(var10xxxxxx);
//                  int var12xxxxxxxx = var11xxxxxxx.get("Y").asInt(0);
//                  Stream<Integer> var13xxxxxxxxx = var11xxxxxxx.get("Blocks").asStream().map(var0x -> var0x.asInt(0));
//                  int var14xxxxxxxxxx = 0;
//                  var13xxxxxxxxx.getClass();
//                  for(int var16 : var13xxxxxxxxx::iterator) {
//                     if (416 == (var16 & 0xFF) << 4) {
//                        int var17xxxxxxxxxxx = var14xxxxxxxxxx & 15;
//                        int var18xxxxxxxxxxxx = var14xxxxxxxxxx >> 8 & 15;
//                        int var19xxxxxxxxxxxxx = var14xxxxxxxxxx >> 4 & 15;
//                        Map<Dynamic<?>, Dynamic<?>> var20xxxxxxxxxxxxxx = Maps.newHashMap();
//                        var20xxxxxxxxxxxxxx.put(var11xxxxxxx.createString("id"), var11xxxxxxx.createString("minecraft:bed"));
//                        var20xxxxxxxxxxxxxx.put(var11xxxxxxx.createString("x"), var11xxxxxxx.createInt(var17xxxxxxxxxxx + (var6xx << 4)));
//                        var20xxxxxxxxxxxxxx.put(var11xxxxxxx.createString("y"), var11xxxxxxx.createInt(var18xxxxxxxxxxxx + (var12xxxxxxxx << 4)));
//                        var20xxxxxxxxxxxxxx.put(var11xxxxxxx.createString("z"), var11xxxxxxx.createInt(var19xxxxxxxxxxxxx + (var7xxx << 4)));
//                        var20xxxxxxxxxxxxxx.put(var11xxxxxxx.createString("color"), var11xxxxxxx.createShort((short)14));
//                        var8xxxx.add(
//                           (TE)(var3.read(var11xxxxxxx.createMap(var20xxxxxxxxxxxxxx))
//                                 .result()
//                                 .orElseThrow(() -> new IllegalStateException("Could not parse newly created bed block entity.")))
//                              .getFirst()
//                        );
//                     }
//
//                     ++var14xxxxxxxxxx;
//                  }
//               }
//
//               return !var8xxxx.isEmpty() ? var3x.set(var4, var4x.set(var5xx, var8xxxx)) : var3x;
//            }
//         )
//      );
//   }
private <TE> TypeRewriteRule cap(Type<?> var1, ListType<TE> var2) {
    Type<TE> var3 = var2.getElement();
    OpticFinder<?> var4 = DSL.fieldFinder("Level", var1);
    OpticFinder<List<TE>> var5 = DSL.fieldFinder("TileEntities", var2);
    int bedId = 416;

//    return TypeRewriteRule.seq(
//            this.fixTypeEverywhere(
//                    "InjectBedBlockEntityType",
//                    this.getInputSchema().findChoiceType(References.BLOCK_ENTITY),
//                    this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY),
//                    (DynamicOps<?> ops) -> (input) -> input  // Proper function signature
//            )
    return TypeRewriteRule.seq(this.fixTypeEverywhere("InjectBedBlockEntityType", this.getInputSchema().findChoiceType(References.BLOCK_ENTITY), this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY), (p_233085_0_) -> {
                return (p_209696_0_) -> {
                    return (com.mojang.datafixers.util.Pair)p_209696_0_;
                };
            }),
            this.fixTypeEverywhereTyped(
                    "BedBlockEntityInjecter",
                    this.getOutputSchema().getType(References.CHUNK),
                    var3x -> {
                        Typed<?> var4x = var3x.getTyped(var4);
                        Dynamic<?> var5x = var4x.get(DSL.remainderFinder());
                        int xPos = var5x.get("xPos").asInt(0);
                        int zPos = var5x.get("zPos").asInt(0);
                        List<TE> tileEntities = Lists.newArrayList(var4x.getOrCreate(var5));
                        List<? extends Dynamic<?>> sections = var5x.get("Sections").asList(Function.identity());

                        for(Dynamic<?> section : sections) {
                            int yPos = section.get("Y").asInt(0);
                            Stream<Integer> blocks = section.get("Blocks").asStream().map(var0x -> var0x.asInt(0));
                            int index = 0;

                            Iterator<Integer> iterator = blocks.iterator();
                            while(iterator.hasNext()) {
                                int blockId = iterator.next();
                                if (bedId == (blockId & 0xFF) << 4) {
                                    int x = index & 15;
                                    int y = index >> 8 & 15;
                                    int z = index >> 4 & 15;

                                    Map<Dynamic<?>, Dynamic<?>> bedData = Maps.newHashMap();
                                    bedData.put(section.createString("id"), section.createString("minecraft:bed"));
                                    bedData.put(section.createString("x"), section.createInt(x + (xPos << 4)));
                                    bedData.put(section.createString("y"), section.createInt(y + (yPos << 4)));
                                    bedData.put(section.createString("z"), section.createInt(z + (zPos << 4)));
                                    bedData.put(section.createString("color"), section.createShort((short)14));

                                    tileEntities.add(
                                            var3.read(section.createMap(bedData))
                                                    .result()
                                                    .orElseThrow(() -> new IllegalStateException("Could not parse newly created bed block entity."))
                                                    .getFirst()
                                    );
                                }
                                index++;
                            }
                        }

                        return !tileEntities.isEmpty() ? var3x.set(var4, var4x.set(var5, tileEntities)) : var3x;
                    }
            )
    );
}
}
