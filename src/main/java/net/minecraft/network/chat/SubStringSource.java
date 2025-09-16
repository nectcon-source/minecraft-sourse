package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

public class SubStringSource {
   private final String plainText;
   private final List<Style> charStyles;
   private final Int2IntFunction reverseCharModifier;

   private SubStringSource(String var1, List<Style> var2, Int2IntFunction var3) {
      this.plainText = var1;
      this.charStyles = ImmutableList.copyOf(var2);
      this.reverseCharModifier = var3;
   }

   public String getPlainText() {
      return this.plainText;
   }

   public List<FormattedCharSequence> substring(int var1, int var2, boolean var3) {
      if (var2 == 0) {
         return ImmutableList.of();
      } else {
         List<FormattedCharSequence> var4 = Lists.newArrayList();
         Style var5x = this.charStyles.get(var1);
         int var6xx = var1;

         for(int var7xxx = 1; var7xxx < var2; ++var7xxx) {
            int var8xxxx = var1 + var7xxx;
            Style var9xxxxx = this.charStyles.get(var8xxxx);
            if (!var9xxxxx.equals(var5x)) {
               String var10xxxxxx = this.plainText.substring(var6xx, var8xxxx);
               var4.add(var3 ? FormattedCharSequence.backward(var10xxxxxx, var5x, this.reverseCharModifier) : FormattedCharSequence.forward(var10xxxxxx, var5x));
               var5x = var9xxxxx;
               var6xx = var8xxxx;
            }
         }

         if (var6xx < var1 + var2) {
            String var11xxx = this.plainText.substring(var6xx, var1 + var2);
            var4.add(var3 ? FormattedCharSequence.backward(var11xxx, var5x, this.reverseCharModifier) : FormattedCharSequence.forward(var11xxx, var5x));
         }

         return var3 ? Lists.reverse(var4) : var4;
      }
   }

   public static SubStringSource create(FormattedText var0, Int2IntFunction var1, UnaryOperator<String> var2) {
      StringBuilder var3 = new StringBuilder();
      List<Style> var4 = Lists.newArrayList();
      var0.visit((var2x, var3x) -> {
         StringDecomposer.iterateFormatted(var3x, var2x, (var2xx, var3xx, var4x) -> {
            var3.appendCodePoint(var4x);
            int var5 = Character.charCount(var4x);

            for(int var6x = 0; var6x < var5; ++var6x) {
               var4.add(var3xx);
            }

            return true;
         });
         return Optional.empty();
      }, Style.EMPTY);
      return new SubStringSource(var2.apply(var3.toString()), var4, var1);
   }
}
