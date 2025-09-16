package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TrappedChestBlockEntityFix extends DataFix {
   private static final Logger LOGGER = LogManager.getLogger();

   public TrappedChestBlockEntityFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   public TypeRewriteRule makeRule() {
      Type<?> var1 = this.getOutputSchema().getType(References.CHUNK);
      Type<?> var2x = var1.findFieldType("Level");
      Type<?> var3xx = var2x.findFieldType("TileEntities");
      if (!(var3xx instanceof ListType)) {
         throw new IllegalStateException("Tile entity type is not a list type.");
      } else {
         ListType<?> var4 = (ListType)var3xx;
         OpticFinder<? extends List<?>> var5 = DSL.fieldFinder("TileEntities", var4);
         Type<?> var6xx = this.getInputSchema().getType(References.CHUNK);
         OpticFinder<?> var7xxx = var6xx.findField("Level");
         OpticFinder<?> var8xxxx = var7xxx.type().findField("Sections");
         Type<?> var9xxxxx = var8xxxx.type();
         if (!(var9xxxxx instanceof ListType)) {
            throw new IllegalStateException("Expecting sections to be a list.");
         } else {
            Type<?> var10 = ((ListType)var9xxxxx).getElement();
            OpticFinder<?> var11x = DSL.typeFinder(var10);
            return TypeRewriteRule.seq(
               new AddNewChoices(this.getOutputSchema(), "AddTrappedChestFix", References.BLOCK_ENTITY).makeRule(),
               this.fixTypeEverywhereTyped("Trapped Chest fix", var6xx, var5x -> var5x.updateTyped(var7xxx, var4xx -> {
                     Optional<? extends Typed<?>> var5xx = var4xx.getOptionalTyped(var8xxxx);
                     if (!var5xx.isPresent()) {
                        return var4xx;
                     } else {
                        List<? extends Typed<?>> var6x = ((Typed)var5xx.get()).getAllTyped(var11x);
                        IntSet var7x = new IntOpenHashSet();

                        for(Typed<?> var9x : var6x) {
                           TrappedChestBlockEntityFix.TrappedChestSection var10xx = new TrappedChestBlockEntityFix.TrappedChestSection(var9x, this.getInputSchema());
                           if (!var10xx.isSkippable()) {
                              for(int var11xxx = 0; var11xxx < 4096; ++var11xxx) {
                                 int var12xxxx = var10xx.getBlock(var11xxx);
                                 if (var10xx.isTrappedChest(var12xxxx)) {
                                    var7x.add(var10xx.getIndex() << 12 | var11xxx);
                                 }
                              }
                           }
                        }

                        Dynamic<?> var13xx = (Dynamic)var4xx.get(DSL.remainderFinder());
                        int var14xxx = var13xx.get("xPos").asInt(0);
                        int var15xxxx = var13xx.get("zPos").asInt(0);
                        TaggedChoiceType<String> var16xxxxx = (TaggedChoiceType<String>) this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
                        return var4xx.updateTyped(var5, var4xxx -> var4xxx.updateTyped(var16xxxxx.finder(), var4xxxx -> {
                              Dynamic<?> var5xxx = (Dynamic)var4xxxx.getOrCreate(DSL.remainderFinder());
                              int var6x_ = var5xxx.get("x").asInt(0) - (var14xxx << 4);
                              int var7xx = var5xxx.get("y").asInt(0);
                              int var8xxx = var5xxx.get("z").asInt(0) - (var15xxxx << 4);
                              return var7x.contains(LeavesFix.getIndex(var6x_, var7xx, var8xxx)) ? var4xxxx.update(var16xxxxx.finder(), var0xx -> var0xx.mapFirst(var0xxx -> {
                                    if (!Objects.equals(var0xxx, "minecraft:chest")) {
                                       LOGGER.warn("Block Entity was expected to be a chest");
                                    }

                                    return "minecraft:trapped_chest";
                                 })) : var4xxxx;
                           }));
                     }
                  }))
            );
         }
      }
   }

   public static final class TrappedChestSection extends LeavesFix.Section {
      @Nullable
      private IntSet chestIds;

      public TrappedChestSection(Typed<?> var1, Schema var2) {
         super(var1, var2);
      }

      @Override
      protected boolean skippable() {
         this.chestIds = new IntOpenHashSet();

         for(int var1 = 0; var1 < this.palette.size(); ++var1) {
            Dynamic<?> var2 = (Dynamic)this.palette.get(var1);
            String var3 = var2.get("Name").asString("");
            if (Objects.equals(var3, "minecraft:trapped_chest")) {
               this.chestIds.add(var1);
            }
         }

         return this.chestIds.isEmpty();
      }

      public boolean isTrappedChest(int var1) {
         return this.chestIds.contains(var1);
      }
   }
}
