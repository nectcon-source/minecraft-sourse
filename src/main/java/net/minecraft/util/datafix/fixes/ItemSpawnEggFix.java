package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemSpawnEggFix extends DataFix {
   private static final String[] ID_TO_ENTITY = (String[])DataFixUtils.make(new String[256], var0 -> {
      var0[1] = "Item";
      var0[2] = "XPOrb";
      var0[7] = "ThrownEgg";
      var0[8] = "LeashKnot";
      var0[9] = "Painting";
      var0[10] = "Arrow";
      var0[11] = "Snowball";
      var0[12] = "Fireball";
      var0[13] = "SmallFireball";
      var0[14] = "ThrownEnderpearl";
      var0[15] = "EyeOfEnderSignal";
      var0[16] = "ThrownPotion";
      var0[17] = "ThrownExpBottle";
      var0[18] = "ItemFrame";
      var0[19] = "WitherSkull";
      var0[20] = "PrimedTnt";
      var0[21] = "FallingSand";
      var0[22] = "FireworksRocketEntity";
      var0[23] = "TippedArrow";
      var0[24] = "SpectralArrow";
      var0[25] = "ShulkerBullet";
      var0[26] = "DragonFireball";
      var0[30] = "ArmorStand";
      var0[41] = "Boat";
      var0[42] = "MinecartRideable";
      var0[43] = "MinecartChest";
      var0[44] = "MinecartFurnace";
      var0[45] = "MinecartTNT";
      var0[46] = "MinecartHopper";
      var0[47] = "MinecartSpawner";
      var0[40] = "MinecartCommandBlock";
      var0[48] = "Mob";
      var0[49] = "Monster";
      var0[50] = "Creeper";
      var0[51] = "Skeleton";
      var0[52] = "Spider";
      var0[53] = "Giant";
      var0[54] = "Zombie";
      var0[55] = "Slime";
      var0[56] = "Ghast";
      var0[57] = "PigZombie";
      var0[58] = "Enderman";
      var0[59] = "CaveSpider";
      var0[60] = "Silverfish";
      var0[61] = "Blaze";
      var0[62] = "LavaSlime";
      var0[63] = "EnderDragon";
      var0[64] = "WitherBoss";
      var0[65] = "Bat";
      var0[66] = "Witch";
      var0[67] = "Endermite";
      var0[68] = "Guardian";
      var0[69] = "Shulker";
      var0[90] = "Pig";
      var0[91] = "Sheep";
      var0[92] = "Cow";
      var0[93] = "Chicken";
      var0[94] = "Squid";
      var0[95] = "Wolf";
      var0[96] = "MushroomCow";
      var0[97] = "SnowMan";
      var0[98] = "Ozelot";
      var0[99] = "VillagerGolem";
      var0[100] = "EntityHorse";
      var0[101] = "Rabbit";
      var0[120] = "Villager";
      var0[200] = "EnderCrystal";
   });

   public ItemSpawnEggFix(Schema var1, boolean var2) {
      super(var1, var2);
   }

   public TypeRewriteRule makeRule() {
      Schema var1 = this.getInputSchema();
      Type<?> var2x = var1.getType(References.ITEM_STACK);
      OpticFinder<Pair<String, String>> var3xx = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<String> var4xxx = DSL.fieldFinder("id", DSL.string());
      OpticFinder<?> var5xxxx = var2x.findField("tag");
      OpticFinder<?> var6xxxxx = var5xxxx.type().findField("EntityTag");
      OpticFinder<?> var7xxxxxx = DSL.typeFinder(var1.getTypeRaw(References.ENTITY));
      Type<?> var8xxxxxxx = this.getOutputSchema().getTypeRaw(References.ENTITY);
      return this.fixTypeEverywhereTyped(
         "ItemSpawnEggFix", var2x,
         var6x -> {
            Optional<Pair<String, String>> var7x = var6x.getOptional(var3xx);
            if (var7x.isPresent() && Objects.equals(((Pair)var7x.get()).getSecond(), "minecraft:spawn_egg")) {
               Dynamic<?> var19_1x = (Dynamic)var6x.get(DSL.remainderFinder());
               short var9xx = var19_1x.get("Damage").asShort((short)0);
               Optional<? extends Typed<?>> var10xxx = var6x.getOptionalTyped(var5xxxx);
               Optional<? extends Typed<?>> var11xxxx = var10xxx.flatMap(var1xx -> var1xx.getOptionalTyped(var6xxxxx));
               Optional<? extends Typed<?>> var12xxxxx = var11xxxx.flatMap(var1xx -> var1xx.getOptionalTyped(var7xxxxxx));
               Optional<String> var13xxxxxx = var12xxxxx.flatMap(var1xx -> var1xx.getOptional(var4xxx));
               Typed<?> var14xxxxxxx = var6x;
               String var15xxxxxxxx = ID_TO_ENTITY[var9xx & 255];
               if (var15xxxxxxxx != null && (!var13xxxxxx.isPresent() || !Objects.equals(var13xxxxxx.get(), var15xxxxxxxx))) {
                  Typed<?> var16xxxxxxxxx = var6x.getOrCreateTyped(var5xxxx);
                  Typed<?> var17xxxxxxxxxx = var16xxxxxxxxx.getOrCreateTyped(var6xxxxx);
                  Typed<?> var18xxxxxxxxxxx = var17xxxxxxxxxx.getOrCreateTyped(var7xxxxxx);
//                  Dynamic<?> ☃xxxxxxxxxxxx = ☃x;
                  Dynamic<?> finalVar19_1x = var19_1x;
                  Typed<?> var20xxxxxxxxxxxxx = (Typed)((Pair)var18xxxxxxxxxxx.write()
                        .flatMap(var3xx_ -> var8xxxxxxx.readTyped(var3xx_.set("id", finalVar19_1x.createString(var15xxxxxxxx))))
                        .result()
                        .orElseThrow(() -> new IllegalStateException("Could not parse new entity")))
                     .getFirst();
                  var14xxxxxxx = var6x.set(var5xxxx, var16xxxxxxxxx.set(var6xxxxx, var17xxxxxxxxxx.set(var7xxxxxx, var20xxxxxxxxxxxxx)));
               }
   
               if (var9xx != 0) {
                  var19_1x = var19_1x.set("Damage", var19_1x.createShort((short)0));
                  var14xxxxxxx = var14xxxxxxx.set(DSL.remainderFinder(), var19_1x);
               }
   
               return var14xxxxxxx;
            } else {
               return var6x;
            }
         }
      );
   }
}
