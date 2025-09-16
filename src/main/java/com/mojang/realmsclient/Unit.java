//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.realmsclient;

import java.util.Locale;

public enum Unit {
   B,
   KB,
   MB,
   GB;

   private Unit() {
   }

   public static Unit getLargest(long var0) {
      if (var0 < 1024L) {
         return B;
      } else {
         try {
            int var2 = (int)(Math.log((double)var0) / Math.log((double)1024.0F));
            String var3 = String.valueOf("KMGTPE".charAt(var2 - 1));
            return valueOf(var3 + "B");
         } catch (Exception var4) {
            return GB;
         }
      }
   }

   public static double convertTo(long var0, Unit var2) {
      return var2 == B ? (double)var0 : (double)var0 / Math.pow((double)1024.0F, (double)var2.ordinal());
   }

   public static String humanReadable(long var0) {
      int var2 = 1024;
      if (var0 < 1024L) {
         return var0 + " B";
      } else {
         int var3 = (int)(Math.log((double)var0) / Math.log((double)1024.0F));
         String var4 = "KMGTPE".charAt(var3 - 1) + "";
         return String.format(Locale.ROOT, "%.1f %sB", (double)var0 / Math.pow((double)1024.0F, (double)var3), var4);
      }
   }

   public static String humanReadable(long var0, Unit var2) {
      return String.format("%." + (var2 == GB ? "1" : "0") + "f %s", convertTo(var0, var2), var2.name());
   }
}
