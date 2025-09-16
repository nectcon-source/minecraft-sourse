package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.Util;

public interface StatFormatter {
   DecimalFormat DECIMAL_FORMAT = Util.make(
      new DecimalFormat("########0.00"), var0 -> var0.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
   );
   StatFormatter DEFAULT = NumberFormat.getIntegerInstance(Locale.US)::format;
   StatFormatter DIVIDE_BY_TEN = var0 -> DECIMAL_FORMAT.format((double)var0 * 0.1);
   StatFormatter DISTANCE = var0 -> {
      double var1 = (double)var0 / (double)100.0F;
      double var3 = var1 / (double)1000.0F;
      if (var3 > (double)0.5F) {
         return DECIMAL_FORMAT.format(var3) + " km";
      } else {
         return var1 > (double)0.5F ? DECIMAL_FORMAT.format(var1) + " m" : var0 + " cm";
      }
   };
   StatFormatter TIME = var0 -> {
      double var1 = (double)var0 / (double)20.0F;
      double var3 = var1 / (double)60.0F;
      double var5 = var3 / (double)60.0F;
      double var7 = var5 / (double)24.0F;
      double var9 = var7 / (double)365.0F;
      if (var9 > (double)0.5F) {
         return DECIMAL_FORMAT.format(var9) + " y";
      } else if (var7 > (double)0.5F) {
         return DECIMAL_FORMAT.format(var7) + " d";
      } else if (var5 > (double)0.5F) {
         return DECIMAL_FORMAT.format(var5) + " h";
      } else {
         return var3 > (double)0.5F ? DECIMAL_FORMAT.format(var3) + " m" : var1 + " s";
      }
   };

   String format(int var1);
}
