package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.datafix.PackedBitStorage;

public class LeavesFix extends DataFix {
   private static final int[][] DIRECTIONS = new int[][]{{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
   private static final Object2IntMap<String> LEAVES = DataFixUtils.make(new Object2IntOpenHashMap(), var0 -> {
      var0.put("minecraft:acacia_leaves", 0);
      var0.put("minecraft:birch_leaves", 1);
      var0.put("minecraft:dark_oak_leaves", 2);
      var0.put("minecraft:jungle_leaves", 3);
      var0.put("minecraft:oak_leaves", 4);
      var0.put("minecraft:spruce_leaves", 5);
   });
   private static final Set<String> LOGS = ImmutableSet.of(
      "minecraft:acacia_bark",
      "minecraft:birch_bark",
      "minecraft:dark_oak_bark",
      "minecraft:jungle_bark",
      "minecraft:oak_bark",
      "minecraft:spruce_bark",
      new String[]{
         "minecraft:acacia_log",
         "minecraft:birch_log",
         "minecraft:dark_oak_log",
         "minecraft:jungle_log",
         "minecraft:oak_log",
         "minecraft:spruce_log",
         "minecraft:stripped_acacia_log",
         "minecraft:stripped_birch_log",
         "minecraft:stripped_dark_oak_log",
         "minecraft:stripped_jungle_log",
         "minecraft:stripped_oak_log",
         "minecraft:stripped_spruce_log"
      }
   );

   public LeavesFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> var1 = this.getInputSchema().getType(References.CHUNK);
      OpticFinder<?> var2x = var1.findField("Level");
      OpticFinder<?> var3 = var2x.type().findField("Sections");
      Type<?> var4xxx = var3.type();
      if (!(var4xxx instanceof ListType)) {
         throw new IllegalStateException("Expecting sections to be a list.");
      } else {
         Type<?> var5 = ((ListType)var4xxx).getElement();
         OpticFinder<?> var6x = DSL.typeFinder(var5);
         return this.fixTypeEverywhereTyped(
            "Leaves fix",
                 var1,
            var4x -> var4x.updateTyped(
                    var3,
                  var3xx -> {
                     int[] var4xx = new int[]{0};
                     Typed<?> var5x = var3xx.updateTyped(
                             var3,
                        var3xxx -> {
                           Int2ObjectMap<LeavesFix.LeavesSection> var4xxx_ = new Int2ObjectOpenHashMap(
                              var3xxx.getAllTyped(var6x)
                                 .stream()
                                 .map(var1xxxx -> new LeavesFix.LeavesSection(var1xxxx, this.getInputSchema()))
                                 .collect(Collectors.toMap(LeavesFix.Section::getIndex, var0 -> var0))
                           );
                           if (var4xxx_.values().stream().allMatch(LeavesFix.Section::isSkippable)) {
                              return var3xxx;
                           } else {
                              List<IntSet> var5xx = Lists.newArrayList();

                              for(int ix = 0; ix < 7; ++ix) {
                                 var5xx.add(new IntOpenHashSet());
                              }

                              ObjectIterator var25 = var4xxx_.values().iterator();

                              while(var25.hasNext()) {
                                 LeavesFix.LeavesSection var7x = (LeavesFix.LeavesSection)var25.next();
                                 if (!var7x.isSkippable()) {
                                    for(int ixx = 0; ixx < 4096; ++ixx) {
                                       int var9xxx = var7x.getBlock(ixx);
                                       if (var7x.isLog(var9xxx)) {
                                          ((IntSet)var5xx.get(0)).add(var7x.getIndex() << 12 | ixx);
                                       } else if (var7x.isLeaf(var9xxx)) {
                                          int var10xxx = this.getX(ixx);
                                          int var11xxxx = this.getZ(ixx);
                                          var4xx[0] |= getSideMask(var10xxx == 0, var10xxx == 15, var11xxxx == 0, var11xxxx == 15);
                                       }
                                    }
                                 }
                              }

                              for(int ix = 1; ix < 7; ++ix) {
                                 IntSet var27xx = (IntSet)var5xx.get(ix - 1);
                                 IntSet var28xxx = (IntSet)var5xx.get(ix);
                                 IntIterator var29xxxx = var27xx.iterator();

                                 while(var29xxxx.hasNext()) {
                                    int var30xxxxx = var29xxxx.nextInt();
                                    int var31xxxxxx = this.getX(var30xxxxx);
                                    int var12xxxxxxx = this.getY(var30xxxxx);
                                    int var13xxxxxxxx = this.getZ(var30xxxxx);

                                    for(int[] var17 : DIRECTIONS) {
                                       int var18xxxxxxxxx = var31xxxxxx + var17[0];
                                       int var19xxxxxxxxxx = var12xxxxxxx + var17[1];
                                       int var20xxxxxxxxxxx = var13xxxxxxxx + var17[2];
                                       if (var18xxxxxxxxx >= 0
                                          && var18xxxxxxxxx <= 15
                                          && var20xxxxxxxxxxx >= 0
                                          && var20xxxxxxxxxxx <= 15
                                          && var19xxxxxxxxxx >= 0
                                          && var19xxxxxxxxxx <= 255) {
                                          LeavesFix.LeavesSection var21xxxxxxxxxxxx = (LeavesFix.LeavesSection)var4xxx_.get(var19xxxxxxxxxx >> 4);
                                          if (var21xxxxxxxxxxxx != null && !var21xxxxxxxxxxxx.isSkippable()) {
                                             int var22xxxxxxxxxxxxx = getIndex(var18xxxxxxxxx, var19xxxxxxxxxx & 15, var20xxxxxxxxxxx);
                                             int var23xxxxxxxxxxxxxx = var21xxxxxxxxxxxx.getBlock(var22xxxxxxxxxxxxx);
                                             if (var21xxxxxxxxxxxx.isLeaf(var23xxxxxxxxxxxxxx)) {
                                                int var24xxxxxxxxxxxxxxx = var21xxxxxxxxxxxx.getDistance(var23xxxxxxxxxxxxxx);
                                                if (var24xxxxxxxxxxxxxxx > ix) {
                                                   var21xxxxxxxxxxxx.setDistance(var22xxxxxxxxxxxxx, var23xxxxxxxxxxxxxx, ix);
                                                   var28xxx.add(getIndex(var18xxxxxxxxx, var19xxxxxxxxxx, var20xxxxxxxxxxx));
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }

                              return var3xxx.updateTyped(
                                      var6x,
                                 var1xxxx -> ((LeavesFix.LeavesSection)var4xxx_.get(((Dynamic)var1xxxx.get(DSL.remainderFinder())).get("Y").asInt(0)))
                                       .write(var1xxxx)
                              );
                           }
                        }
                     );
                     if (var4xx[0] != 0) {
                        var5x = var5x.update(DSL.remainderFinder(), var1xxx -> {
                           Dynamic<?> var2xxx = (Dynamic)DataFixUtils.orElse(var1xxx.get("UpgradeData").result(), var1xxx.emptyMap());
                           return var1xxx.set("UpgradeData", var2xxx.set("Sides", var1xxx.createByte((byte)(var2xxx.get("Sides").asByte((byte)0) | var4xx[0]))));
                        });
                     }

                     return var5x;
                  }
               )
         );
      }
   }

   public static int getIndex(int var0, int var1, int var2) {
      return var1 << 8 | var2 << 4 | var0;
   }

   private int getX(int var1) {
      return var1 & 15;
   }

   private int getY(int var1) {
      return var1 >> 8 & 0xFF;
   }

   private int getZ(int var1) {
      return var1 >> 4 & 15;
   }

   public static int getSideMask(boolean var0, boolean var1, boolean var2, boolean var3) {
      int var4 = 0;
      if (var2) {
         if (var1) {
            var4 |= 2;
         } else if (var0) {
            var4 |= 128;
         } else {
            var4 |= 1;
         }
      } else if (var3) {
         if (var0) {
            var4 |= 32;
         } else if (var1) {
            var4 |= 8;
         } else {
            var4 |= 16;
         }
      } else if (var1) {
         var4 |= 4;
      } else if (var0) {
         var4 |= 64;
      }

      return var4;
   }

   public static final class LeavesSection extends LeavesFix.Section {
      @Nullable
      private IntSet leaveIds;
      @Nullable
      private IntSet logIds;
      @Nullable
      private Int2IntMap stateToIdMap;

      public LeavesSection(Typed<?> var1, Schema var2) {
         super(var1, var2);
      }

      @Override
      protected boolean skippable() {
         this.leaveIds = new IntOpenHashSet();
         this.logIds = new IntOpenHashSet();
         this.stateToIdMap = new Int2IntOpenHashMap();

         for(int var1 = 0; var1 < this.palette.size(); ++var1) {
            Dynamic<?> var2x = (Dynamic)this.palette.get(var1);
            String var3xx = var2x.get("Name").asString("");
            if (LeavesFix.LEAVES.containsKey(var3xx)) {
               boolean var4xxx = Objects.equals(var2x.get("Properties").get("decayable").asString(""), "false");
               this.leaveIds.add(var1);
               this.stateToIdMap.put(this.getStateId(var3xx, var4xxx, 7), var1);
               this.palette.set(var1, this.makeLeafTag(var2x, var3xx, var4xxx, 7));
            }

            if (LeavesFix.LOGS.contains(var3xx)) {
               this.logIds.add(var1);
            }
         }

         return this.leaveIds.isEmpty() && this.logIds.isEmpty();
      }

      private Dynamic<?> makeLeafTag(Dynamic<?> var1, String var2, boolean var3, int var4) {
         Dynamic<?> var5 = var1.emptyMap();
         var5 = var5.set("persistent", var5.createString(var3 ? "true" : "false"));
         var5 = var5.set("distance", var5.createString(Integer.toString(var4)));
         Dynamic<?> var6x = var1.emptyMap();
         var6x = var6x.set("Properties", var5);
         return var6x.set("Name", var6x.createString(var2));
      }

      public boolean isLog(int var1) {
         return this.logIds.contains(var1);
      }

      public boolean isLeaf(int var1) {
         return this.leaveIds.contains(var1);
      }

      private int getDistance(int var1) {
         return this.isLog(var1) ? 0 : Integer.parseInt(((Dynamic)this.palette.get(var1)).get("Properties").get("distance").asString(""));
      }

      private void setDistance(int var1, int var2, int var3) {
         Dynamic<?> var4 = (Dynamic)this.palette.get(var2);
         String var5x = var4.get("Name").asString("");
         boolean var6xx = Objects.equals(var4.get("Properties").get("persistent").asString(""), "true");
         int var7xxx = this.getStateId(var5x, var6xx, var3);
         if (!this.stateToIdMap.containsKey(var7xxx)) {
            int var8xxxx = this.palette.size();
            this.leaveIds.add(var8xxxx);
            this.stateToIdMap.put(var7xxx, var8xxxx);
            this.palette.add(this.makeLeafTag(var4, var5x, var6xx, var3));
         }

         int var11 = this.stateToIdMap.get(var7xxx);
         if (1 << this.storage.getBits() <= var11) {
            PackedBitStorage var9x = new PackedBitStorage(this.storage.getBits() + 1, 4096);

            for(int var10xx = 0; var10xx < 4096; ++var10xx) {
               var9x.set(var10xx, this.storage.get(var10xx));
            }

            this.storage = var9x;
         }

         this.storage.set(var1, var11);
      }
   }

   public abstract static class Section {
      private final Type<Pair<String, Dynamic<?>>> blockStateType = DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType());
      protected final OpticFinder<List<Pair<String, Dynamic<?>>>> paletteFinder = DSL.fieldFinder("Palette", DSL.list(this.blockStateType));
      protected final List<Dynamic<?>> palette;
      protected final int index;
      @Nullable
      protected PackedBitStorage storage;

      public Section(Typed<?> var1, Schema var2) {
         if (!Objects.equals(var2.getType(References.BLOCK_STATE), this.blockStateType)) {
            throw new IllegalStateException("Block state type is not what was expected.");
         } else {
            Optional<List<Pair<String, Dynamic<?>>>> var3 = var1.getOptional(this.paletteFinder);
            this.palette = var3.<List<Dynamic<?>>>map(var0 -> var0.stream().map(Pair::getSecond).collect(Collectors.toList())).orElse(ImmutableList.of());
            Dynamic<?> var4x = (Dynamic)var1.get(DSL.remainderFinder());
            this.index = var4x.get("Y").asInt(0);
            this.readStorage(var4x);
         }
      }

      protected void readStorage(Dynamic<?> var1) {
         if (this.skippable()) {
            this.storage = null;
         } else {
            long[] var2 = var1.get("BlockStates").asLongStream().toArray();
            int var3x = Math.max(4, DataFixUtils.ceillog2(this.palette.size()));
            this.storage = new PackedBitStorage(var3x, 4096, var2);
         }
      }

      public Typed<?> write(Typed<?> var1) {
//         return this.isSkippable()
//                 ? var1
//                 : var1.update(DSL.remainderFinder(), var1x -> var1x.set("BlockStates", var1x.createLongList(Arrays.stream(this.storage.getRaw()))))
//                 .set(this.paletteFinder, this.palette.stream().map(var0 -> Pair.of(References.BLOCK_STATE.typeName(), var0)).collect(Collectors.toList()));

         if (this.isSkippable()) {
            return var1;
         }

         // First update the BlockStates
         Typed<?> updated = var1.update(DSL.remainderFinder(), var1x ->
                 var1x.set("BlockStates", var1x.createLongList(Arrays.stream(this.storage.getRaw())))
         );

         // Create the palette list with explicit type handling
         List<Pair<String, Dynamic<?>>> paletteList = this.palette.stream()
                 .<Pair<String, Dynamic<?>>>map(var0 ->
                         Pair.of(References.BLOCK_STATE.typeName(), var0)
                 )
                 .collect(Collectors.toList());

         // Set the palette
         return updated.set(this.paletteFinder, paletteList);
      }

      public boolean isSkippable() {
         return this.storage == null;
      }

      public int getBlock(int var1) {
         return this.storage.get(var1);
      }

      protected int getStateId(String var1, boolean var2, int var3) {
         return LeavesFix.LEAVES.get(var1) << 5 | (var2 ? 16 : 0) | var3;
      }

      int getIndex() {
         return this.index;
      }

      protected abstract boolean skippable();
   }
}
