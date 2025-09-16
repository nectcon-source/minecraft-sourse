//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.client.resources.metadata.animation;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class VillagerMetaDataSection {
   public static final VillagerMetadataSectionSerializer SERIALIZER = new VillagerMetadataSectionSerializer();
   private final Hat hat;

   public VillagerMetaDataSection(Hat var1) {
      this.hat = var1;
   }

   public Hat getHat() {
      return this.hat;
   }

   public static enum Hat {
      NONE("none"),
      PARTIAL("partial"),
      FULL("full");

      private static final Map<String, Hat> BY_NAME = (Map)Arrays.stream(values()).collect(Collectors.toMap(Hat::getName, (var0) -> var0));
      private final String name;

      private Hat(String var3) {
         this.name = var3;
      }

      public String getName() {
         return this.name;
      }

      public static Hat getByName(String var0) {
         return (Hat)BY_NAME.getOrDefault(var0, NONE);
      }
   }
}
